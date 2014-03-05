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
               #^{:static true} [operation [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [serviceCall [org.json.JSONObject] org.json.JSONObject]

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
  (service-call eps-token-id url path)
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

(defn -serviceCall [json-java-object]
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

  (def tokens-response
    {:success true, :access {:token {:issued_at "2014-03-05T09:04:07.399444", :expires "2014-03-06T09:04:07Z", :id "MIIDCwYJKoZIhvcNAQcCoIIC-DCCAvgCAQExCTAHBgUrDgMCGjCCAWEGCSqGSIb3DQEHAaCCAVIEggFOeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMy0wNVQwOTowNDowNy4zOTk0NDQiLCAiZXhwaXJlcyI6ICIyMDE0LTAzLTA2VDA5OjA0OjA3WiIsICJpZCI6ICJwbGFjZWhvbGRlciJ9LCAic2VydmljZUNhdGFsb2ciOiBbXSwgInVzZXIiOiB7InVzZXJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCIsICJyb2xlc19saW5rcyI6IFtdLCAiaWQiOiAiMmVhZWRhYzBkY2MwNDM1Y2JmZWM2OWRjYmQzMzkxYjQiLCAicm9sZXMiOiBbXSwgIm5hbWUiOiAiZmFjZWJvb2sxNDI4NDY3ODUwIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgMBVVuc2V0MQ4wDAYDVQQHDAVVbnNldDEOMAwGA1UECgwFVW5zZXQxGDAWBgNVBAMMD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQAgVA2iEfzJ3KRgggG-8b-gmJQ1CA+xiD7v3TvVzwv9sRUEFutdWeMZCObrwGoSQVR-VnmuvOSCs5BFPYYqx19fid46lkaEY6TOeNn5zxxnEbaE-3Gs4WXtKEme1zbdrbVCcdeVNkcdsJyZgsCnLiF4bUexG1xI-gi6l+pz9jYKTfs0Y5nsoNTpcinobigl7lJagpZstt4r3WFnZbxtA3D+H6HTzQhkZqGbIpYWyPx2kDZHkkmWv8jCvEfJBZt1SFJ0TXghaBM1VGJSpj3FNCmV-C9qHPireupVmI0NQ88QcnRlNq2CdE+1t1E6WdtUDRW2EkrPllXsvC3fjg0uYuvj"}, :serviceCatalog [], :user {:username "facebook1428467850", :roles_links [], :id "2eaedac0dcc0435cbfec69dcbd3391b4", :roles [], :name "facebook1428467850"}, :metadata {:is_admin 0, :roles []}}})

  (def token-id (get-in tokens-response [:access :token :id]))

  (get-tenants (assoc (select-keys login-properties [:url]) :token-id token-id))


  (def tenants-response {:success true, :tenants_links [], :tenants [{:description "Auto created account", :enabled true, :id "da05a30dff7746b9a20027a68cfe6076", :name "facebook1428467850"}]})
  (def tenant-name (-> (:tenants tenants-response ) first  :name))

  (comment "rest call"
           (get-endpoints (assoc (select-keys login-properties [:url :password :username]) :tenant-name tenant-name)))

  (def endpoints-response  *1)

  (util/pprint-json-scheme endpoints-response)

  (def endpoints-structured (structured-endpoints endpoints-response))

  (util/pprint-json-scheme endpoints-structured)

  (def new-token-id (get-in endpoints-response [:access :token :id]))

  (service-call new-token-id (get-in  endpoints-structured  [:compute :publicURL] ) "/images")




  (def service-call-response *1)




   ((fn [{:keys [url username password tenant-name service-type path] :as m}]
             (operation  url username password tenant-name service-type path)
             ) (assoc login-properties :tenant-name tenant-name :service-type "compute" :path  "/images")
               )

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

  )
