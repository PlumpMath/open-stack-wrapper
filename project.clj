(defproject open-stack-wrapper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.8"]
                 [liberator "0.10.0"]
                 [org.clojure/data.json "0.2.4"]
                 [compojure "1.1.3"]
		 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.1.0"]]
  :uberjar-name "open-stack-wrapper-standalone.jar"
  :min-lein-version "2.0.0"
  )
