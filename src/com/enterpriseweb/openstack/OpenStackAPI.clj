(ns com.enterpriseweb.openstack.OpenStackAPI
  (:use [open-stack-wrapper.core :as os-core]
        [open-stack-wrapper.util :as util])
  (:require [clojure.data.json :as clj-json]
            [clojure.java.io :as io]
            )
  (:import [org.json JSONObject])
  (:gen-class :methods
              [#^{:static true} [json [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [tokens [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [tenants [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [endpoints [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [serviceCall [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [operation [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [createServer [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [createNetwork [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [createSubnet [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [deleteNetwork [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [deleteSubnet [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [deleteServer [org.json.JSONObject] org.json.JSONObject]
               ]))


                                        ;
;UTILITIES

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
    (.put this  (name x) y))
  (get-in+ [this   nested-ks]
    (let [this-clj (java-json->clojure-json this)]
      (get-in this-clj nested-ks)))
  )

                                        ; ADAPTERS
(defn delete-entity [{:keys [eps-token-id url]}]
  (os-core/delete eps-token-id url )
  )

(defn get-service-call [{:keys [eps-token-id url path]}]
  (os-core/service-call eps-token-id url path)
  )

(defn get-operation [{:keys [url username password tenant-name service-type path]}]
  (let [service-type (if (keyword? service-type) service-type (keyword service-type))]
    (os-core/operation url username password tenant-name service-type path))
  )

(defn get-create-server [{:keys [token-id nova-url server-name flavor-href image-href network-id]}]
  (os-core/create-server token-id nova-url server-name flavor-href image-href network-id)
  )

(defn get-create-network [{:keys [token-id quantum-url network-name]}]
  (os-core/create-network token-id quantum-url network-name)
  )

(defn get-create-subnet [{:keys [token-id quantum-url network-id cidr start end]}]
  (os-core/create-subnet token-id quantum-url network-id cidr start end)
  )

                                        ; JSON CALLS

(defn dispatch [json-java-object action-fn & ks]
  (let [-clj-object (java-json->clojure-json json-java-object)
        -selected-data (select-keys -clj-object ks)
        action-result  (action-fn -selected-data)
       to-json-java (clojure-json->java-json action-result)
        ]
 ;   action-result
   to-json-java
   ))

(defn -tokens [json-java-object]
  (dispatch json-java-object os-core/tokens :url :username :password)
  )

(defn -tenants [json-java-object]
  (dispatch json-java-object os-core/tenants :url :token-id)
  )

(defn -endpoints [json-java-object]
  (dispatch json-java-object os-core/endpoints-adaptated  :url :username :password :tenant-name)

  )

(defn -serviceCall [json-java-object]
  (dispatch json-java-object os-core/service-call  :url :eps-token-id :path)
  )

(defn -delete [json-java-object path]
  (let [c (java-json->clojure-json json-java-object)
        modified-json (assoc c :url (str (:eps-url c) path (:id c)))]
    (create-java-json
     (clj-json/write-str (delete-entity modified-json)))))

(defn -deleteNetwork [json-java-object]
  (-delete json-java-object "v2.0/networks/"))

(defn -deleteSubnet [json-java-object]
  (-delete json-java-object "v2.0/subnets/"))

(defn -deleteServer [json-java-object]
  (-delete json-java-object "/servers/"))

(defn -createServer [json-java-object]
  (create-java-json
   (clj-json/write-str (get-create-server (java-json->clojure-json json-java-object))))
  )

(defn -createNetwork [json-java-object]
  (create-java-json
   (clj-json/write-str (get-create-network (java-json->clojure-json json-java-object))))
  )

(defn -createSubnet [json-java-object]
  (create-java-json
   (clj-json/write-str (get-create-subnet (java-json->clojure-json json-java-object))))
  )

(defn -operation [json-java-object]
  (create-java-json
   (clj-json/write-str (get-operation (java-json->clojure-json json-java-object))))
  )

(defn -json [json-java-object]
  (println json-java-object)
  (create-java-json (clj-json/write-str {:response {:hola "que tal?"}}))
  )
