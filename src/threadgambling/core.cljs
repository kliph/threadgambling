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

(def nav-links
  [:div#nav-links
   [:span
    [:a {} "threadgambling"]]
   [:span.pull-right
    [:a {:on-click #(swap! s/app-state assoc :page :fixtures)}
     "Fixtures"]
    [:a {:on-click #(swap! s/app-state assoc :page :standings)}
     "Standings"]]])

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
    [current-page]]])

(r/render-component [home-page] (by-id "app"))
