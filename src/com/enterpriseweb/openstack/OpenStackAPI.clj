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
               ])
  )

; UTILITIES



(defn create-json-java-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))

(defn java-json->clojure-json  [java-json-object]
  (clj-json/read-str (.toString java-json-object) :key-fn keyword))


; ADAPTERS
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

(defn get-tenants [{:keys [url token-id]}]
  (os-core/tenants url token-id)
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
  (create-json-java-object (clj-json/write-str (get-tokens (java-json->clojure-json json-java-object)))))

(defn -tenants [json-java-object]
  (create-json-java-object (clj-json/write-str (get-tenants (java-json->clojure-json json-java-object))))
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


; INTERACTIVE TEST
(comment
  (def login-properties (util/load-config "./login.properties"))

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


  (get-operation (assoc login-properties :tenant-name tenant-name :service-type "compute" :path "/images"))

  (def operation-response *1)

  (def new-token-id (get-in endpoints-response [:access :token :id]))


  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/images"})

  (def images-response *1)


  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/flavors"})

  (def flavors-response *1)


  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:network :publicURL] ) :path "v2.0/networks"})

  (def networks-response *1)


  (util/pprint-json-scheme images-response)

  (map (juxt :id :name #(:href (first (:links %)))) (:images  images-response))




  (defn create-json-create-server []
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "nova-url" (:publicURL (:compute endpoints-structured)))

      (.put "server-name" "eeeeeeaaaaaa")
      (.put "flavor-href" (:href (first (:links (last (:flavors  flavors-response))))))
      (.put "image-href" (:href (first (:links (last (:images  images-response))))))
      (.put "network-id" (:id (last (:networks networks-response))))
      )
    )

  (-createServer (create-json-create-server))


  (defn create-json-create-network []
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "quantum-url" (:publicURL (:network endpoints-structured)))
      (.put "network-name" "ofuuuu")
      )
    )

  (-createNetwork (create-json-create-network))

  (defn create-json-create-subnet []
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "quantum-url" (:publicURL (:network endpoints-structured)))
      (.put "network-id" (:id (last (:networks networks-response))))
      (.put "cidr" "192.168.198.0/24")
      (.put "start" "192.168.198.40")
      (.put "end" "192.168.198.50")
      )
    )

  (-createSubnet (create-json-create-subnet))



  (defn create-json-operation []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")
      (.put "service-type" "compute")
      (.put "path" "/images")))

  (defn create-json-endpoints []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")))


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

  (-serviceCall (create-json-service-call))

  )
