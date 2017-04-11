(ns threadgambling.auth
  (:require [threadgambling.state :as s]))

(set! *warn-on-infer* true)

(defn ^:export on-sign-in [^js/gapi.auth2.GoogleUser google-user]
  (let [^js/gapi.auth2.BasicProfile profile (.getBasicProfile google-user)]
    (js/console.log (str "ID: " (.getId profile)))
    (js/console.log (str "Name: " (.getName profile)))
    (js/console.log (str "Email: " (.getEmail profile)))
    (js/console.log (str "Profile: " profile))))

(defn onSignOut []
  (let [^js/gapi.auth2.GoogleAuth auth2 (.getAuthInstance js/gapi.auth2)
        ^js/Promise sign-out (.signOut auth2)]
    (.then sign-out
           (fn [] (js/console.log "User Signed out")))))

(defn sign-in-page []
  [:div#sign-in-page
   ;; [:a {:on-click #(swap! s/app-state assoc
   ;;                        :signed-in true)
   ;;      :href "/#/sign-in"}]

   [:div {:className "g-signin2"
          :data-onsuccess "onSignIn"}]
   [:a {:on-click (fn [] (onSignOut))} "Sign out"]])
