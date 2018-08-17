(ns threadstreaks.home
  (:require [threadstreaks.state :as s]
            [threadstreaks.color :as c]
            [threadstreaks.auth :as auth]
            [reagent.session :as session]
            [cljs-react-material-ui.core :as ui]))

(defn rank-to-nth [n]
  (cond
    (= n 1) (str n "st")
    (= n 2) (str n "nd")
    (= n 3) (str n "rd")
    :else (str n "th")))

(defn home-page []
  (fn []
    [:div.home-container
     [:div.team-info
      [:div#pick-container
       (when (string? (session/get-in [:user :current_pick]))
         [:div
          [:span (session/get-in [:user :current_pick])]
          [ui/chip
           {:id "pick-locked"
            :background-color c/pank}
           "LOCKED"]])]
      [:div.team-container
       [:h3 (session/get-in [:user :name])]
       [:h2 {:style {:padding-left "0"}} (session/get-in [:user :team])]]
      [:div.rank
       [:span
        (-> (session/get-in [:user :rank])
            rank-to-nth)
        " place"]]]
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
