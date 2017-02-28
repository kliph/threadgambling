(ns threadgambling.team
  (:require [threadgambling.state :as s]))

(defn results-row [props]
  (let [{:keys [date round team points]} props]
    [:tr
     [:td date]
     [:td round]
     [:td team]
     [:td points]]))

(defn team-page []
  [:div
   [:h2 "South Philly Kittens"]
   [:table
    {:cell-spacing "0" :width "100%"}
    [:thead>tr
     [:th "Dates"]
     [:th "Week"]
     [:th "Team"]
     [:th "Points"]]
    [:tbody
     (map (fn [x] ^{:key (str (:date x) (:team x))} [results-row x])
          (sort-by :week (@s/app-state :results)))]]])
