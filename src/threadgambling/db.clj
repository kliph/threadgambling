(ns threadgambling.db
  (:require [clojure.java.jdbc :as sql]))

(def db (or (System/getenv "DATABASE_URL")
            "postgresql://localhost:5432/threadgambling-dev"))
