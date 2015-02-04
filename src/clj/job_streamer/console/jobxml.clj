(ns job-streamer.console.jobxml
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml])
  (:import [org.jsoup Jsoup]))

(defn field-value [el field-name]
  (some-> el
          (.children)
          (.select (str "field[name=" field-name "]"))
          first
          (.text)))

(defn item-component [el type]
  (when-let [block (some-> el
                           (.select (str "block[type=" type "]"))
                           first)]
    {(keyword type "ref") (field-value block "ref")}))

(defn xml->chunk [chunk]
  {:chunk/reader    (item-component chunk "reader")
   :chunk/processor (item-component chunk "processor")
   :chunk/writer    (item-component chunk "writer")})

(defn xml->batchlet [batchlet]
  {:batchlet/ref (field-value batchlet "ref")})

(defn xml->property [prop]
  (when-let [prop-block (some-> prop
                                (.select "block[type=property]")
                                first)]
    {(keyword (field-value prop-block "name")) (field-value prop-block "value")}))

(defn xml->step [step]
  (merge
   {:step/id (field-value step "id")
    :step/properties (some->> (.select step "> value[name^=ADD]")
                                  (map (fn [prop] (xml->property prop)))
                                  (reduce merge))}
   (when-let [batchlet (some-> step
                               (.select "block[type=batchlet]")
                               first
                               xml->batchlet)]
     {:step/batchlet batchlet})
   (when-let [chunk (some-> step
                            (.select "block[type=chunk]")
                            first
                            xml->chunk)]
     {:step/chunk chunk})
   (when-let [next-step (some-> step
                                (.select "next > block[type=step]")
                                first
                                (field-value "id"))]
     {:step/next next-step})))

(defn xml->job [xml]
  (let [doc (Jsoup/parse xml)
        job-els (. doc select "xml > block[type=job]")]
    (if (= (count job-els) 1)
      (let [job (first job-els)]
        {:job/id (field-value job "id")
         :job/steps (some->> (.select job "> statement[name=steps] block[type=step]")
                         (map (fn [step] (xml->step step)))
                         vec)
         :job/properties (some->> (.select job "> value[name^=ADD]")
                                  (map (fn [prop] (xml->property prop)))
                                  (reduce merge))})
      (throw (IllegalStateException. "Block must be one.")))))


