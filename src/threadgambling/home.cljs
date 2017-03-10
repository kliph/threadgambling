(ns threadgambling.home
  (:require [threadgambling.state :as s]
            [cljs-react-material-ui.core :as ui]))

(defn home-page []
  [:div.home-container
   [:div.team-info
    [:div#pick-container
     [:span#pick-icon "[Icon]"]
     [:div#pick-locked "LOCKED"]]
    [:div.team-container
     [:h3 (get-in @s/app-state [:account :name])]
     [:h2 {:style {:padding-left "0"}} (get-in @s/app-state [:account :team])]]
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
       {:href "/#/sign-out"
        :label "Sign Out"}]]]
    [:div.quarter]]])
