(ns bbb.core
  #?(:bb (:require [clojure.core]
                   [babashka.process :refer [process]]
                   [clojure.string :as st])
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

;; this must stay near the top of this file since it is doing requires under the hood 
(climatic-hax)

;; Since cli-matic only has one entry point (see [1]), we re-export that from bbb.core 
;; after macro hackery has finished
;;
;; [1]: https://github.com/l3nz/cli-matic/blob/master/src/cli_matic/core.cljc#L3-L10
(def run-cmd climatic-ns/run-cmd)

#?(:bb
   (defn install-graalvm-locally [dest]
     ;; not gonna win any style points with this, but gets the job done for now
     ;; @TODO: do this in a more babashka native way
     (let [install (str "
install-graalvm() {
  local platform=\"$(echo \"$(uname)\" | tr '[:upper:]' '[:lower:]')\"
  local dest='" dest "'

  wget -O \"/tmp/graalvm.tar.gz\" \"https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.1.0/graalvm-ce-java11-$platform-amd64-21.1.0.tar.gz\"

  mkdir -p /tmp/graalvm/out \"$dest\"
  tar -C /tmp/graalvm/out -xvzf /tmp/graalvm.tar.gz
  [[ -d \"$dest\" ]] && mv \"$dest\" \"$dest.bak\"
  mv /tmp/graalvm/out/graalvm-ce-* \"$dest\"

  # install native-image binary
  if [[ -d \"$dest\"/bin ]]; then
    \"$dest\"/bin/gu install native-image
  elif [[ -d \"$dest\"/Contents/Home/bin ]]; then # handle macos folder structure
    \"$dest\"/Contents/Home/bin/gu install native-image
  fi

  # cleanup
  rm /tmp/graalvm.tar.gz
}
install-graalvm
       ")]

       (if (System/getenv "BBB_AUTOGRAAL_NOINTERACTIVE")
         @(process ["bash"] {:in install :inherit true})
         (do
           (print "👉 GraalVM's `native-image` tool not found on PATH, attempt to install a copy to vendor/ folder? (y/n) ")
           (flush)
           (let [resp (read-line)]
             (when (-> resp (st/lower-case) (st/starts-with? "y"))
               @(process ["bash"] {:in install :inherit true}))))))))
