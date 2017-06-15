(ns threadstreaks.standings
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [threadstreaks.state :as s]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn fetch-standings! []
  (go (let [response (<! (http/get "/standings" {:headers {"Content-Type" "application/json"}}))
            standings (->> (get-in response [:body :standings])
                           (map-indexed (fn [idx x]
                                          (assoc x :rank (inc idx)))))]
        (swap! s/app-state assoc
               :standings standings))))

(defn standings-row [props]
  (let [{:keys [rank team name points id]} props
        current-streak (:current_streak props)
        current-pick (:current_pick props)]
    [:tr.zebra
     [:td rank]
     [:td [:a {:href (str "/#/team/" id)} team]]
     [:td name]
     [:td points]
     [:td current-streak]
     [:td current-pick]]))

(defn standings-page []
  (fetch-standings!)
  (fn []
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
            (@s/app-state :standings))]]]))
