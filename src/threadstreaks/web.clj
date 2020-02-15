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
      :matches))

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
       (every? #(#{"FINISHED" "CANCELED" "POSTPONED"} %)
               (map :status fixtures))))

(defn create-result-update-gameweek-and-user-fields! [scored-result]
  (let [user (db/get-user {:id (:user_id scored-result)})
        current-user-pick {:pick (:pick scored-result)
                           :id (:id user)}
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
    (db/create-result! scored-result)))

(defn score-finished-week! [body]
  (when (all-finished? (parse-fixtures body))
    (let [gameweek (:gameweek (db/get-gameweek {:id 1}))
          parsed-fixtures (parse-fixtures body)
          date (-> parsed-fixtures
                   first
                   :utcDate
                   f/parse
                   c/to-sql-time)
          game (-> parsed-fixtures
                   first)
          winners-set  (->> (map (fn [fixture]
                                   (let [home-goals (get-in fixture [:score :fullTime :homeTeam])
                                         home-goals (if (nil? home-goals)
                                                      0
                                                      home-goals)
                                         away-goals (get-in fixture [:score :fullTime :awayTeam])
                                         away-goals (if (nil? away-goals)
                                                      0
                                                      away-goals)
                                         winner-key-path (cond (> home-goals away-goals) [:homeTeam :name]
                                                               (> away-goals home-goals) [:awayTeam :name]
                                                               :else [:draw])
                                         winner (get-in fixture winner-key-path :draw)]
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
          current-gameweek (db/get-gameweek {:id 1})
          updated-gameweek {:id (:id current-gameweek)
                            :gameweek (inc (:gameweek current-gameweek))}
          _ (println "UPDATING GAMEWEEK")
          _ (println updated-gameweek)
          scored-user-results (->> (map (fn [user-pick]
                                          (-> (if (winners-set (:pick user-pick))
                                                (assoc user-pick :points (inc (:current_streak user-pick)) :current_streak (inc (:current_streak user-pick)))
                                                (assoc user-pick :points 0 :current_streak 0))))
                                        current-picks)
                                   (remove #(previously-scored-set (:user_id %))))]
      (when-not (empty? scored-user-results)
        (db/update-gameweek! updated-gameweek)
        (mapv create-result-update-gameweek-and-user-fields! scored-user-results)))))

(defn fetch-fixtures! []
  (let [body (-> (client/get "http://api.football-data.org/v2/competitions/2021/matches"
                             {:query-params {"matchday" (str (:gameweek (db/get-gameweek {:id 1})))}
                              :headers {"X-Response-Control" "minified"
                                        "X-Auth-Token" (env :auth-token)}})
                 :body)


        body "{\"count\":10,
 \"filters\":{\"matchday\":\"26\"},
 \"competition\":{\"id\":2021,
		\"area\":{\"id\":2072,
			\"name\":\"England\"},
		\"name\":\"Premier League\",
		\"code\":\"PL\",
		\"plan\":\"TIER_ONE\",
		\"lastUpdated\":\"2020-02-09T23:59:24Z\"},
 \"matches\":[{\"id\":264596,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-08T12:30:00Z\",
	     \"status\":\"FINISHED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2020-02-09T15:40:02Z\",
	     \"score\":{\"winner\":\"HOME_TEAM\",
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":3,
				  \"awayTeam\":1},
		      \"halfTime\":{\"homeTeam\":1,
				  \"awayTeam\":0},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":62,
			 \"name\":\"Everton FC\"},
	     \"awayTeam\":{\"id\":354,
			 \"name\":\"Crystal Palace FC\"},
	     \"referees\":[{\"id\":11556,
			  \"name\":\"David Coote\",
			  \"nationality\":null},
			 {\"id\":11530,
			  \"name\":\"Lee Betts\",
			  \"nationality\":null},
			 {\"id\":11425,
			  \"name\":\"Nicholas Hopton\",
			  \"nationality\":null},
			 {\"id\":11520,
			  \"name\":\"Paul Tierney\",
			  \"nationality\":null},
			 {\"id\":11494,
			  \"name\":\"Stuart Attwell\",
			  \"nationality\":null}]},
	    {\"id\":264600,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-08T17:30:00Z\",
	     \"status\":\"FINISHED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2020-02-09T15:40:02Z\",
	     \"score\":{\"winner\":\"DRAW\",
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":1,
				  \"awayTeam\":1},
		      \"halfTime\":{\"homeTeam\":0,
				  \"awayTeam\":1},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":397,
			 \"name\":\"Brighton & Hove Albion FC\"},
	     \"awayTeam\":{\"id\":346,
			 \"name\":\"Watford FC\"},
	     \"referees\":[{\"id\":11487,
			  \"name\":\"Kevin Friend\",
			  \"nationality\":null},
			 {\"id\":11488,
			  \"name\":\"Simon Bennett\",
			  \"nationality\":null},
			 {\"id\":11595,
			  \"name\":\"Adrian Holmes\",
			  \"nationality\":null},
			 {\"id\":11430,
			  \"name\":\"Simon Hooper\",
			  \"nationality\":null},
			 {\"id\":11610,
			  \"name\":\"Andre Marriner\",
			  \"nationality\":null}]},
	    {\"id\":264598,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-09T14:00:00Z\",
	     \"status\":\"FINISHED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2020-02-09T23:59:24Z\",
	     \"score\":{\"winner\":\"HOME_TEAM\",
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":2,
				  \"awayTeam\":1},
		      \"halfTime\":{\"homeTeam\":1,
				  \"awayTeam\":1},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":356,
			 \"name\":\"Sheffield United FC\"},
	     \"awayTeam\":{\"id\":1044,
			 \"name\":\"AFC Bournemouth\"},
	     \"referees\":[{\"id\":11567,
			  \"name\":\"Jonathan Moss\",
			  \"nationality\":null},
			 {\"id\":11531,
			  \"name\":\"Marc Perry\",
			  \"nationality\":null},
			 {\"id\":11480,
			  \"name\":\"Eddie Smart\",
			  \"nationality\":null},
			 {\"id\":11479,
			  \"name\":\"Lee Mason\",
			  \"nationality\":null},
			 {\"id\":11309,
			  \"name\":\"Peter Bankes\",
			  \"nationality\":null}]},
	    {\"id\":264591,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-14T20:00:00Z\",
	     \"status\":\"FINISHED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":\"DRAW\",
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":0,
				  \"awayTeam\":0},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":76,
			 \"name\":\"Wolverhampton Wanderers FC\"},
	     \"awayTeam\":{\"id\":338,
			 \"name\":\"Leicester City FC\"},
	     \"referees\":[]},
	    {\"id\":264599,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-15T12:30:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":340,
			 \"name\":\"Southampton FC\"},
	     \"awayTeam\":{\"id\":328,
			 \"name\":\"Burnley FC\"},
	     \"referees\":[]},
	    {\"id\":264597,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-15T17:30:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":68,
			 \"name\":\"Norwich City FC\"},
	     \"awayTeam\":{\"id\":64,
			 \"name\":\"Liverpool FC\"},
	     \"referees\":[]},
	    {\"id\":264592,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-16T14:00:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":58,
			 \"name\":\"Aston Villa FC\"},
	     \"awayTeam\":{\"id\":73,
			 \"name\":\"Tottenham Hotspur FC\"},
	     \"referees\":[]},
	    {\"id\":264594,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-16T16:30:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":57,
			 \"name\":\"Arsenal FC\"},
	     \"awayTeam\":{\"id\":67,
			 \"name\":\"Newcastle United FC\"},
	     \"referees\":[]},
	    {\"id\":264595,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-17T20:00:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2019-12-21T18:35:13Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":61,
			 \"name\":\"Chelsea FC\"},
	     \"awayTeam\":{\"id\":66,
			 \"name\":\"Manchester United FC\"},
	     \"referees\":[]},
	    {\"id\":264593,
	     \"season\":{\"id\":468,
		       \"startDate\":\"2019-08-09\",
		       \"endDate\":\"2020-05-17\",
		       \"currentMatchday\":26},
	     \"utcDate\":\"2020-02-19T19:30:00Z\",
	     \"status\":\"SCHEDULED\",
	     \"matchday\":26,
	     \"stage\":\"REGULAR_SEASON\",
	     \"group\":\"Regular Season\",
	     \"lastUpdated\":\"2020-02-11T20:35:15Z\",
	     \"score\":{\"winner\":null,
		      \"duration\":\"REGULAR\",
		      \"fullTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"halfTime\":{\"homeTeam\":null,
				  \"awayTeam\":null},
		      \"extraTime\":{\"homeTeam\":null,
				   \"awayTeam\":null},
		      \"penalties\":{\"homeTeam\":null,
				   \"awayTeam\":null}},
	     \"homeTeam\":{\"id\":65,
			 \"name\":\"Manchester City FC\"},
	     \"awayTeam\":{\"id\":563,
			 \"name\":\"West Ham United FC\"},
	     \"referees\":[]}]}"

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
                                                   :rank
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
