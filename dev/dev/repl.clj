(ns dev.repl
  (:require [figwheel-sidecar.repl-api :as repl-api]
            [mount.core :as mount]
            threadstreaks.web))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn restart []
  (stop)
  (start))

(defn figwheel-up []
  (do (repl-api/start-figwheel!)
      (repl-api/cljs-repl)))
