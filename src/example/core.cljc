(ns example.core
  (:require
   #?(:bb [bbb.core :refer [climatic-hax]] ;; <-- really this should be a no-op, but not sure how to 
                                           ;; do that with reader conditionals
      :clj [cli-matic.core :refer [run-cmd]]))
  (:gen-class))

#?(:bb (climatic-hax)
   :clj (comment "climatic already required in jvm clojure, do nothing"))

(defn -main
  [& args]

  (run-cmd args
           {:command     "example"
            :description "An example CLI with climatic"
            :version     "0.0.1"
            :opts        [{:as "example" :option "example" :short "e"
                           :type :string}]
            :runs (fn [& args]
                    (println "I weas called as an example with args: " args))}))

