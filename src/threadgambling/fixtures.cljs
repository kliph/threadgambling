(ns threadgambling.fixtures
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [threadgambling.state :as s]
            [cognitect.transit :as t]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cljs-react-material-ui.core :as ui]))

(def previously-picked?
  (get-in @s/app-state [:account :picked] #{}))

(defn toggle-picked! [a confirm-disabled]
  (if (= @a "picked")
    (do (reset! a "pickable")
        (reset! confirm-disabled true))
    (do (reset! a "picked")
        (reset! confirm-disabled false))))

(defn reset-other-picked! [table-state name]
  (doall
   (map
    (fn [x]
      (compare-and-set! (val x) "picked" "pickable"))
    (filter #(not= (key %) name) table-state))))

(defn disable-other-pickable! [table-state]
  (doall
   (map
    (fn [x]
      (compare-and-set! (val x) "pickable" ""))
    table-state)))

(defn confirm-pick! [table-state]
  (let [pick (first (filter #(= (deref (val %)) "picked")  @table-state))]
    (when pick
      (js/console.log (str "Picked " (key pick)))
      (disable-other-pickable! @table-state)
      (reset! (val pick) "confirmed"))))

(defn pick-item [props club type table-state]
  (let [{:keys [name goals]} club
        {:keys [confirm-disabled]} props
        td-state (get table-state name)]
    (fn []
      (if (#{"pickable" "picked"} @td-state)
        [:td {:class @td-state
              :data-th type
              :on-click #(do
                           (reset-other-picked! table-state name)
                           (toggle-picked! td-state confirm-disabled))}
         name
         (when goals
           [:span.goals goals])]
        [:td {:class @td-state}
         name
         (when goals
           [:span.goals goals])]))))

(defn pick-row [props table-state]
  (let [{:keys [home-club away-club]} props]
    [:tr
     [pick-item props home-club "Home" table-state]
     [:td.vs "vs"]
     [pick-item props away-club "Away" table-state]]))

(defn fetch-fixtures! [fixtures-atom]
  (go (let [response (<! (http/get "/fixtures"))
            transform-fn #(map
                           (fn [x]
                             {:home-club {:name (get x :homeTeamName)
                                          :goals (get-in x [:result :goalsHomeTeam])}
                              :away-club {:name (get x :awayTeamName)
                                          :goals (get-in x [:result :goalsAwayTeam])}
                              :date (get x :date)})
                           (get % :fixtures []))
            fixtures (-> response
                         :body
                         transform-fn)]
        (swap! fixtures-atom assoc
               :fixtures fixtures
               :fetched true))))

(defn table-and-confirm-button [table-state sorted-fixtures confirm-disabled]
  [:div
   [:table.fixtures
    [:thead>tr
     [:th "Home"]
     [:th]
     [:th "Away"]]
    [:tbody
     (doall
      (map (fn [fixture]
             ^{:key (str (:home-club fixture) (:away-club fixture))}
             [pick-row (-> fixture
                           (assoc :confirm-disabled confirm-disabled))
              @table-state])
           sorted-fixtures))]]
   [:div
    {:on-click #(confirm-pick! table-state)
     :className "pick-button"}
    [ui/raised-button
     {:label "Lock Pick"
      :disabled @confirm-disabled
      :full-width true}]]])

(defn fixtures-page []
  (let [fixtures-atom (@s/app-state :fixtures)]
    (fetch-fixtures! fixtures-atom)
    (fn []
      (let [sorted-fixtures (sort-by :date (:fixtures @fixtures-atom))
            table-keys (-> (into [] (map #(get-in % [:home-club :name]) sorted-fixtures))
                           (into (map #(get-in % [:away-club :name]) sorted-fixtures)))
            table-vals  (map #(if (previously-picked? %)
                                (r/atom "disabled")
                                (r/atom "pickable"))
                             table-keys)
            table-state (r/atom (zipmap table-keys table-vals))
            confirm-disabled (r/atom true)]
        [:div#fixtures
         [:h2 "Fixtures"]
         [table-and-confirm-button table-state sorted-fixtures confirm-disabled]]))))
