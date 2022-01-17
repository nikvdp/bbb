(ns bbb.core
  (:require
   [babashka.impl.pprint]
   [babashka.impl.httpkit-cient])
  (:gen-class))

(:bb
 (do
   (defmacro climatic-hax []
     (let [orig-ns (ns-name *ns*)]
        ;; hack: bb doesn't have java.text.DateFormat, conversions are done using java.time
        ;; @see https://github.com/borkdude/spartan.spec/blob/master/examples/cli_matic.clj
       `(do
          (ns ~(symbol "java.text"))
          ~'(defrecord SimpleDateFormat [])
          (ns ~orig-ns)

          ~'(do
              (require '[spartan.spec])
              (binding [*err* (java.io.StringWriter.)]
                (require '[cli-matic.core :refer [run-cmd]]))))))

   (climatic-hax)))
