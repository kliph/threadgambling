(ns threadgambling.core
  (:require [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.reagent :as rui]
            [reagent.core :as r]
            [goog.dom]))

(def by-id goog.dom.getElement)

(def app-state (r/atom {:page :home
                        :tables  [{:position 1
                                   :club "Tottenham"}
                                  {:position 3
                                   :club "Mighty Morecambe Shrimps"}
                                  {:position 2
                                   :club "Chelsea"}]}))

(defn pick-row [props]
  (let [{:keys [position club]} props]
    [:tr
     [:td position]
     [:td club]]))

(defn pick-table []
  [:div
   [:h2 "Tables"]
   [:table
    {:cell-spacing "0" :width "100%"}
    [:thead>tr
     [:th "Position"]
     [:th "Club"]]
    [:tbody
     (map (fn [x] ^{:key (:position x)} [pick-row x])
          (sort-by :position (@app-state :tables)))]]])

(defn home-page []
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   [:div
    [rui/app-bar { ;; :style {:position "fixed"}
                  :show-menu-icon-button false
                  :title "threadgambling"}]
    [pick-table]]])


(defmulti current-page #(@app-state :page))
(defmethod current-page :home []
  [home-page])

(r/render-component [current-page] (by-id "app"))
