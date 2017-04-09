(ns threadgambling.auth
  (:require [threadgambling.state :as s]))

(defn onSignIn [google-user]
  (let [profile (.getBasicProfile google-user)]
    (js/console.log (str "ID: " (.getId profile)))
    (js/console.log (str "Name: " (.getName profile)))
    (js/console.log (str "Email: " (.getEmail profile)))
    (js/console.log (str "Profile: " profile))))

(defn onSignOut []
  (let [auth2 (.getAuthInstance gapi/auth2)]
    (.then (.signOut auth2)
           (fn [] (js/console.log "User Signed out")))))

(defn sign-in-page []
  [:div#sign-in-page
   ;; [:a {:on-click #(swap! s/app-state assoc
   ;;                        :signed-in true)
   ;;      :href "/#/sign-in"}]

   [:div {:className "g-signin2"
          :data-onsuccess "onSignIn"}]
   [:a {:on-click (fn [] (onSignOut))} "Sign out"]])
