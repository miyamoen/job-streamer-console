(ns job-streamer.console.components.execution
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [put! <! chan pub sub unsub-all]]
            [job-streamer.console.format :as fmt]))

(defcomponent execution-view [step-executions owner]
  (render [_]
    (html
     [:div.ui.list.step-view
      (for [step-execution step-executions]
        [:div.item
         [:div.top.aligned.image
          [:i.play.icon]]
         [:div.content
          [:div.header (get-in step-execution [:step-execution/step :step/name])]
          (fmt/date-short (:step-execution/start-time step-execution))
          " - "
          (fmt/date-short (:step-execution/end-time step-execution))
          [:span.ui.label
           (name (get-in step-execution [:step-execution/batch-status :db/ident] :unknown))
           " [" (:step-execution/exit-status step-execution) "]"]
          (if-let [logs (not-empty (:step-execution/logs step-execution))]
            [:div.log.list
             (for [log logs]
               [:div.item
                [:div.content
                 [:div.description
                  [:span.date (fmt/date-medium (:execution-log/date log))]
                  (let [level (name (get-in log [:execution-log/level :db/ident]))]
                    [:span.ui.horizontal.label {:class (case level
                                                         "error" "red"
                                                         "warn" "yellow"
                                                         "info" "blue"
                                                         "")}
                     level])
                  [:span (:execution-log/message log)]]
                 (when-let [exception (:execution-log/exception log)]
                   [:div.description
                    [:pre.exception exception]])]])]
            [:div])]])])))
