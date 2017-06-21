(ns job-streamer.console.middleware
  (:require [clojure.tools.logging :as log]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util :as outil]))

(defn wrap-authorization [handler config]
  (fn [request]
    (log/error "みちゃうぞー : " config)
    (let [new-config (-> config
                         (assoc :auth-error-fn (fn [error] nil))
                         (assoc :credential-fn (fn [creds] creds)))
          workflow (oauth2/workflow new-config)]
      (if-let [access-token (or (get-in request [:params :access-token])
                                (:access-token (workflow request)))
        (-> request
            (assoc-in [:session :access-token] access-token)
            handler)
        (-> request
            (assoc-in [:session :state] (outil/generate-anti-forgery-token))
            (handler request))))))
