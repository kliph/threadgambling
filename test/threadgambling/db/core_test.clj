(ns threadgambling.db.core-test
  (:require [threadgambling.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.java.jdbc :as jdbc]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [threadgambling.api-test :refer [sample-response]]
            [mount.core :as mount]))

(defmacro debug [& body]
  `(try
     ~@body
     (catch Exception e#
       (-> e#
           .getNextException
           .getMessage
           println))))

(use-fixtures :once
  (fn [f]
    (mount/start
     #'threadgambling.db.core/*db*)
    (migrations/migrate ["migrate"] {:database-url (env :database-url)})
    (f)))

(deftest test-fixtures
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [record {:body sample-response
                  :gameweek 28}]
      (is (= 1 (db/save-fixtures! t-conn
                                  record
                                  {:connection t-conn})))
      (is (= record (-> (db/get-fixtures-by-gameweek t-conn {:gameweek 28})
                        (select-keys [:body :gameweek])))))))

(deftest test-user
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [record-without-team {:id "110169484474386276334"
                               :email "testuser@gmail.com"
                               :name "Test User"
                               :team ""}
          record-with-team {:id "110169484474386276335"
                            :email "testuser@example.com"
                            :name "Test User"
                            :team "South Philly Kittens"}]
      (is (= nil
             (db/get-user t-conn {:id (:id "nonexistant")})))
      (is (= 1
             (db/create-user! t-conn
                              record-without-team
                              {:connection t-conn})))
      (is (= 1
             (db/delete-user! t-conn
                              {:id (:id record-without-team)}
                              {:connection t-conn})))
      (is (= 0
             (db/delete-user! t-conn
                              {:id (:id record-without-team)}
                              {:connection t-conn})))
      (is (= 1
             (db/create-user! t-conn
                              record-with-team
                              {:connection t-conn})))
      (is (= record-with-team
             (-> (db/get-user t-conn {:id (:id record-with-team)})
                 (select-keys [:id :email :name :team]))))
      (is (= {:current_streak 0
              :points 0}
             (-> (db/get-user t-conn {:id (:id record-with-team)})
                 (select-keys [:current_streak :points])))))))

(deftest updates-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [user {:id "187"
                :name "Test User"
                :email "foo@example.com"
                :team ""}
          updated-user {:id "187"
                        :name "Leroy Brown"
                        :team "South Philly Kittens"}]
      (is (= 1
             (db/create-user! t-conn
                              user
                              {:connection t-conn})))
      (db/update-user! t-conn
                       updated-user
                       {:connection t-conn})
      (is (= (merge
              user
              updated-user)
             (-> (db/get-user t-conn {:id (:id user)})
                 (select-keys [:id :email :name :team])))))))

(deftest updates-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [user {:id "187"
                :name "Test User"
                :email "foo@example.com"
                :team ""}
          user-pick-1 {:id "187"
                       :pick "Tottenham"}
          user-pick-2 {:id "187"
                       :pick "Everton"}
          expected-picks #{"Tottenham" "Everton"}]
      (db/create-user! t-conn
                       user
                       {:connection t-conn})
      (is (= #{}
             (db/get-picks-set {:id "187"}
                               t-conn)))
      (db/add-pick! user-pick-1
                    t-conn)
      (is (= #{"Tottenham"}
             (db/get-picks-set {:id "187"}
                               t-conn)))
      (is (not (= #{"Everton"}
                  (db/get-picks-set {:id "187"}
                                    t-conn))))
      (db/add-pick! user-pick-2
                    t-conn)
      (is (= expected-picks
             (db/get-picks-set {:id "187"}
                               t-conn))))))

(deftest updates-current-pick
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [user {:id "187"
                :name "Test User"
                :email "foo@example.com"
                :team ""}
          pick "Tottenham"
          user-pick-1 {:id "187"
                       :current_pick pick}]
      (db/create-user! t-conn
                       user
                       {:connection t-conn})
      (db/update-current-pick! t-conn
                               user-pick-1
                               {:connection t-conn})
      (is (= pick
             ((db/get-user t-conn
                              {:id "187"}
                              {:connection t-conn})
              :current_pick))))))

(deftest get-and-update-gameweek
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [gameweek {:id 1
                    :gameweek 38}
          updated-gameweek (-> gameweek
                               (assoc :gameweek 39))]
      (is (= gameweek
             (select-keys  (db/get-gameweek t-conn
                                            {:id 1}
                                            {:connection t-conn})
                           [:id :gameweek])))
      (db/update-gameweek! t-conn
                           updated-gameweek
                           {:connection t-conn})
      (is (= updated-gameweek
             (select-keys  (db/get-gameweek t-conn
                                            {:id 1}
                                            {:connection t-conn})
                           [:id :gameweek]))))))

(deftest get-results
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let  [result-1 {:user_id "1234"
                     :pick "Tottenham Hotspur FC"
                     :gameweek 28
                     :date (c/to-sql-time (f/parse "2017-05-21T14:00:00Z"))
                     :points 1}
           result-2 {:user_id "1234"
                     :pick "Chelsea FC"
                     :gameweek 29
                     :date (c/to-sql-time (f/parse "2017-05-21T14:00:00Z"))
                     :points 2}
           user {:id "1234"
                 :email "blah@example.com"
                 :team "Test"
                 :name "Bob Test"}]
      (db/create-user! t-conn
                       user
                       {:connection t-conn})
      (db/create-result! t-conn
                         result-1
                         {:connection t-conn})
      (db/create-result! t-conn
                         result-2
                         {:connection t-conn})
      (is (=
           (->> [result-1 result-2]
                (mapv #(dissoc % :user_id))
                (mapv #(assoc % :team (:team user)))
                (mapv #(update-in % [:date] (fn [d]
                                              (f/unparse (f/formatters :year-month-day) (c/from-sql-time d)))))
                (into #{}))
           (->> (db/get-results t-conn
                                {:id (:id user)}
                                {:connection t-conn})
                (mapv #(update-in % [:date] (fn [d]
                                              (f/unparse (f/formatters :year-month-day) (c/from-date d)))))
                (into #{})))))))

(deftest get-standings
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [user-1 {:id "187"
                  :name "Test User"
                  :email "foo@example.com"
                  :team "Chickens"
                  :current_pick "Tottenham"
                  :points 3
                  :current_streak 1}
          user-2 {:id "1878"
                  :name "Test User"
                  :email "bar@example.com"
                  :team "Tacos"
                  :current_pick "Chelsea"
                  :points 4
                  :current_streak 0}]
      (db/create-user! t-conn
                       user-1
                       {:connection t-conn})
      (db/create-user! t-conn
                       user-2
                       {:connection t-conn})
      (db/update-current-pick! t-conn
                               (select-keys user-1
                                            [:id :current_pick])
                               {:connection t-conn})
      (db/update-points! t-conn
                         (select-keys user-1
                                      [:id :points])
                         {:connection t-conn})
      (db/update-current-streak! t-conn
                                 (select-keys user-1
                                              [:id :current_streak])
                                 {:connection t-conn})
      (db/update-current-pick! t-conn
                               (select-keys user-2
                                            [:id :current_pick])
                               {:connection t-conn})
      (db/update-points! t-conn
                         (select-keys user-2
                                      [:id :points])
                         {:connection t-conn})
      (db/update-current-streak! t-conn
                                 (select-keys user-2
                                              [:id :current_streak])
                                 {:connection t-conn})

      (is (= (map #(select-keys % [:name :team :points :current_streak :current_pick])
              [user-2 user-1])
             (db/get-standings t-conn
                               {:connection t-conn}))))))
