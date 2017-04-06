(ns threadgambling.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [threadgambling.web :as web]
            [stubadub.core :refer [with-stub calls-to]]
            [compojure.handler :refer [site]]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]
            [luminus-migrations.core :as migrations]
            [threadgambling.db.core :refer [*db*] :as db]
            [ring.mock.request :as mock]))

  (def sample-response "{\"count\":10,\"fixtures\":[{\"id\":150572,\"competitionId\":426,\"date\":\"2017-03-08T19:45:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Manchester City FC\",\"homeTeamId\":65,\"awayTeamName\":\"Stoke City FC\",\"awayTeamId\":70,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.22,\"draw\":6.5,\"awayWin\":15.0}},{\"id\":150565,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"AFC Bournemouth\",\"homeTeamId\":1044,\"awayTeamName\":\"West Ham United FC\",\"awayTeamId\":563,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":2},\"odds\":{\"homeWin\":2.5,\"draw\":3.4,\"awayWin\":2.9}},{\"id\":150568,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Chelsea FC\",\"homeTeamId\":61,\"awayTeamName\":\"Watford FC\",\"awayTeamId\":346,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150569,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Crystal Palace FC\",\"homeTeamId\":354,\"awayTeamName\":\"Tottenham Hotspur FC\",\"awayTeamId\":73,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150570,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Everton FC\",\"homeTeamId\":62,\"awayTeamName\":\"West Bromwich Albion FC\",\"awayTeamId\":74,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.7,\"draw\":3.8,\"awayWin\":5.5}},{\"id\":150571,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Hull City FC\",\"homeTeamId\":322,\"awayTeamName\":\"Swansea City FC\",\"awayTeamId\":72,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":2.4,\"draw\":3.4,\"awayWin\":3.0}},{\"id\":150573,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Middlesbrough FC\",\"homeTeamId\":343,\"awayTeamName\":\"Sunderland AFC\",\"awayTeamId\":71,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150574,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Southampton FC\",\"homeTeamId\":340,\"awayTeamName\":\"Manchester United FC\",\"awayTeamId\":66,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150566,\"competitionId\":426,\"date\":\"2017-03-11T17:30:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Arsenal FC\",\"homeTeamId\":57,\"awayTeamName\":\"Leicester City FC\",\"awayTeamId\":338,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150826,\"competitionId\":426,\"date\":\"2017-03-12T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Liverpool FC\",\"homeTeamId\":64,\"awayTeamName\":\"Burnley FC\",\"awayTeamId\":328,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":1.25,\"draw\":6.5,\"awayWin\":13.0}}]}")

(def sample-response-with-changed-data "{\"count\":10,\"fixtures\":[{\"id\":150572,\"competitionId\":426,\"date\":\"2017-03-08T19:45:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Manchester City FC\",\"homeTeamId\":65,\"awayTeamName\":\"Stoke City FC\",\"awayTeamId\":70,\"result\":{\"goalsHomeTeam\":0,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.22,\"draw\":6.5,\"awayWin\":15.0}},{\"id\":150565,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"AFC Bournemouth\",\"homeTeamId\":1044,\"awayTeamName\":\"West Ham United FC\",\"awayTeamId\":563,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":2},\"odds\":{\"homeWin\":2.5,\"draw\":3.4,\"awayWin\":2.9}},{\"id\":150568,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Chelsea FC\",\"homeTeamId\":61,\"awayTeamName\":\"Watford FC\",\"awayTeamId\":346,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150569,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Crystal Palace FC\",\"homeTeamId\":354,\"awayTeamName\":\"Tottenham Hotspur FC\",\"awayTeamId\":73,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150570,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Everton FC\",\"homeTeamId\":62,\"awayTeamName\":\"West Bromwich Albion FC\",\"awayTeamId\":74,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":0},\"odds\":{\"homeWin\":1.7,\"draw\":3.8,\"awayWin\":5.5}},{\"id\":150571,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Hull City FC\",\"homeTeamId\":322,\"awayTeamName\":\"Swansea City FC\",\"awayTeamId\":72,\"result\":{\"goalsHomeTeam\":2,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":2.4,\"draw\":3.4,\"awayWin\":3.0}},{\"id\":150573,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Middlesbrough FC\",\"homeTeamId\":343,\"awayTeamName\":\"Sunderland AFC\",\"awayTeamId\":71,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150574,\"competitionId\":426,\"date\":\"2017-03-11T15:00:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Southampton FC\",\"homeTeamId\":340,\"awayTeamName\":\"Manchester United FC\",\"awayTeamId\":66,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150566,\"competitionId\":426,\"date\":\"2017-03-11T17:30:00Z\",\"status\":\"POSTPONED\",\"matchday\":28,\"homeTeamName\":\"Arsenal FC\",\"homeTeamId\":57,\"awayTeamName\":\"Leicester City FC\",\"awayTeamId\":338,\"result\":{\"goalsHomeTeam\":null,\"goalsAwayTeam\":null},\"odds\":null},{\"id\":150826,\"competitionId\":426,\"date\":\"2017-03-12T16:00:00Z\",\"status\":\"FINISHED\",\"matchday\":28,\"homeTeamName\":\"Liverpool FC\",\"homeTeamId\":64,\"awayTeamName\":\"Burnley FC\",\"awayTeamId\":328,\"result\":{\"goalsHomeTeam\":3,\"goalsAwayTeam\":1},\"odds\":{\"homeWin\":1.25,\"draw\":6.5,\"awayWin\":13.0}}]}")

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

(deftest stores-fetched-fixtures-in-database
  (mount/start #'threadgambling.db.core/*db*)
  (migrations/migrate ["migrate"] {:database-url (env :database-url)})
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (testing "storing"
      (with-stub clj-http.client/get :returns {:body sample-response}
        (with-stub web/save-fixtures! :returns {:body sample-response}
          (let [req (web/handler (mock/request :get "/fixtures"))]
            (is (= 1 (count (calls-to web/save-fixtures!))))))))
    (testing "updates fixtures when it is changed"
      (db/save-fixtures! t-conn {:body sample-response
                                 :gameweek 28})
      (is (= {:body sample-response
              :gameweek 28}
             (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 28})
                 (select-keys [:body :gameweek]))))
      (with-stub clj-http.client/get :returns {:body sample-response-with-changed-data}
        (let [req (web/handler (mock/request :get "/fixtures"))]
          (is (= {:body sample-response-with-changed-data
                  :gameweek 28}
                 (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 28})
                     (select-keys [:body :gameweek])))))))))



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
