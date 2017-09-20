(defproject threadstreaks "1.0.0-SNAPSHOT"
  :description ""
  :url "https://threadstreaks.herokuapp.com"
  :license  {:name "CC BY-NC-SA 4.0"
             :url "https://creativecommons.org/licenses/by-nc-sa/4.0/"}
  :clean-targets ^{:protect false} [:target-path "out" "resources/public/js"]
  :repl-options {:init-ns dev.repl}
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :min-lein-version "2.5.3"
  :main threadstreaks.web
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.521"]
                 [org.clojure/core.async "0.3.442"]
                 [cljs-react-material-ui "0.2.38" :exclusions [cljsjs/material-ui]]
                 [cljsjs/material-ui "0.17.0-0" :exclusions [cljsjs/react]]
                 [reagent-utils "0.2.1"]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader cljsjs/react cljsjs/react-dom]]
                 [cljs-react-test "0.1.4-SNAPSHOT" :exclusions [cljsjs/react-with-addons]]
                 [prismatic/dommy "1.1.0"]
                 [secretary "1.2.3"]
                 [clj-http "2.3.0"]
                 [cljs-http "0.1.42"]
                 [luminus-migrations "0.3.0"]
                 [com.layerware/hugsql "0.4.7"]
                 [org.clojure/java.jdbc "0.7.0-alpha2"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.clojure/data.json "0.2.6"]
                 [stubadub "2.0.0"]
                 [compojure "1.5.1"]
                 [lein-doo "0.1.7"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-mock "0.3.0"]
                 [conman "0.6.3"]
                 [mount "0.1.11"]
                 [environ "1.0.0"]
                 [clj-time "0.13.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [log4j/log4j "1.2.17"]]
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-environ "1.1.0"]
            [lein-doo "0.1.7"]
            [migratus-lein "0.4.4"]
            [lein-cljsbuild "1.1.4"]]
  :hooks [environ.leiningen.hooks]
  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 3450}
  :uberjar-name "threadstreaks.jar"
  :profiles {:project/dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                          [figwheel-sidecar "0.5.9"]
                                          [pjstadig/humane-test-output "0.8.1"]
                                          [binaryage/devtools "0.9.4"]]
                           :plugins [[com.jakemccrary/lein-test-refresh "0.19.0"]]
                           :injections [(require 'pjstadig.humane-test-output)
                                        (pjstadig.humane-test-output/activate!)]
                           :source-paths ["src" "dev"]
                           :cljsbuild {:builds [{:id "dev"
                                                 :source-paths ["src"]
                                                 :figwheel true
                                                 :compiler {:main "threadstreaks.core"
                                                            :preloads [devtools.preload]
                                                            :asset-path "js/out"
                                                            :output-to "resources/public/js/main.js"
                                                            :output-dir "resources/public/js/out"
                                                            :optimizations :none
                                                            :externs ["resources/public/js/out/inferred-externs.js"]
                                                            :foreign-libs
                                                            [{:file "gapi/platform.js"
                                                              :provides ["com.google.api"]}]
                                                            :infer-externs true
                                                            :recompile-dependents true
                                                            :source-map true}}
                                                {:id "admin"
                                                 :source-paths ["src"]
                                                 :figwheel true
                                                 :compiler {:main "threadstreaks.admin"
                                                            :preloads [devtools.preload]
                                                            :asset-path "js/admin/out"
                                                            :output-to "resources/public/js/admin.js"
                                                            :output-dir "resources/public/js/admin/out"
                                                            :optimizations :none
                                                            :recompile-dependents true
                                                            :source-map true}}
                                                {:id "test"
                                                 :source-paths ["src" "test"]
                                                 :compiler {:output-to "resources/public/js/test.js"
                                                            :asset-path "js/test/out"
                                                            :output-dir "resources/public/js/test/out"
                                                            :main "threadstreaks.runner"
                                                            :optimizations :simple}}]}}
             :profiles/test {}
             :proflies/dev {}
             :dev [:project/dev :profiles/dev]
             :test [:project/dev :profiles/test]
             :uberjar {:env {:production true}
                       :source-paths ["src"]
                       :prep-tasks ["compile" ["cljsbuild" "once"]]
                       :cljsbuild {:builds [{:id "production"
                                             :source-paths ["src"]
                                             :jar true
                                             :compiler {:main "threadstreaks.core"
                                                        :asset-path "js/out"
                                                        :output-to "resources/public/js/main.js"
                                                        :output-dir "resources/public/js/out"
                                                        :infer-externs true
                                                        :externs ["resources/public/js/out/inferred-externs.js"]
                                                        :foreign-libs
                                                        [{:file "gapi/platform.js"
                                                          :provides ["com.google.api"]}]
                                                        :optimizations :advanced
                                                        :pretty-print false}}
                                            {:id "admin"
                                             :source-paths ["src"]
                                             :jar true
                                             :compiler {:main "threadstreaks.admin"
                                                        :asset-path "js/admin/out"
                                                        :output-to "resources/public/js/admin.js"
                                                        :output-dir "resources/public/js/admin/out"
                                                        :infer-externs true
                                                        :externs ["resources/public/js/out/inferred-externs.js"]
                                                        :foreign-libs
                                                        [{:file "gapi/platform.js"
                                                          :provides ["com.google.api"]}]
                                                        :optimizations :advanced
                                                        :pretty-print false}}]}}})
