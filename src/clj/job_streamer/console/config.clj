(ns job-streamer.console.config
  (:require [environ.core :refer [env]]))

(def defaults
  {:http {:port 3000}
   :console {:control-bus-url "http://localhost:45102"}})

(def environ
  {:http {:port (some-> env :console-port Integer.)}
   :console {:control-bus-url (:control-bus-url env)}
   :app {:auth {:client-config {:client-id (:client-id env)
                                :client-secret (:client-secret env)
                                :callback {:domain (:domain env)
                                           :path "/login"}}
                :uri-config {:authentication-uri {:url (:auth-url env)
                                                  :query {:client_id (:client-id env)
                                                          :redirect_uri (str (:domain env) "/oauth")
                                                          :response_type (:response_type env)}}
                             :access-token-uri {:url (:token-url env)
                                                :query {:client_id (:client-id env)
                                                        :client_secret (:client-secret env)
                                                        :redirect_uri (str (:domain env) "/oauth")}}}}}})
