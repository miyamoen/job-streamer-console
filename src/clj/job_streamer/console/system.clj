(ns job-streamer.console.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [job-streamer.console.middleware :refer [wrap-authorization]]
            [job-streamer.console.endpoint.console :refer [console-endpoint]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-authorization :auth]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   (meta-merge api-defaults {:static {:resources "job-streamer-console/public"}
                                               :cookies true
                                               :session true})
         :auth {:uri-config {:authentication-uri {:query {:response_type "code"
                                                          :scope "default"}}
                             :access-token-uri {:query {:grant_type "authorization_code"}}}}}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :console (endpoint-component
                   (fn [component]
                     (console-endpoint (merge (:console config)
                                              (get-in config [:app :auth]))))))
        (component/system-using
         {:http [:app]
          :app  [:console]
          :console []}))))

