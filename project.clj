(defproject com.enterpriseweb/open-stack-wrapper "0.1.12"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.8"]
                 [slingshot "0.10.3"]
                 [org.json/json "20131018"]
                 [org.clojure/data.json "0.2.4"]
                 [com.enterpriseweb/clj-java-json-utils "0.1.0"]]

  :scm {:name "git"
        :url "https://github.com/tangrammer/open-stack-wrapper"}


  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}


  )
