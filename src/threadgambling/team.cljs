(ns threadstreaks.team
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [threadstreaks.state :as s]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
             ))

(defn results-row [props]
  (let [{:keys [date gameweek pick points]} props]
    [:tr.zebra
     [:td date]
     [:td gameweek]
     [:td pick]
     [:td points]]))

(defn fetch-results! [team-user-id]
  (go (let [response (<! (http/get "/results"
                                   {:headers {"Accept" "application/json"}
                                    :query-params {:id team-user-id}}))
            results (-> response
                        :body
                        :results)]
        (swap! s/app-state assoc :results results))))

(defn team-page []
  (fetch-results! (@s/app-state :team-user-id))
  (fn []
    [:div.team-page
     [:h2 (get-in @s/app-state [:results 0 :team])]
     [:table
      {:cell-spacing "0" :width "100%"}
      [:thead>tr
       [:th "Dates"]
       [:th "Week"]
       [:th "Team"]
       [:th "Points"]]
      [:tbody
       (map (fn [x] ^{:key (str (:date x) (:team x))} [results-row x])
            (sort-by :week (@s/app-state :results)))]]]))
