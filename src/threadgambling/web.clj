(ns threadgambling.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [threadgambling.models.migration :as schema]
            [clojure.java.jdbc :as sql]
            [threadgambling.db :as db]
            [cljs.build.api :as cljs-build]))

(defn fetch-fixtures []
  (-> (client/get "http://api.football-data.org/v1/competitions/426/fixtures"
                  {:query-params {"matchday" "28"}
                   :headers {"X-Response-Control" "minified"
                             "X-Auth-Token" (env :auth-token)}})
      :body))



(defmacro with-db-error-printing )

(defn save-fixtures! [resp-body gameweek]
  (try (sql/insert! db/db :fixtures [:body :gameweek] [resp-body gameweek])
       (catch Exception e
         (-> e
             .getNextException
             .printStackTrace))))



#_(defn get-current-fixtures [])

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
  (schema/migrate)
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))
