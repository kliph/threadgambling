(ns threadgambling.core
  (:require [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.reagent :as rui]
            [reagent.core :as r]
            [threadgambling.fixtures :as f]
            [threadgambling.standings :as standings]
            [threadgambling.state :as s]
            [threadgambling.team :as team]
            [threadgambling.home :as home]
            [threadgambling.account :as account]
            [threadgambling.auth :as auth]
            [goog.dom]))

(def by-id goog.dom.getElement)

(defn footer []
  [:div#footer
   [:ul
    [:li
     [ui/flat-button
      {:style {:color "#FFFFFF"}
       :href "https://soundcloud.com/user-70053674"
       :label "Podcast"}]]
    [:li
     [ui/flat-button
      {:style {:color "#FFFFFF"}
       :href "https://twitter.com/soccerthread"
       :label "Twitter"}]]
    [:li
     [ui/flat-button
      {:style {:color "#FFFFFF"}
       :href "https://www.facebook.com/soccerthread/"
       :label "Facebook"}]]]])

(defn nav-links []
  [:div#nav-links
   [:span
    [:a {:on-click #(when (:signed-in @s/app-state)
                      (swap! s/app-state assoc :page :home))} "threadgambling"]]
   (if (:signed-in @s/app-state)
     [:span.pull-right
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc :page :fixtures)
        :style {:color "#FFFFFF"}
        :label "Fixtures"}]
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc :page :standings)
        :style {:color "#FFFFFF"}
        :label "Standings"}]
      [ui/flat-button
       {:on-click #(swap! s/app-state assoc
                          :page :sign-in
                          :signed-in false)
        :style {:color "#FFFFFF"}
        :label "Sign Out"}]])])

(defmulti current-page #(@s/app-state :page))
(defmethod current-page :fixtures []
  [f/fixtures-page])
(defmethod current-page :standings []
  [standings/standings-page])
(defmethod current-page :team []
  [team/team-page])
(defmethod current-page :home []
  [home/home-page])
(defmethod current-page :update-account []
  [account/update-page])
(defmethod current-page :sign-in []
  [auth/sign-in-page])

(defn app-container []
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   [:div
    [rui/app-bar {:style {:background-color "#29002E"}
                  :show-menu-icon-button false
                  :title (r/as-element (nav-links))}]
    [:div
     {:style {:padding-top "1em"
              :padding-bottom "3em"
              :box-shadow "0 1px 6px rgba(0,0,0,0.12), 0 1px 4px rgba(0,0,0,0.12)"
              :position "relative"
              :z-index 1000}}
     [:div {:style {:margin "1em"}}
      [current-page]]]
    [footer]]])

(r/render-component [app-container] (by-id "app"))
