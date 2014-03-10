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

; UTILITIES

(defn create-json-java-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))

(defn clojure-json->java-json [clojure-object]
  (create-json-java-object (clj-json/write-str clojure-object)))


(defn java-json->clojure-json  [java-json-object]
  (clj-json/read-str (.toString java-json-object) :key-fn keyword))


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

(defn get-endpoints [{:keys [url username password tenant-name]}]
  (os-core/endpoints url username password tenant-name)
  )

(defn get-tokens [{:keys [url username password ]}]
  (os-core/tokens url username password)
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
(defn -tokens [json-java-object]
  (dispatch json-java-object os-core/tokens :url :username :password)
)


(comment
  defn select-data [m ks]
  (let [mapa {:uno 1 :dos 2 :tres 3}
       claves [:uno :tres]]
   (map #(get mapa %) claves)
   (select-keys mapa claves)
   ))

(defn dispatch [json-java-object action-fn & ks]
  (let [-clj-object (java-json->clojure-json json-java-object)
        -selected-data (select-keys -clj-object ks)
        action-result  (action-fn -selected-data)
        to-json-java (clojure-json->java-json action-result)
        ]
    to-json-java
))



(defn -tenants [json-java-object]
  (dispatch json-java-object os-core/tenants :url :token-id)
  )

(defn -endpoints
  "also included the "
  [json-java-object]
  (let [ready-json-clojure (java-json->clojure-json json-java-object)
        eps (get-endpoints ready-json-clojure)
        res {:token-id (get-in eps [:access :token :id])
             :eps (os-core/structured-endpoints eps)}
        ]
    (create-json-java-object (clj-json/write-str res))
    )


  )

(defn -serviceCall [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-service-call (java-json->clojure-json json-java-object))))
  )

(defn -delete [json-java-object path]
  (let [c (java-json->clojure-json json-java-object)
        modified-json (assoc c :url (str (:eps-url c) path (:id c)))]
    (create-json-java-object
     (clj-json/write-str (delete-entity modified-json)))))

(defn -deleteNetwork [json-java-object]
  (-delete json-java-object "v2.0/networks/"))

(defn -deleteSubnet [json-java-object]
  (-delete json-java-object "v2.0/subnets/"))

(defn -deleteServer [json-java-object]
  (-delete json-java-object "/servers/"))

(defn -createServer [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-create-server (java-json->clojure-json json-java-object))))
  )

(defn -createNetwork [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-create-network (java-json->clojure-json json-java-object))))
  )

(defn -createSubnet [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-create-subnet (java-json->clojure-json json-java-object))))
  )

(defn -operation [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-operation (java-json->clojure-json json-java-object))))
  )

(defn -json [json-java-object]
  (println json-java-object)
  (create-json-java-object (clj-json/write-str {:response {:hola "que tal?"}}))
  )
