(ns threadgambling.web
  (:require [compojure.core :refer [defroutes GET ANY POST]]
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
            [cljs.build.api :as cljs-build])
  (:gen-class))

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

(defn fetch-token-info [client-id-token]
  (client/get "https://www.googleapis.com/oauth2/v3/tokeninfo"
              {:query-params {"id_token" client-id-token}}))

(defn aud-contains-client-id? [token-info client-id]
  (let [aud-claim (get token-info "aud" "")]
    (clojure.string/includes? aud-claim client-id)))

(defn get-user-from-token-info [token-info]
  (let [sub (get token-info "sub" "")
        user (db/get-user {:id sub})]
    user))

(defn create-user-from-token-info! [token-info]
  (let [user-to-be-created {:id (get token-info "sub")
                            :name (get token-info "name" "")
                            :email (get token-info "email" "")
                            :team ""}
        _ (db/create-user! user-to-be-created)
        user (db/get-user {:id (:id user-to-be-created)})]
    {:status 200
     :headers {}
     :body {:user user}}))

(defn log-in-or-create-user! [token-info]
  (if-let [user (get-user-from-token-info token-info)]
    {:status 200
     :headers {}
     :body {:user user}}
    (create-user-from-token-info! token-info)))

(defn verify-token-info [token-info-response]
  (let [token-info (:body token-info-response)]
    (if (aud-contains-client-id? token-info (env :google-oauth2-client-id))
      (log-in-or-create-user! token-info)
      {:status 403
       :headers {}
       :body "Forbidden"})))

(defn handle-token-signin! [client-id-token]
  (let [client-id (env :google-oauth2-client-id)
        _ (println client-id)
        _ (println client-id-token)]
    (-> client-id-token
        fetch-token-info
        verify-token-info)))

(defroutes app
  (GET "/" []
       (slurp (io/resource "public/index.html")))
  (GET "/fixtures" []
       (fetch-fixtures!))
  (POST "/tokensignin" [idtoken]
        (handle-token-signin! idtoken))
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
