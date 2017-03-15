(defproject threadgambling "0.1.0-SNAPSHOT"
  :description ""
  :url "https://threadgambling.herokuapp.com"
  :license  {:name "CC BY-NC-SA 4.0"
             :url "https://creativecommons.org/licenses/by-nc-sa/4.0/"}
  :clean-targets ^{:protect false} [:target-path "out" "resources/public/js"]
  :repl-options {:init-ns dev.repl}
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.3.441"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [cljs-react-material-ui "0.2.38" :exclusions [cljsjs/material-ui]]
                 [cljsjs/material-ui "0.17.0-0" :exclusions [cljsjs/react]]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader cljsjs/react cljsjs/react-dom]]
                 [cljs-react-test "0.1.4-SNAPSHOT" :exclusions [cljsjs/react-with-addons]]
                 [prismatic/dommy "1.1.0"]
                 [secretary "1.2.3"]
                 [clj-http "2.3.0"]
                 [cljs-http "0.1.42"]
                 [com.layerware/hugsql "0.4.7"]
                 [org.clojure/java.jdbc "0.7.0-alpha2"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.clojure/data.json "0.2.6"]
                 [compojure "1.5.1"]
                 [lein-doo "0.1.7"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [environ "1.0.0"]]
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-doo "0.1.7"]
            [lein-cljsbuild "1.1.4"]]
  :hooks [environ.leiningen.hooks]
  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 3450}
  :uberjar-name "threadgambling.jar"
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.7"]
                                  [binaryage/devtools "0.8.1"]]
                   :source-paths ["src" "dev"]
                   :cljsbuild {:builds [{:id "dev"
                                         :source-paths ["src"]
                                         :figwheel true
                                         :compiler {:main "threadgambling.core"
                                                    :preloads [devtools.preload]
                                                    :asset-path "js/out"
                                                    :output-to "resources/public/js/main.js"
                                                    :output-dir "resources/public/js/out"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true}}
                                        {:id "admin"
                                         :source-paths ["src"]
                                         :figwheel true
                                         :compiler {:main "threadgambling.admin"
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
                                                    :main "threadgambling.runner"
                                                    :optimizations :simple}}]}}
             :uberjar {:env {:production true}
                       :source-paths ["src"]
                       :prep-tasks ["compile" ["cljsbuild" "once"]]
                       :cljsbuild {:builds [{:id "production"
                                             :source-paths ["src"]
                                             :jar true
                                             :compiler {:main "threadgambling.core"
                                                        :asset-path "js/out"
                                                        :output-to "resources/public/js/main.js"
                                                        :output-dir "resources/public/js/out"
                                                        :optimizations :advanced
                                                        :pretty-print false}}
                                            {:id "admin"
                                             :source-paths ["src"]
                                             :jar true
                                             :compiler {:main "threadgambling.admin"
                                                        :preloads [devtools.preload]
                                                        :asset-path "js/admin/out"
                                                        :output-to "resources/public/js/admin.js"
                                                        :output-dir "resources/public/js/admin/out"
                                                        :optimizations :advanced
                                                        :pretty-print false}}]}}})
