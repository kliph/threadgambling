(ns threadgambling.core
  (:require [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.reagent :as rui]
            [reagent.core :as r]
            [threadgambling.fixtures :as f]
            [threadgambling.standings :as standings]
            [threadgambling.state :as s]
            [goog.dom]))

(def by-id goog.dom.getElement)

(defn footer []
  [:div#footer
   [:ul
    [:li
     [ui/flat-button
      {:href "https://soundcloud.com/user-70053674"
       :label "Podcast"}]]
    [:li
     [ui/flat-button
      {:href "https://twitter.com/soccerthread"
       :label "Twitter"}]]
    [:li
     [ui/flat-button
      {:href "https://www.facebook.com/soccerthread/"
       :label "Facebook"}]]]])

(def nav-links
  [:div#nav-links
   [:span
    [:a {} "threadgambling"]]
   [:span.pull-right
    [ui/flat-button
     {:on-click #(swap! s/app-state assoc :page :fixtures)
      :style {:color "#FFFFFF"}
      :label "Fixtures"}]
    [ui/flat-button
     {:on-click #(swap! s/app-state assoc :page :standings)
      :style {:color "#FFFFFF"}
      :label "Standings"}]]])

(defmulti current-page #(@s/app-state :page))
(defmethod current-page :fixtures []
  [f/fixtures-page])
(defmethod current-page :standings []
  [standings/standings-page])

(defn home-page []
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   [:div
    [rui/app-bar { ;; :style {:position "fixed"}
                  :show-menu-icon-button false
                  :title (r/as-element nav-links)}]
    [:div
     {:style {:padding "1em"}}
     [current-page]]
    [footer]]])

(r/render-component [home-page] (by-id "app"))
