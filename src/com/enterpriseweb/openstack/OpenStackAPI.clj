(ns com.enterpriseweb.openstack.OpenStackAPI
  (:use [open-stack-wrapper.core :as os-core])
  (:require [clojure.data.json :as clj-json][clojure.java.io :as io])
  (:import [org.json JSONObject])
  (:gen-class :methods
              [#^{:static true} [json [org.json.JSONObject] org.json.JSONObject]
               #^{:static true} [tokens [org.json.JSONObject] org.json.JSONObject]])
  )


(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-json-java-object [clojure-json-object]
  (JSONObject. (str clojure-json-object)))

(defn java-json->clojure-json  [java-json-object]
  (clj-json/read-str (.toString java-json-object) :key-fn keyword))

(comment
  (def login-properties (load-config "./login.properties")))


(defn -json [json-java-object]
  (println json-java-object)
  (create-json-java-object (clj-json/write-str {:response {:hola "que tal?"}}))
  )

(defn get-tokens [{:keys [url username password ]}]
  (os-core/tokens url username password)
  )

(defn -tokens [json-java-object]
  (create-json-java-object (clj-json/write-str (get-tokens (java-json->clojure-json json-java-object))))
  )



(comment
  get-tokens => {:success true, :access {:token {:issued_at "2014-03-04T08:59:45.144719", :expires "2014-03-05T08:59:45Z", :id "MIIDCwYJKoZIhvcNAQcCoIIC-DCCAvgCAQExCTAHBgUrDgMCGjCCAWEGCSqGSIb3DQEHAaCCAVIEggFOeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMy0wNFQwODo1OTo0NS4xNDQ3MTkiLCAiZXhwaXJlcyI6ICIyMDE0LTAzLTA1VDA4OjU5OjQ1WiIsICJpZCI6ICJwbGFjZWhvbGRlciJ9LCAic2VydmljZUNhdGFsb2ciOiBbXSwgInVzZXIiOiB7InVzZXJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCIsICJyb2xlc19saW5rcyI6IFtdLCAiaWQiOiAiMmVhZWRhYzBkY2MwNDM1Y2JmZWM2OWRjYmQzMzkxYjQiLCAicm9sZXMiOiBbXSwgIm5hbWUiOiAiZmFjZWJvb2sxNDI4NDY3ODUwIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgMBVVuc2V0MQ4wDAYDVQQHDAVVbnNldDEOMAwGA1UECgwFVW5zZXQxGDAWBgNVBAMMD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQDTpT3JRA9UM74uh0ibNgE+BXeVi+KRTUYJ9iHlRf7Ud6EOrOwN1ae58bi8JKBebaGYkzLOlQVeDYI2DnkE1KMtcNClTLB51NazLKcSK1DIwgEJFXMKndPMR3oSdhfA6GyM6sTUvvBQOUymuP4zDLcvu3iUVZcoXqwauJK2ZpTGhlNa55vXIXb3A+MMJiaL4ZM4NJd9+HbWFlOyq+HNdLh5w9CHK6VbgvAkkmr6pw1bPsji8ROp4OnGQGykNYzQTLJEouiluz5EgBgXpB9LLlKO3GVPdmsctQNlXCV8cVe70X8j3rDn8eW4x6D-0WnPv1SGU61oQj8ExnpA15mK84w6"}, :serviceCatalog [], :user {:username "facebook1428467850", :roles_links [], :id "2eaedac0dcc0435cbfec69dcbd3391b4", :roles [], :name "facebook1428467850"}, :metadata {:is_admin 0, :roles []}}})
