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
     [:h3 "Colin Smith"]
     [:h2 "South Philly Kittens"]]
    [:div.rank
     [:span "1th Place"]]]
   [:div#footer
    [:div#third]
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
       {:label "Results"}]]]
    [:div#third]]])
