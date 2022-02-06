(ns example.core
  (:require
   [bbb.core :refer [run-cmd]])
  (:gen-class #_"👈 this is *required* for native-image compilation under GraalVM"))

(defn -main
  [& args]
  (run-cmd args
           {:command     "example"
            :description "An example CLI with climatic"
            :version     "0.0.1"
            :opts        [{:as "example" :option "example" :short "e"
                           :type :string}]
            :runs (fn [& args]
                    (println "I was called as an example with args: ")
                    (println args))}))

