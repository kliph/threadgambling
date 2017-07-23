(ns threadstreaks.web
  (:require [compojure.core :refer [defroutes GET ANY POST]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [threadstreaks.db.core :refer [*db*] :as db]
            [mount.core :as mount]
            [luminus-migrations.core :as migrations]
            [cljs.build.api :as cljs-build])
  (:gen-class))

(defn parse-fixtures [body]
  (-> body
      (json/read-str :key-fn keyword)
      :fixtures))

(defn fixtures-updated? [response-body record]
  (let [parsed-response-fixtures (parse-fixtures response-body)
        parsed-record-fixtures (parse-fixtures (get record :body "{}"))]
    (not= parsed-response-fixtures
          parsed-record-fixtures)))


(defn save-fixtures! [body]
  (when (fixtures-updated?
         body
         (-> (db/get-fixtures-by-gameweek {:gameweek (:gameweek (db/get-gameweek {:id 1}))})
             (select-keys [:body])))
    (db/save-fixtures! {:gameweek (:gameweek (db/get-gameweek {:id 1}))
                        :body body})))

(defn all-finished? [fixtures]
  (and (seq fixtures)
       (every? #(= "FINISHED" %)
               (map :status fixtures))))

(defn create-result-update-gameweek-and-user-fields! [scored-result]
  (let [user (db/get-user {:id (:user_id scored-result)})
        current-user-pick {:pick (:pick scored-result)
                           :id (:id user)}
        current-gameweek (db/get-gameweek {:id 1})
        updated-gameweek {:id (:id current-gameweek)
                          :gameweek (inc (:gameweek current-gameweek))}
        updated-user-points {:id (:id user)
                             :points (+ (:points user)
                                        (:points scored-result))}
        updated-user-current-pick {:id (:id user)
                                   :current_pick nil}
        updated-user-streak {:id (:id user)
                             :current_streak (:current_streak scored-result)}]
    (if (= 0 (:points scored-result))
      (db/update-picks! {:id (:id user)
                         :picks ""})
      (db/add-pick! current-user-pick))
    (db/update-points! updated-user-points)
    (db/update-current-streak! updated-user-streak)
    (db/update-current-pick! updated-user-current-pick)
    (db/update-gameweek! updated-gameweek)
    (db/create-result! scored-result)))

(defn score-finished-week! [body]
  (when (all-finished? (parse-fixtures body))
    (let [gameweek (:gameweek (db/get-gameweek {:id 1}))
          parsed-fixtures (parse-fixtures body)
          date (-> parsed-fixtures
                   first
                   :date
                   f/parse
                   c/to-sql-time)
          goals (-> parsed-fixtures
                    first
                    :result
                    :goalsAwayTeam)
          game (-> parsed-fixtures
                   first)
          winner-keys (map (fn [fixture]
                             (let [home-goals (get-in fixture [:result :goalsHomeTeam])
                                   away-goals (get-in fixture [:result :goalsAwayTeam])
                                   winner-key (cond (> home-goals away-goals) :homeTeamName
                                                    (> away-goals home-goals) :awayTeamName
                                                    :else :draw)]
                               winner-key))
                           parsed-fixtures)
          winners-set  (->> (map (fn [fixture]
                                   (let [home-goals (get-in fixture [:result :goalsHomeTeam])
                                         away-goals (get-in fixture [:result :goalsAwayTeam])
                                         winner-key (cond (> home-goals away-goals) :homeTeamName
                                                          (> away-goals home-goals) :awayTeamName
                                                          :else :draw)
                                         winner (get fixture winner-key)]
                                     winner))
                                 parsed-fixtures)
                            (filter (complement nil?))
                            (into #{}))
          current-picks (->> (db/get-all-current-picks)
                             (map #(-> %
                                       (assoc :date date)
                                       (assoc :gameweek gameweek)
                                       (assoc :user_id (:id %))
                                       (assoc :pick (:current_pick %))
                                       (dissoc :current_pick)
                                       (dissoc :id))))
          previously-scored-set (into #{}
                                      (map :user_id
                                           (db/get-gameweek-results {:gameweek gameweek})))
          scored-user-results (->> (map (fn [user-pick]
                                         (-> (if (winners-set (:pick user-pick))
                                               (assoc user-pick :points (inc (:current_streak user-pick)) :current_streak (inc (:current_streak user-pick)))
                                               (assoc user-pick :points 0 :current_streak 0))))
                                       current-picks)
                                  (remove #(previously-scored-set (:user_id %))))]
      (when-not (empty? scored-user-results)
        (mapv create-result-update-gameweek-and-user-fields! scored-user-results)))))

(defn fetch-fixtures! []
  (let [body (-> (client/get "http://api.football-data.org/v1/competitions/453/fixtures"
                             {:query-params {"matchday" (str (:gameweek (db/get-gameweek {:id 1})))}
                              :headers {"X-Response-Control" "minified"
                                        "X-Auth-Token" (env :auth-token)}})
                 :body)
        _ (save-fixtures! body)
        _ (score-finished-week! body)]
    {:status 200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body body}))

(defn fetch-token-info [client-id-token]
  (-> (client/get "https://www.googleapis.com/oauth2/v3/tokeninfo"
                  {:query-params {"id_token" client-id-token}})
      :body
      (json/read-str :key-fn keyword)))

(defn aud-contains-client-id? [token-info client-id]
  (let [aud-claim (get token-info :aud "")]
    (clojure.string/includes? aud-claim client-id)))

(defn get-user-from-token-info [token-info]
  (let [sub (get token-info :sub "")
        user (db/get-user {:id sub})]
    user))

(defn respond-success-with-user [user]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str {:user (select-keys user [:id
                                                   :name
                                                   :team
                                                   :email
                                                   :current_pick
                                                   :picks
                                                   :current_streak
                                                   :points])})})

(defn create-user-from-token-info! [token-info]
  (let [user-to-be-created {:id (get token-info :sub)
                            :name (get token-info :name "")
                            :email (get token-info :email "")
                            :team ""}
        _ (db/create-user! user-to-be-created)
        user (db/get-user {:id (:id user-to-be-created)})]
    (respond-success-with-user user)))

(defn log-in-or-create-user! [token-info]
  (if-let [user (get-user-from-token-info token-info)]
    (respond-success-with-user user)
    (create-user-from-token-info! token-info)))

(defn verify-token-info [token-info]
  (if (aud-contains-client-id? token-info (env :google-oauth2-client-id))
    (log-in-or-create-user! token-info)
    {:status 403
     :headers {}
     :body "Forbidden"}))

(defn handle-token-signin! [client-id-token]
  (let [client-id (env :google-oauth2-client-id)]
    (-> client-id-token
        fetch-token-info
        verify-token-info)))

(defn update-user! [{:keys [id] :as updated-map}]
  (let [_ (db/update-user! updated-map)
        updated-user (db/get-user {:id id})]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:user (select-keys updated-user [:id :name :email :team])})}))

(defn update-current-pick! [{:keys [id] :as updated-map}]
  (let [_ (db/update-current-pick! updated-map)
        updated-user (db/get-user {:id id})]
    {:status 200
     :headers {"Content-Type" "application/json"}}))

(defn fetch-standings []
  (let [standings (db/get-standings)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:standings standings})}))

(defn fetch-results [id]
  (let [dbresults (db/get-results {:id id})
        results (->> dbresults
                    (mapv (fn [r]
                            (update-in r [:date]
                                       #(->> (c/from-date %)
                                             (f/unparse (f/formatters :year-month-day)))))))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:results results})}))

(defroutes app
  (GET "/" []
       (slurp (io/resource "public/index.html")))
  (GET "/fixtures" []
       (fetch-fixtures!))
  (GET "/standings" []
       (fetch-standings))
  (GET "/results" [id]
       (fetch-results id))
  (POST "/pick" [id current_pick]
        (update-current-pick! {:id id
                               :current_pick current_pick}))
  (POST "/account" [id name team]
        (update-user! {:id id
                       :name name
                       :team team}))
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
      (mount/start #'threadstreaks.db.core/*db*)
      (jetty/run-jetty handler {:port port :join? false}))))
