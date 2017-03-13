(ns threadgambling.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [cljs.build.api :as cljs-build]))

(defn get-fixtures []
  (-> (client/get "http://api.football-data.org/v1/competitions/426/fixtures"
                  {:query-params {"matchday" "28"}
                   :headers {"X-Response-Control" "minified"
                             "X-Auth-Token" (env :auth-token)}})
      :body))

(defroutes app
  (GET "/" []
       (slurp (io/resource "public/index.html")))
  (GET "/fixtures" []
       (get-fixtures))
  (GET "/admin" []
       (slurp (io/resource "public/admin.html")))
  (route/resources "/")
  (ANY "*" []
       (route/not-found "<h1>404 Not found</h1>")))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))
