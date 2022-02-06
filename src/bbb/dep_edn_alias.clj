(ns bbb.dep-edn-alias
  (:require [clj.native-image]))

(defn tools-deps-entrypoint [{:keys [main-ns]}]
  (clj.native-image/-main main-ns
                          "--initialize-at-build-time"
                          "--allow-incomplete-classpath"))
