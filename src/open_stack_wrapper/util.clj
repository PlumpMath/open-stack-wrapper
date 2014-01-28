(ns open-stack-wrapper.util
  (:use [clojure.pprint])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )


(def example {:key1 1
              :key2 {:key3 "hola" :key4 {}}
              :key5 [5 6 [7 [8] {:key6 [{:key7 17}]}]]
              })

(defn keywordize [v]
  (let [s (.getSimpleName (type v))
        t (keyword (clojure.string/lower-case s))
        ]

         (condp = t
           :long "N"
           :string "S"
           :persistentarraymap "{}"
           t)
    ))

(defn pprint-json-scheme [the-json-data]
  (let [result  ((fn rr [ex]
          (if (map? ex)
            (into {} (vec (map (fn [[k v]]  (if (map?  v)
                                             (if (empty? v)
                                               [k (keywordize {})]
                                               [k   (into {} (rr v))]
                                               )
                                             (if (vector? v)
                                               (if (empty? v)
                                                 [k []]
                                                 [k  [(rr (first v))]]
                                                 )

                                               [k  (keywordize v)]
                                               ))) ex)))
            (if (vector? ex)
              [(rr (first ex))]
              (keywordize ex)
              )


            ) ) the-json-data)
        r (into {} result)
        ]
    (pprint r)
    r
    )

    )
