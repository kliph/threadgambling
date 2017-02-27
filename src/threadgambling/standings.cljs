(ns threadgambling.standings
  (:require [threadgambling.state :as s]))

(defn standings-row [props]
  (let [{:keys [rank team user points streak current-pick]} props]
    [:tr
     [:td rank]
     [:td team]
     [:td user]
     [:td points]
     [:td streak]
     [:td current-pick]]))

(defn standings-page []
  [:div
   [:h2 "Standings"]
   [:table
    {:cell-spacing "0" :width "100%"}
    [:thead>tr
     [:th]
     [:th "Team"]
     [:th "User"]
     [:th "Points"]
     [:th "Streaks"]
     [:th "Current Pick"]]
    [:tbody
     (map (fn [x] ^{:key (str (:rank x) (:team x) (:user x))} [standings-row x])
          (sort-by :rank (@s/app-state :standings)))]]])
