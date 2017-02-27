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
                 [cljs-react-material-ui "0.2.21"]
                 [reagent "0.6.0" :exclusions [org.clojure/tools.reader cljsjs/react]]
                 [compojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [environ "1.0.0"]]
  :plugins [[environ/environ.lein "0.3.1"]
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
                                                    :source-map true}}]}}
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
                                                        :pretty-print false}}]}}})
