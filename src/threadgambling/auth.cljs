(ns threadgambling.auth
  (:require [threadgambling.state :as s]))

(defn sign-in-page []
  [:div#sign-in-page
   [:a {:on-click #(swap! s/app-state assoc
                          :signed-in true)
        :href "/#/sign-in"}
    [:img {:src "https://developers.google.com/identity/images/btn_google_signin_light_normal_web.png"
           :alt "Sign in with google"}]]])
