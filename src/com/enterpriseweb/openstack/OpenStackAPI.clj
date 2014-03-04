(ns com.enterpriseweb.openstack.OpenStackAPI
  #_(:use [ew_snmp.v2 :only (v2-json)]
        [com.enterpriseweb.snmp.utils])
  (:require [clojure.data.json :as clj-json][clojure.java.io :as io])
  (:import [org.json JSONObject])
  (:gen-class :methods
              [#^{:static true} [json [org.json.JSONObject] org.json.JSONObject]])
  )


(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-json-java-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))



(defn -json [json-java-object]
  (println json-java-object)
  (create-json-java-object (clj-json/write-str {:response {:hola "que tal?"}}))
  )
