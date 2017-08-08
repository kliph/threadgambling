(ns threadstreaks.api-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [threadstreaks.web :as web]
            [stubadub.core :refer [with-stub calls-to]]
            [compojure.handler :refer [site]]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]
            [luminus-migrations.core :as migrations]
            [threadstreaks.db.core :refer [*db*] :as db]
            [ring.mock.request :as mock]))

(use-fixtures :once
  (fn [f]
    (mount/start
     #'threadstreaks.db.core/*db*)
    (migrations/migrate ["migrate"] {:database-url (env :database-url)})
    (f)))


  (def sample-response "{\"count\":10,\"fixtures\":[{\"id\":150572,\"competitionId\":426,\"date\":\"2017-03-08T19:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Manchester City FC\",\"homeTeamId\":65,\"awayTeamName\":\"Stoke City FC\",\"awayTeamId\":70,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.22,\"draw\":6.5,\"awayWin\":15.0}},{\"id\":150565,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"AFC Bournemouth\",\"homeTeamId\":1044,\"awayTeamName\":\"West Ham United FC\",\"awayTeamId\":563,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":2},\"odds\":{\"homeWin\":2.5,\"draw\":3.4,\"awayWin\":2.9}},{\"id\":150568,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Chelsea FC\",\"homeTeamId\":61,\"awayTeamName\":\"Watford FC\",\"awayTeamId\":346,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150569,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Crystal Palace FC\",\"homeTeamId\":354,\"awayTeamName\":\"Tottenham Hotspur FC\",\"awayTeamId\":73,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150570,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Everton FC\",\"homeTeamId\":62,\"awayTeamName\":\"West Bromwich Albion FC\",\"awayTeamId\":74,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.7,\"draw\":3.8,\"awayWin\":5.5}},{\"id\":150571,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Hull City FC\",\"homeTeamId\":322,\"awayTeamName\":\"Swansea City FC\",\"awayTeamId\":72,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":2.4,\"draw\":3.4,\"awayWin\":3.0}},{\"id\":150573,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Middlesbrough FC\",\"homeTeamId\":343,\"awayTeamName\":\"Sunderland AFC\",\"awayTeamId\":71,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150574,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Southampton FC\",\"homeTeamId\":340,\"awayTeamName\":\"Manchester United FC\",\"awayTeamId\":66,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150566,\"competitionId\":426,\"date\":\"2017-03-11T17:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Arsenal FC\",\"homeTeamId\":57,\"awayTeamName\":\"Leicester City FC\",\"awayTeamId\":338,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150826,\"competitionId\":426,\"date\":\"2017-03-12T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Liverpool FC\",\"homeTeamId\":64,\"awayTeamName\":\"Burnley FC\",\"awayTeamId\":328,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":1.25,\"draw\":6.5,\"awayWin\":13.0}}]}")

(def sample-response-with-changed-data "{\"count\":10,\"fixtures\":[{\"id\":150572,\"competitionId\":426,\"date\":\"2017-03-08T19:45:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Manchester City FC\",\"homeTeamId\":65,\"awayTeamName\":\"Stoke City FC\",\"awayTeamId\":70,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.22,\"draw\":6.5,\"awayWin\":15.0}},{\"id\":150565,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"AFC Bournemouth\",\"homeTeamId\":1044,\"awayTeamName\":\"West Ham United FC\",\"awayTeamId\":563,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":2},\"odds\":{\"homeWin\":2.5,\"draw\":3.4,\"awayWin\":2.9}},{\"id\":150568,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Chelsea FC\",\"homeTeamId\":61,\"awayTeamName\":\"Watford FC\",\"awayTeamId\":346,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150569,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Crystal Palace FC\",\"homeTeamId\":354,\"awayTeamName\":\"Tottenham Hotspur FC\",\"awayTeamId\":73,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150570,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Everton FC\",\"homeTeamId\":62,\"awayTeamName\":\"West Bromwich Albion FC\",\"awayTeamId\":74,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.7,\"draw\":3.8,\"awayWin\":5.5}},{\"id\":150571,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Hull City FC\",\"homeTeamId\":322,\"awayTeamName\":\"Swansea City FC\",\"awayTeamId\":72,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":2.4,\"draw\":3.4,\"awayWin\":3.0}},{\"id\":150573,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Middlesbrough FC\",\"homeTeamId\":343,\"awayTeamName\":\"Sunderland AFC\",\"awayTeamId\":71,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150574,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Southampton FC\",\"homeTeamId\":340,\"awayTeamName\":\"Manchester United FC\",\"awayTeamId\":66,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150566,\"competitionId\":426,\"date\":\"2017-03-11T17:30:00Z\",\"status\":\"SCHEDULED\",\"matchday\":1,\"homeTeamName\":\"Arsenal FC\",\"homeTeamId\":57,\"awayTeamName\":\"Leicester City FC\",\"awayTeamId\":338,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150826,\"competitionId\":426,\"date\":\"2017-03-12T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Liverpool FC\",\"homeTeamId\":64,\"awayTeamName\":\"Burnley FC\",\"awayTeamId\":328,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":1.25,\"draw\":6.5,\"awayWin\":13.0}}]}")

(def sample-finished-response
"{\"count\":10,\"fixtures\":[{\"id\":150466,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Burnley FC\",\"homeTeamId\":328,\"awayTeamName\":\"West Ham United FC\",\"awayTeamId\":563,\"result\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":2,\"halfTime\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":1}},\"odds\":{\"homeWin\":2.2,\"draw\":3.3,\"awayWin\":3.5}},{\"id\":150467,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Chelsea FC\",\"homeTeamId\":61,\"awayTeamName\":\"Sunderland AFC\",\"awayTeamId\":71,\"result\":{\"goalsHomeTeam\":5,\"goalsAwayTeam\":1,\"halfTime\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":1}},\"odds\":{\"homeWin\":1.12,\"draw\":7.5,\"awayWin\":26.0}},{\"id\":150468,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Hull City FC\",\"homeTeamId\":322,\"awayTeamName\":\"Tottenham Hotspur FC\",\"awayTeamId\":73,\"result\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":7,\"halfTime\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":3}},\"odds\":{\"homeWin\":5.5,\"draw\":4.0,\"awayWin\":1.61}},{\"id\":150469,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Leicester City FC\",\"homeTeamId\":338,\"awayTeamName\":\"AFC Bournemouth\",\"awayTeamId\":1044,\"result\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":1,\"halfTime\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":1}},\"odds\":{\"homeWin\":1.83,\"draw\":3.5,\"awayWin\":4.5}},{\"id\":150470,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Liverpool FC\",\"homeTeamId\":64,\"awayTeamName\":\"Middlesbrough FC\",\"awayTeamId\":343,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":0,\"halfTime\":{\"goalsHomeTeam\":1,\"goalsAwayTeam\":0}},\"odds\":{\"homeWin\":1.25,\"draw\":5.5,\"awayWin\":13.0}},{\"id\":150472,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Southampton FC\",\"homeTeamId\":340,\"awayTeamName\":\"Stoke City FC\",\"awayTeamId\":70,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":1,\"halfTime\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":0}},\"odds\":{\"homeWin\":1.72,\"draw\":3.5,\"awayWin\":5.0}},{\"id\":150473,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Swansea City FC\",\"homeTeamId\":72,\"awayTeamName\":\"West Bromwich Albion FC\",\"awayTeamId\":74,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1,\"halfTime\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":1}},\"odds\":{\"homeWin\":1.72,\"draw\":3.6,\"awayWin\":4.5}},{\"id\":150474,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Watford FC\",\"homeTeamId\":346,\"awayTeamName\":\"Manchester City FC\",\"awayTeamId\":65,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":5,\"halfTime\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":4}},\"odds\":{\"homeWin\":9.0,\"draw\":4.5,\"awayWin\":1.36}},{\"id\":150465,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Arsenal FC\",\"homeTeamId\":57,\"awayTeamName\":\"Everton FC\",\"awayTeamId\":62,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":1,\"halfTime\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":0}},\"odds\":{\"homeWin\":1.61,\"draw\":3.75,\"awayWin\":5.5}},{\"id\":150471,\"competitionId\":426,\"date\":\"2017-05-21T14:00:00Z\",\"status\":\"FINISHED\",\"matchday\":1,\"homeTeamName\":\"Manchester United FC\",\"homeTeamId\":66,\"awayTeamName\":\"Crystal Palace FC\",\"awayTeamId\":354,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":0,\"halfTime\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":0}},\"odds\":{\"homeWin\":1.53,\"draw\":4.0,\"awayWin\":6.0}}]}"

)

(deftest renders-index
  (with-stub slurp :returns "stub"
    (let [req (web/handler (mock/request :get "/"))]
      (is (= 200 (-> req
                     :status)))
      (is (= "stub" (-> req
                        :body)))
      (is (= 1 (count (calls-to slurp))))
      (is (s/includes? (-> slurp
                           calls-to
                           first
                           first
                           .toString)
                       "index.html")))))

(deftest renders-admin
  (with-stub slurp :returns "stub"
    (let [req (web/handler (mock/request :get "/admin"))]
      (is (= 200 (-> req
                     :status)))
      (is (= "stub" (-> req
                        :body)))
      (is (= 1 (count (calls-to slurp))))
      (is (s/includes? (-> slurp
                           calls-to
                           first
                           first
                           .toString)
                       "admin.html")))))

(deftest renders-404
  (let [req (web/handler (mock/request :get "/an-invalid-path"))]
    (is (= 404 (-> req
                   :status)))
    (is (= "<h1>404 Not found</h1>" (-> req
                                        :body)))))

(deftest updates-user-account-details
  (with-stub db/update-user! :returns 1
    (let [name "Foo"
          team "South Philly Kittens"
          req (web/handler (mock/request :post "/account"
                                         {:headers {"Accept" "application/json"}
                                          :form-params {:name name
                                                        :team team}}))]
      (is (= 1 (count (calls-to db/update-user!)))))))

(deftest stores-fetched-fixtures-in-database
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (db/clear-fixtures! t-conn)
    (with-stub db/get-gameweek :returns {:gameweek 1}
      (testing "storing"
        (with-stub clj-http.client/get :returns {:body sample-response}
          (with-stub web/save-fixtures! :returns {:body sample-response}
            (let [req (web/handler (mock/request :get "/fixtures"))]
              (is (= 1 (count (calls-to web/save-fixtures!))))))))
      (testing "updates fixtures when it is changed"
        (db/save-fixtures! t-conn {:body sample-response
                                   :gameweek 1})
        (is (= {:body sample-response
                :gameweek 1}
               (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 1})
                   (select-keys [:body :gameweek]))))
        (with-stub clj-http.client/get :returns {:body sample-response-with-changed-data}
          (let [req (web/handler (mock/request :get "/fixtures"))]
            (is (= {:body sample-response-with-changed-data
                    :gameweek 1}
                   (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 1})
                       (select-keys [:body :gameweek]))))))))
    (db/clear-fixtures! t-conn)))

(deftest returns-json-fixture-response
  (with-stub clj-http.client/get :returns {:body sample-response}
    (let [req (web/handler (mock/request :get "/fixtures"))]
      (is (= 200 (-> req
                     :status)))
      (is (= sample-response (-> req
                                 :body))))))

(deftest fixtures-updated?
  (is (= false (web/fixtures-updated? sample-response
                                      {:body sample-response})))
  (is (= true (web/fixtures-updated? sample-response
                                     nil)))
  (is (= true (web/fixtures-updated? sample-response
                                     {:body sample-response-with-changed-data}))))

(deftest consumes-idtoken-form-param
  (let [user {:id "187"
              :name "Test User"
              :email "foo@example.com"
              :team ""}]
    (with-stub web/create-user-from-token-info! :returns {:status 200
                                                          :headers {}
                                                          :body {:user user}}
      (with-stub web/fetch-token-info :returns {:sub (:id user)
                                                :name (:name user)
                                                :email (:email user)}
        (with-stub web/aud-contains-client-id? :returns true
          (let [token "12345"
                req (web/handler (-> (mock/request :post "/tokensignin")
                                     (mock/content-type "application/x-www-form-urlencoded")
                                     (mock/body (str "idtoken=" token))))]
            (is (= 200 (-> req
                           :status)))
            (is (= user (-> req
                            :body
                            :user
                            (select-keys [:id :name :team :email]))))))))))



(deftest validates-the-passed-idtoken
  (is (= true
         (web/aud-contains-client-id? {:aud "abc123"}
                                      "abc123"))))

(deftest returns-error-status-when-aud-is-invalid
  (with-stub web/aud-contains-client-id? :returns false
    (let [invalid-token-info {:iss "https://accounts.google.com"
                              :sub "110169484474386276334"
                              :azp "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com"
                              :aud "invalid"
                              :iat "1433978353"
                              :exp "1433981953"
                              :email "testuser@gmail.com"
                              :email_verified "true"
                              :name  "Test User",
                              :picture "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg"
                              :given_name "Test"
                              :family_name "User"
                              :locale "en"}]
      (is (= 403
             (:status
              (web/verify-token-info invalid-token-info)))))))

(deftest all-fixtures-finished?
  (let [finished-fixtures [{:status "FINISHED"}
                           {:status "FINISHED"}]
        finished-with-canceled-and-postponed [{:status "FINISHED"}
                                              {:status "POSTPONED"}
                                              {:status "CANCELED"}]
        unfinished-fixtures [{:status "FINISHED"}
                             {:status "IN-PLAY"}]]
    (is (= true
           (web/all-finished? finished-fixtures)))
    (is (= true
           (web/all-finished? finished-with-canceled-and-postponed)))
    (is (= false
           (web/all-finished? unfinished-fixtures)))))

(deftest score-finished-week
  (let [expected-scores [{:date "2017-05-21T14:00:00Z"
                          :gameweek 1
                          :user_id "456"
                          :pick "Leicester City FC"
                          :current_streak 0
                          :points 0}
                         {:date "2017-05-21T14:00:00Z"
                          :gameweek 1
                          :user_id "789"
                          :pick "Chelsea FC"
                          :current_streak 3
                          :points 3}]]
    (with-stub db/update-current-streak! :returns 1
      (with-stub db/update-points! :returns 1
        (with-stub db/get-user :returns {:points 2
                                         :current_streak 2}
          (with-stub db/get-gameweek :returns {:gameweek 1}
            (with-stub db/get-all-current-picks :returns [{:id "1234"
                                                           :current_pick "Tottenham Hotspur FC"
                                                           :current_streak 0}
                                                          {:id "456"
                                                           :current_pick "Leicester City FC"
                                                           :current_streak 0}
                                                          {:id "789"
                                                           :current_pick "Chelsea FC"
                                                           :current_streak 2}]
              ;; Simulate having previously scored results.  We shouldn't
              ;; see half-scored weeks in practice, but this makes sure we
              ;; don't end up with multiple rows corresponding to the same
              ;; unique gameweek and user id
              (with-stub db/get-gameweek-results :returns [{:user_id "1234"}]
                (with-stub db/create-result! :returns 2
                  (web/score-finished-week! sample-finished-response)
                  (is (= (count (calls-to db/update-points!))
                         2))
                  (is (= (count (calls-to db/update-current-streak!))
                         2))
                  (is (= (->>  expected-scores
                               (mapv #(update-in % [:date] (fn [d]
                                                             (f/unparse (f/formatters :year-month-day) (f/parse d))))))

                         (->> (map first (calls-to db/create-result!))
                              (mapv #(update-in % [:date] (fn [d]
                                                            (f/unparse (f/formatters :year-month-day) (c/from-sql-time d)))))))))))))))))

(deftest respond-success-with-user
  (let [user {:id "187"
              :name "Test User"
              :email "foo@example.com"
              :last_login (java.sql.Timestamp. 1000)
              :created_at (java.sql.Timestamp. 1000)
              :team ""}
        json-body (json/write-str
                   {:user
                    (select-keys user
                                 [:id :name :email :team])})]
    (is (= 200
           (:status (web/respond-success-with-user user))))
    (is (= (json/read-str json-body)
           (json/read-str
            (:body (web/respond-success-with-user user)))))))


#_(deftest get-user-from-token-info
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [token-info {"iss" "https://accounts.google.com"
                      "sub" "110169484474386276334"
                      "azp" "1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com"
                      "aud" "valid"
                      "iat" "1433978353"
                      "exp" "1433981953"
                      "email" "testuser@gmail.com"
                      "email_verified" "true"
                      "name"  "Test User",
                      "picture" "https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg"
                      "given_name" "Test"
                      "family_name" "User"
                      "locale" "en"}
          user {:id (get token-info "sub")}]
      (db/create-user! t-conn
                       (-> user
                           (assoc :email "foo@example.com")
                           (assoc :name "Test User")
                           (assoc :team "South Philly Kittens"))
                       {:connection t-conn})
      (is (= user (-> (web/get-user-from-token-info token-info)
                      (select-keys [:id])))))))
