(defproject auburn-reports "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [itext "5.0.5"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot auburn-reports.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
