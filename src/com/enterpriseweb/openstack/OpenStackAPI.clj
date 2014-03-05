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

(defn get-operation [{:keys [url username password tenant-name service-type path]}]
  (os-core/operation url username password tenant-name service-type path)
  )

(defn get-endpoints [{:keys [url username password tenant-name]}]
  (os-core/endpoints url username password tenant-name)
  )

(defn get-endpoints-structured [{:keys [url username password tenant-name] :as m}]
  (os-core/structured-endpoints (get-endpoints m))
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


(defn -operation [json-java-object]
  (create-json-java-object (clj-json/write-str (get-operation (java-json->clojure-json json-java-object))))
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

  (def endpoints-response #_(get-endpoints (assoc (select-keys login-properties [:url :password :username]) :tenant-name tenant-name))
    {:success true, :access {:token {:issued_at "2014-03-05T09:06:34.195517", :expires "2014-03-06T09:06:34Z", :id "MIIOzgYJKoZIhvcNAQcCoIIOvzCCDrsCAQExCTAHBgUrDgMCGjCCDSQGCSqGSIb3DQEHAaCCDRUEgg0ReyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMy0wNVQwOTowNjozNC4xOTU1MTciLCAiZXhwaXJlcyI6ICIyMDE0LTAzLTA2VDA5OjA2OjM0WiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogIkF1dG8gY3JlYXRlZCBhY2NvdW50IiwgImVuYWJsZWQiOiB0cnVlLCAiaWQiOiAiZGEwNWEzMGRmZjc3NDZiOWEyMDAyN2E2OGNmZTYwNzYiLCAibmFtZSI6ICJmYWNlYm9vazE0Mjg0Njc4NTAifX0sICJzZXJ2aWNlQ2F0YWxvZyI6IFt7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xMC4xMDAuMC4yMjI6ODc3NC92Mi9kYTA1YTMwZGZmNzc0NmI5YTIwMDI3YTY4Y2ZlNjA3NiIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xMC4xMDAuMC4yMjI6ODc3NC92Mi9kYTA1YTMwZGZmNzc0NmI5YTIwMDI3YTY4Y2ZlNjA3NiIsICJpZCI6ICI4NGRiZjBkNWVkOWE0OTg0OGVmNjdlNDlmNjMxNjE3ZiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzguMjEuMjguMjIyOjg3NzQvdjIvZGEwNWEzMGRmZjc3NDZiOWEyMDAyN2E2OGNmZTYwNzYifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiY29tcHV0ZSIsICJuYW1lIjogIm5vdmEifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMTAwLjAuNDo5Njk2LyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xMC4xMDAuMC40Ojk2OTYvIiwgImlkIjogIjFiNDI3Zjg0Y2VlMDQzMjE5NWQ3MTAxYjI1OTUxYTU2IiwgInB1YmxpY1VSTCI6ICJodHRwOi8vOC4yMS4yOC40Ojk2OTYvIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogIm5ldHdvcmsiLCAibmFtZSI6ICJuZXV0cm9uIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo4MDgwIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo4MDgwIiwgImlkIjogIjA1ODdhYmQxNWNhYTQ1MDI5MGNhZThiYjExZjU0M2JiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vOC4yMS4yOC4yMjI6ODA4MCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJzMyIsICJuYW1lIjogInN3aWZ0X3MzIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo5MjkyIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo5MjkyIiwgImlkIjogIjA5NmEyMGU3MmE4MjRjNjZiOTU5NWYyMjY0YTI5MDFiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vOC4yMS4yOC4yMjI6OTI5MiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJpbWFnZSIsICJuYW1lIjogImdsYW5jZSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xMC4xMDAuMC4yMjI6ODc3NyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xMC4xMDAuMC4yMjI6ODc3NyIsICJpZCI6ICIwMmI4ODE2YThmZGQ0M2UxYWRlYzdhMmY4NTAwZjAyZiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzguMjEuMjguMjIyOjg3NzcifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAibWV0ZXJpbmciLCAibmFtZSI6ICJjZWlsb21ldGVyIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo4Nzc2L3YxL2RhMDVhMzBkZmY3NzQ2YjlhMjAwMjdhNjhjZmU2MDc2IiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo4Nzc2L3YxL2RhMDVhMzBkZmY3NzQ2YjlhMjAwMjdhNjhjZmU2MDc2IiwgImlkIjogIjJjZjZhNDM5OTMyMTRjODNhNTA1NjBjYzkwMDkyYTQ1IiwgInB1YmxpY1VSTCI6ICJodHRwOi8vOC4yMS4yOC4yMjI6ODc3Ni92MS9kYTA1YTMwZGZmNzc0NmI5YTIwMDI3YTY4Y2ZlNjA3NiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJ2b2x1bWUiLCAibmFtZSI6ICJjaW5kZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMTAwLjAuMjIyOjg3NzMvc2VydmljZXMvQWRtaW4iLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTAuMTAwLjAuMjIyOjg3NzMvc2VydmljZXMvQ2xvdWQiLCAiaWQiOiAiMGFhMjJhZTAxNGRlNDc0ZmFkOTE0ZTMzNDdiYTc2ZWEiLCAicHVibGljVVJMIjogImh0dHA6Ly84LjIxLjI4LjIyMjo4NzczL3NlcnZpY2VzL0Nsb3VkIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImVjMiIsICJuYW1lIjogIm5vdmFfZWMyIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo4MDgwLyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xMC4xMDAuMC4yMjI6ODA4MC92MS9BVVRIX2RhMDVhMzBkZmY3NzQ2YjlhMjAwMjdhNjhjZmU2MDc2IiwgImlkIjogIjFiNmMwMDNmMWExNjRmMzJhNTUxZmUwNzVhYTUzN2JmIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vOC4yMS4yOC4yMjI6ODA4MC92MS9BVVRIX2RhMDVhMzBkZmY3NzQ2YjlhMjAwMjdhNjhjZmU2MDc2In1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogIm9iamVjdC1zdG9yZSIsICJuYW1lIjogInN3aWZ0In0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjozNTM1Ny92Mi4wIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjEwMC4wLjIyMjo1MDAwL3YyLjAiLCAiaWQiOiAiMDhlMTg0ZTQyZTc2NDU0Y2FlNWYzYWZlODk4NGRiMWQiLCAicHVibGljVVJMIjogImh0dHA6Ly84LjIxLjI4LjIyMjo1MDAwL3YyLjAifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaWRlbnRpdHkiLCAibmFtZSI6ICJrZXlzdG9uZSJ9XSwgInVzZXIiOiB7InVzZXJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCIsICJyb2xlc19saW5rcyI6IFtdLCAiaWQiOiAiMmVhZWRhYzBkY2MwNDM1Y2JmZWM2OWRjYmQzMzkxYjQiLCAicm9sZXMiOiBbeyJuYW1lIjogIl9tZW1iZXJfIn0sIHsibmFtZSI6ICJNZW1iZXIifV0sICJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCJ9LCAibWV0YWRhdGEiOiB7ImlzX2FkbWluIjogMCwgInJvbGVzIjogWyI5ZmUyZmY5ZWU0Mzg0YjE4OTRhOTA4NzhkM2U5MmJhYiIsICI1OWVhYjYwMzgyOTA0MmFkYjlhZjkzZDNmZDM1MGUxYyJdfX19MYIBgTCCAX0CAQEwXDBXMQswCQYDVQQGEwJVUzEOMAwGA1UECAwFVW5zZXQxDjAMBgNVBAcMBVVuc2V0MQ4wDAYDVQQKDAVVbnNldDEYMBYGA1UEAwwPd3d3LmV4YW1wbGUuY29tAgEBMAcGBSsOAwIaMA0GCSqGSIb3DQEBAQUABIIBACzZvuCeoJt4Q2Y2nPtjtGzABNHpkzuuif2ZR9cddsxl48FgFlkMPSxjUBtFDpwviDZbkLPeufDbdNzk+YejR7THFW-9etVIw+KIjh7JbIhb4vpv7jcCuviSFcjJ7lgVdviQhimNA7Em1FO81kGiPlWG+mxE2v+NioEAT4EgU7RhdV-MvFnTuwJsaebRBkiH6cr5mINQ4jd5Q6nTNrtLtmBlYEQDWAe19-m-jRn0eqX3WK96rdrg06UCb+3qp+s1s2mt2jEiPviKCRkFFPcgfCay3wBD7f854Udh7XtqIXymMZPGdk+8jR6zbdOjKLPmZYXjQqH9ImFPYafT9u5beFA=", :tenant {:description "Auto created account", :enabled true, :id "da05a30dff7746b9a20027a68cfe6076", :name "facebook1428467850"}}, :serviceCatalog [{:endpoints [{:adminURL "http://10.100.0.222:8774/v2/da05a30dff7746b9a20027a68cfe6076", :region "RegionOne", :internalURL "http://10.100.0.222:8774/v2/da05a30dff7746b9a20027a68cfe6076", :id "84dbf0d5ed9a49848ef67e49f631617f", :publicURL "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076"}], :endpoints_links [], :type "compute", :name "nova"} {:endpoints [{:adminURL "http://10.100.0.4:9696/", :region "RegionOne", :internalURL "http://10.100.0.4:9696/", :id "1b427f84cee0432195d7101b25951a56", :publicURL "http://8.21.28.4:9696/"}], :endpoints_links [], :type "network", :name "neutron"} {:endpoints [{:adminURL "http://10.100.0.222:8080", :region "RegionOne", :internalURL "http://10.100.0.222:8080", :id "0587abd15caa450290cae8bb11f543bb", :publicURL "http://8.21.28.222:8080"}], :endpoints_links [], :type "s3", :name "swift_s3"} {:endpoints [{:adminURL "http://10.100.0.222:9292", :region "RegionOne", :internalURL "http://10.100.0.222:9292", :id "096a20e72a824c66b9595f2264a2901b", :publicURL "http://8.21.28.222:9292"}], :endpoints_links [], :type "image", :name "glance"} {:endpoints [{:adminURL "http://10.100.0.222:8777", :region "RegionOne", :internalURL "http://10.100.0.222:8777", :id "02b8816a8fdd43e1adec7a2f8500f02f", :publicURL "http://8.21.28.222:8777"}], :endpoints_links [], :type "metering", :name "ceilometer"} {:endpoints [{:adminURL "http://10.100.0.222:8776/v1/da05a30dff7746b9a20027a68cfe6076", :region "RegionOne", :internalURL "http://10.100.0.222:8776/v1/da05a30dff7746b9a20027a68cfe6076", :id "2cf6a43993214c83a50560cc90092a45", :publicURL "http://8.21.28.222:8776/v1/da05a30dff7746b9a20027a68cfe6076"}], :endpoints_links [], :type "volume", :name "cinder"} {:endpoints [{:adminURL "http://10.100.0.222:8773/services/Admin", :region "RegionOne", :internalURL "http://10.100.0.222:8773/services/Cloud", :id "0aa22ae014de474fad914e3347ba76ea", :publicURL "http://8.21.28.222:8773/services/Cloud"}], :endpoints_links [], :type "ec2", :name "nova_ec2"} {:endpoints [{:adminURL "http://10.100.0.222:8080/", :region "RegionOne", :internalURL "http://10.100.0.222:8080/v1/AUTH_da05a30dff7746b9a20027a68cfe6076", :id "1b6c003f1a164f32a551fe075aa537bf", :publicURL "http://8.21.28.222:8080/v1/AUTH_da05a30dff7746b9a20027a68cfe6076"}], :endpoints_links [], :type "object-store", :name "swift"} {:endpoints [{:adminURL "http://10.100.0.222:35357/v2.0", :region "RegionOne", :internalURL "http://10.100.0.222:5000/v2.0", :id "08e184e42e76454cae5f3afe8984db1d", :publicURL "http://8.21.28.222:5000/v2.0"}], :endpoints_links [], :type "identity", :name "keystone"}], :user {:username "facebook1428467850", :roles_links [], :id "2eaedac0dcc0435cbfec69dcbd3391b4", :roles [{:name "_member_"} {:name "Member"}], :name "facebook1428467850"}, :metadata {:is_admin 0, :roles ["9fe2ff9ee4384b1894a90878d3e92bab" "59eab603829042adb9af93d3fd350e1c"]}}})

  (util/pprint-json-scheme endpoints-response)

  (def endpoints-structured (structured-endpoints endpoints-response))

  (util/pprint-json-scheme endpoints-structured)

  (def new-token-id (get-in endpoints-response [:access :token :id]))

  (def service-call-response
    {:success true, :images [{:id "9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :type "application/vnd.openstack.image", :rel "alternate"}], :name "Fedora 20 x86_64"} {:id "55ebdcd2-d6ce-4c24-8717-888dd01c551d", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :type "application/vnd.openstack.image", :rel "alternate"}], :name "CentOS 6.5 x86_64"} {:id "6667f872-7fce-4da3-b2b8-1490f365b041", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :type "application/vnd.openstack.image", :rel "alternate"}], :name "Ubuntu 13.10 amd64"}]}
    #_(service-call new-token-id (get-in  endpoints-structured  [:compute :publicURL] ) "/images"))




(comment ((fn [{:keys [url username password tenant-name service-type path] :as m}]
      (operation  url username password tenant-name service-type path)
      ) (assoc login-properties :tenant-name tenant-name :service-type :compute :path :images)
        ))

  (get-operation (assoc login-properties :tenant-name tenant-name :service-type :compute :path :images))


  {:success true, :images [{:id "9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/9aab0cc5-3721-464d-b7c7-c7eebbb567f5", :type "application/vnd.openstack.image", :rel "alternate"}], :name "Fedora 20 x86_64"} {:id "55ebdcd2-d6ce-4c24-8717-888dd01c551d", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d", :type "application/vnd.openstack.image", :rel "alternate"}], :name "CentOS 6.5 x86_64"} {:id "6667f872-7fce-4da3-b2b8-1490f365b041", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :rel "bookmark"} {:href "http://8.21.28.3:9292/da05a30dff7746b9a20027a68cfe6076/images/6667f872-7fce-4da3-b2b8-1490f365b041", :type "application/vnd.openstack.image", :rel "alternate"}], :name "Ubuntu 13.10 amd64"}]}

  )
