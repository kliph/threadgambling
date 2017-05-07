(ns threadgambling.home
  (:require [threadgambling.state :as s]
            [threadgambling.color :as c]
            [threadgambling.auth :as auth]
            [reagent.session :as session]
            [cljs-react-material-ui.core :as ui]))

(defn home-page []
  (fn []
    (js/console.log @s/app-state)
    (js/console.log @session/state)
    (js/console.log (session/get :user))
    [:div.home-container
     [:div.team-info
      [:div#pick-container
       [:span#pick-icon "[Icon]"]
       [ui/chip
        {:id "pick-locked"
         :background-color c/pank}
        "LOCKED"]]
      [:div.team-container
       [:h3 (session/get-in [:user :name])]
       [:h2 {:style {:padding-left "0"}} (session/get-in [:user :team])]]
      [:div.rank
       [:span "1th Place"]]]
     [:div.actions
      [:div.quarter]
      [:ul
       [:li
        [ui/flat-button
         {:href "/#/fixtures"
          :label "Fixtures"}]]
       [:li
        [ui/flat-button
         {:href "/#/standings"
          :label "Standings"}]]
       [:li
        [ui/flat-button
         {:href "/#/update-account"
          :label "Update Account"}]]
       [:li
        [ui/flat-button
         {:href "/#/"
          :on-click (fn []
                      (swap! s/app-state assoc :page :sign-in :signed-in false)
                      (auth/onSignOut))
          :label "Sign Out"}]]]
      [:div.quarter]]]))
