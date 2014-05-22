(ns com.enterpriseweb.json.util
  (:require [clojure.data.json :as clj-json]
            [com.enterpriseweb.json.protocol :refer :all]
            [com.enterpriseweb.json.tools :refer :all])
  (:import [org.json JSONObject]))



(defn dispatch
  "MIDDLEWARE java-json-adapter: takes a java-json and returns a java-json"
  [json-java-object action-fn  ks]
  (let [-clj-object (java-json->clojure-json json-java-object)
        -selected-data (select-keys -clj-object ks)
        action-result  (action-fn -selected-data)
        to-json-java (clojure-json->java-json action-result)]
                                        ;    (println  action-fn -selected-data)
                                        ;   action-result
    to-json-java))


(defn json-url-delete-adapter-add-id
  ""
  [json-java-object path]
             (let [first-url (get-in+ json-java-object [:eps-url])
                   id (get-in+ json-java-object [:id])
                   modified-json (assoc+ json-java-object :url (str first-url path id))]
               modified-json))
