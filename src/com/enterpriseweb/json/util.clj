(ns com.enterpriseweb.json.util
  (:require [clojure.data.json :as clj-json])
  (:import [org.json JSONObject]))

(defn create-java-json-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))

(defn create-java-json
  ([clojure-json]
     (create-java-json-object clojure-json))
  ([clojure-json & ks]
     (reduce #(.put % (name %2) (%2 clojure-json) ) (JSONObject.) ks))
  )

(defn clojure-json->java-json [clojure-object]
  (create-java-json (clj-json/write-str clojure-object)))

(defn java-json->clojure-json  [java-json-object]
  (clj-json/read-str (.toString java-json-object) :key-fn keyword))

(defprotocol asociative
  (assoc+  [this x y])
  (get-in+ [this  nested-ks]))

(extend-protocol asociative
  JSONObject
  (assoc+ [this x y]
    (.put this  (name x) (name y)))
  (get-in+ [this   nested-ks]
    (let [this-clj (java-json->clojure-json this)]
      (get-in this-clj nested-ks)))
  )


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
