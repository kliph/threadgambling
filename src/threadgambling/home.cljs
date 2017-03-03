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
       {:on-click #(swap! s/app-state assoc :page :fixtures)
        :label "Fixtures"}]]
     [:li
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc :page :standings)
        :label "Standings"}]]
     [:li
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc :page :update-account)
        :label "Update Account"}]]
     [:li
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc
                          :page :sign-in
                          :signed-in false)
        :label "Sign Out"}]]]
    [:div.quarter]]])
