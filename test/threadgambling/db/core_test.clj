(ns threadgambling.db.core-test
  (:require [threadgambling.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.java.jdbc :as jdbc]
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
                 (select-keys [:id :email :name :team])))))))

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
