(ns threadgambling.db.core-test
  (:require [threadgambling.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [threadgambling.api-test :refer [sample-response]]
            [mount.core :as mount]))

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
