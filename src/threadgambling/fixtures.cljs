(ns threadgambling.fixtures
  (:require [threadgambling.state :as s]))

(defn pick-item [props]
  (let [{:keys [name]} props]
    [:td name]))

(defn pick-row [props]
  (let [{:keys [home-club away-club]} props]
    [:tr
     [pick-item home-club]
     [:td "-"]
     [pick-item away-club]]))

(defn fixtures-page []
  [:div
   [:h2 "Fixtures"]
   [:table
    {:cell-spacing "0" :width "100%"}
    [:thead>tr
     [:th "Home"]
     [:th]
     [:th "Away"]]
    [:tbody
     (map (fn [x] ^{:key (str (:home-club x) (:away-club x))} [pick-row x])
          (sort-by :date (@s/app-state :fixtures)))]]])
