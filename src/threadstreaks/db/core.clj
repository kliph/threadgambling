(ns threadstreaks.db.core
  (:require [conman.core :as conman]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]))

(def db-spec
  {:jdbc-url (env :database-url)})

(defstate ^:dynamic *db*
  :start (conman/connect! db-spec)
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(defn get-picks-str
  ([id-map]
   (let [user (get-user id-map)]
     (if-let [picks-str (get user :picks)]
       picks-str
       "")))
  ([id-map conn]
   (let [user (get-user conn
                        id-map
                        {:connection conn})]
     (if-let [picks-str (get user :picks)]
       picks-str
       ""))))

(defn get-picks-set
  ([id-map]
   (let [picks-str (get-picks-str id-map)]
     (if (clojure.string/includes? picks-str ",")
         (into #{} (clojure.string/split
                    picks-str
                    #","))
         #{})))
  ([id-map conn]
   (let [picks-str (get-picks-str id-map
                                  conn)]
     (if (clojure.string/includes? picks-str ",")
       (into #{} (clojure.string/split
                  picks-str
                  #","))
       #{}))))



(defn add-pick!
  ([user-pick]
   (let [{pick-str :pick
          id :id} user-pick
         previous-picks-str (get-picks-str {:id id})
         update-map {:id id
                     :picks (str previous-picks-str
                                 pick-str
                                 ",")}]
     (update-picks! update-map)))
  ([user-pick conn]
   (let [{pick-str :pick
          id :id} user-pick
         previous-picks-str (get-picks-str {:id id}
                                           conn)
         update-map {:id id
                     :picks (str previous-picks-str
                                 pick-str
                                 ",")}]
     (update-picks! conn
                    update-map
                    {:connection conn}))))
