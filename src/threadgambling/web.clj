(ns threadgambling.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [threadgambling.db.core :refer [*db*] :as db]
            [mount.core :as mount]
            [luminus-migrations.core :as migrations]
            [cljs.build.api :as cljs-build]))

(defn fixtures-updated? [response-body record]
  (let [parse-fn #(-> %
                      (json/read-str :key-fn keyword)
                      :fixtures)
        parsed-response-fixtures (parse-fn response-body)
        parsed-record-fixtures (parse-fn (get record :body "{}"))]
    (not= parsed-response-fixtures
          parsed-record-fixtures)))


(defn save-fixtures! [body]
  (when (fixtures-updated? body
                           (-> (db/get-fixtures-by-gameweek {:gameweek 28})
                               (select-keys [:body])))
    (db/save-fixtures! {:gameweek 28
                        :body body}))
  body)

(defn fetch-fixtures! []
  (let [body (-> (client/get "http://api.football-data.org/v1/competitions/426/fixtures"
                             {:query-params {"matchday" "28"}
                              :headers {"X-Response-Control" "minified"
                                        "X-Auth-Token" (env :auth-token)}})
                 :body
                 save-fixtures!)]
    {:status 200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body body}))

(defroutes app
  (GET "/" []
       (slurp (io/resource "public/index.html")))
  (GET "/fixtures" []
       (fetch-fixtures!))
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
      (mount/start #'threadgambling.db.core/*db*)
      (jetty/run-jetty handler {:port port :join? false}))))
