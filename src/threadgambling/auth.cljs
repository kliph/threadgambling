(ns threadgambling.auth
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [threadgambling.state :as s]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as reagent]
            [reagent.session :as session]))

;; (set! *warn-on-infer* true)

(defn sign-in-session!
  [user]
  (session/put! :user user)
  (swap! s/app-state assoc
         :signed-in true
         :page :home))

(defn post-sign-in-id-token!
  [^String id-token]
  (go (let [response  (<!  (http/post "/tokensignin"
                                      {:headers {"Accept" "application/json"}
                                       :form-params {:idtoken id-token}}))
            success? (= 200 (:status response))
            response-body (:body response)]
        (when success?
          (sign-in-session! (:user response-body))))))

(defn on-sign-in [^js/gapi.auth2.GoogleUser google-user]
  (let [^js/gapi.auth2.BasicProfile profile (.getBasicProfile google-user)
        ^js/gapi.auth2AuthResponse auth-response (.getAuthResponse google-user)
        id-token (.-id-token auth-response)]
    (post-sign-in-id-token! id-token)))

(defn on-failure [error]
  (js/console.log error))


(defn render-button []
  (console.log "Trying to render")
  (.render js/gapi.signin2
           "google-signin"
           #js {"scope" "profile email"
                "longtitle" true
                "onsuccess" on-sign-in
                "onfailure" on-failure}))

(goog.exportSymbol "renderButton", render-button)

(defn onSignOut []
  (js/console.log "trying to log out")
  (try
    (let [^js/gapi.auth2.GoogleAuth auth2 (.getAuthInstance js/gapi.auth2)
          ^js/Promise sign-out (.signOut auth2)]
      (.then sign-out
             (fn []
               (swap! s/app-state assoc :page :sign-in)
               (js/console.log (session/get :user))
               (session/remove! :user)
               (js/console.log "User Signed out"))))
    (catch js/TypeError _)))

(defn sign-in-page-did-mount []
  (try
    (render-button)
    (catch js/ReferenceError _)))

(defn sign-in-page-render []
  [:div#sign-in-page
   [:div {:id "google-signin"}]])

(defn sign-in-page []
  (reagent/create-class {:reagent-render sign-in-page-render
                         :component-did-mount sign-in-page-did-mount} ))
