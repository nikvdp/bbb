(ns bbb.core
  #?(:bb (:require [clojure.core])
     ;; on JVM clojure pull in babashka's GraalVM related patches for increased
     ;; compatibility. see:
     ;; https://twitter.com/borkdude/status/1480464513434537985
     :clj (:require [babashka.impl.pprint]
                    [babashka.impl.httpkit-client]))
  (:gen-class))

(defmacro climatic-hax
  []
  #?(:bb
     #_(comment "Loading climatic inside bb requires shifting namespaces on the fly a couple times, 
                so automate that with this macro")
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
                (require '[cli-matic.core  :as climatic-ns]))
              climatic-ns/run-cmd)))

     :clj
     '(do
        #_(comment  "In JVM Clojure we can just do a normal require")
        (require '[cli-matic.core :as climatic-ns])
        climatic-ns/run-cmd)))

;; this must stay at the top of the file since it is doing requires under the hood 
(climatic-hax)

;; Since cli-matic only has one entry point (see [1]), we re-export that from bbb.core 
;; after macro hackery has been finished
;;
;; [1]: https://github.com/l3nz/cli-matic/blob/master/src/cli_matic/core.cljc#L3-L10
(def run-cmd climatic-ns/run-cmd)
