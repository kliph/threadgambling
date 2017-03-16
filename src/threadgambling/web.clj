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
            [luminus-migrations.core :as migrations]
            [clojure.java.jdbc :as sql]
            [threadgambling.db :as db]
            [cljs.build.api :as cljs-build]))

(defn fetch-fixtures []
  (-> (client/get "http://api.football-data.org/v1/competitions/426/fixtures"
                  {:query-params {"matchday" "28"}
                   :headers {"X-Response-Control" "minified"
                             "X-Auth-Token" (env :auth-token)}})
      :body))

(defmacro with-db-error-printing [body]
  `(try
     ~body
     (catch Exception e#
       (-> e#
           .getNextException
           .printStackTrace))))

(defn save-fixtures! [resp-body gameweek]
  (with-db-error-printing
    (sql/insert! db/db :fixtures [:body :gameweek] [resp-body gameweek])))

#_(defn get-fixtures [gameweek]
  (with-db-error-printing
    (sql/select db/db :fixtures)))

(defroutes app
  (GET "/" []
       (slurp (io/resource "public/index.html")))
  (GET "/fixtures" []
       (fetch-fixtures))
  (GET "/admin" []
       (slurp (io/resource "public/admin.html")))
  (route/resources "/")
  (ANY "*" []
       (route/not-found "<h1>404 Not found</h1>")))

(def handler (site #'app))

(defn -main [& args]
  (cond
    (some #{"migrate" "rollback"} args)
    (do
      (migrations/migrate args {:database-url (:database-url env)}))
    :else
    (let [port (Integer. (or (env :port) 5000))]
      (jetty/run-jetty handler {:port port :join? false}))))
