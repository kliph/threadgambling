(ns threadgambling.models.migration
  (:require [clojure.java.jdbc :as sql]
            [threadgambling.db :as db]))

(defn create-fixtures-table! []
  (sql/db-do-commands db/db
                      (sql/create-table-ddl
                       :fixtures
                       [[:id :serial "PRIMARY KEY"]
                        [:body :varchar "NOT NULL"]
                        [:gameweek :integer "NOT NULL"]
                        [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])))

(defn migrated? []
  (-> (sql/query db/db
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='fixtures'")])
      first
      :count
      pos?))

(defn migrate []
  (when (not (migrated?))
    (try
      (println "Creating database structure...")
      (create-fixtures-table!)
      (println "Done")
      (catch Exception e
        (-> e
            .getNextException
            .printStackTrace)))
))
