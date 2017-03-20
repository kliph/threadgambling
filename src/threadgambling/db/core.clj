(ns threadgambling.db.core
  (:require [conman.core :as conman]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]))

(def db-spec
  {:jdbc-url (env :database-url)})

(defstate ^:dynamic *db*
  :start (conman/connect! db-spec)
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")
