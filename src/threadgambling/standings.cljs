(ns threadgambling.standings
  (:require [threadgambling.state :as s]))

(defn standings-row [props]
  (let [{:keys [rank team user points streak current-pick]} props]
    [:tr.zebra
     [:td rank]
     [:td [:a {:href "/#/team"} team]]
     [:td user]
     [:td points]
     [:td streak]
     [:td current-pick]]))

(defn standings-page []
  [:div.standings
   [:h2 "Standings"]
   [:table
    {:cell-spacing "0" :width "100%"}
    [:thead>tr
     [:th]
     [:th
      {:data-th "Team"}
      "Team"]
     [:th
      {:data-th "User"}
      "User"]
     [:th
      {:data-th "Pts"}
      "Points"]
     [:th
      {:data-th "Strk of"}
      "Streak of"]
     [:th
      {:data-th "Curr pk"}
      "Current Pick"]]
    [:tbody
     (map (fn [x] ^{:key (str (:rank x) (:team x) (:user x))} [standings-row x])
          (sort-by :rank (@s/app-state :standings)))]]])
