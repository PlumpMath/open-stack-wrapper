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
               #^{:static true} [servicecall [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [operation [org.json.JSONObject] org.json.JSONObject]


               ])
  )




(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-json-java-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))

(defn java-json->clojure-json  [java-json-object]
  (clj-json/read-str (.toString java-json-object) :key-fn keyword))









(defn -json [json-java-object]
  (println json-java-object)
  (create-json-java-object (clj-json/write-str {:response {:hola "que tal?"}}))
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

(defn get-endpoints-structured [m]

  (let [eps (get-endpoints m)]
    {:token-id (get-in eps [:access :token :id])
     :eps (os-core/structured-endpoints eps)})
  )

(defn get-tenants [{:keys [url token-id]}]
  (os-core/tenants url token-id)
  )
(defn get-tokens [{:keys [url username password ]}]
  (os-core/tokens url username password)
  )

(defn -tokens [json-java-object]
  (create-json-java-object (clj-json/write-str (get-tokens (java-json->clojure-json json-java-object)))))

(defn -tenants [json-java-object]
  (create-json-java-object (clj-json/write-str (get-tenants (java-json->clojure-json json-java-object))))
  )

(defn -endpoints [json-java-object]
  (create-json-java-object (clj-json/write-str (get-endpoints-structured (java-json->clojure-json json-java-object))))
  )

(defn -servicecall [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-service-call (java-json->clojure-json json-java-object))))
  )
(defn -operation [json-java-object]
  (create-json-java-object
   (clj-json/write-str (get-operation (java-json->clojure-json json-java-object))))
  )

(comment
  (def login-properties (load-config "./login.properties"))

  (get-tokens login-properties)

  (def tokens-response *1)

  (def token-id (get-in tokens-response [:access :token :id]))

  (get-tenants (assoc (select-keys login-properties [:url]) :token-id token-id))


  (def tenants-response *1)
  (def tenant-name (-> (:tenants tenants-response ) first  :name))


  (get-endpoints (assoc (select-keys login-properties [:url :password :username]) :tenant-name tenant-name))

  (def endpoints-response  *1)

  (util/pprint-json-scheme endpoints-response)

  (def endpoints-structured (structured-endpoints endpoints-response))

  (util/pprint-json-scheme endpoints-structured)

  (def new-token-id (get-in endpoints-response [:access :token :id]))

  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/images"})




  (def service-call-response *1)


  (get-operation (assoc login-properties :tenant-name tenant-name :service-type "compute" :path "/images"))


  (defn create-json-operation []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")
      (.put "service-type" "compute")
      (.put "path" "/images")
      ))

  (defn create-json-endpoints []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")

      ))


  (get-endpoints-structured (java-json->clojure-json
                             (create-json-endpoints)))

  (def eps *1)

  (get-service-call {:eps-token-id (:token-id eps)
                     :url (get-in eps [:eps :compute :publicURL])
                     :path "/images"} )

  (def service-call-response *1)


  (defn create-json-service-call []
    (doto (JSONObject.)
      (.put "eps-token-id" (:token-id eps))
      (.put "url" (get-in eps [:eps :compute :publicURL]))
      (.put "path" "/images")))

  (-servicecall (create-json-service-call))

  )
