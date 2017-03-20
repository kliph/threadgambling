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

(defn toggle-picked! [a]
  (if (= @a "picked")
    (reset! a "pickable")
    (reset! a "picked")))

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

(defn pick-item [props table-state]
  (let [{:keys [name]} props
        td-state (get table-state name)]
    (fn []
      (if (#{"pickable" "picked"} @td-state)
        [:td {:class @td-state
              :on-click #(do
                           (reset-other-picked! table-state name)
                           (toggle-picked! td-state))}
         name]
        [:td {:class @td-state} name]))))

(defn pick-row [props table-state]
  (let [{:keys [home-club away-club]} props]
    [:tr
     [pick-item home-club table-state]
     [:td "vs"]
     [pick-item away-club table-state]]))

(defn fetch-fixtures! [fixtures-atom]
  (go (let [response (<! (http/get "/fixtures"))
            transform-fn #(map
                           (fn [x]
                             {:home-club {:name (get x :homeTeamName)}
                              :away-club {:name (get x :awayTeamName)}
                              :date (get x :date)})
                           (get % :fixtures []))
            fixtures (-> response
                         :body
                         transform-fn)]
        (swap! fixtures-atom assoc
               :fixtures fixtures
               :fetched true))))

(defn table-and-confirm-button [table-state sorted-fixtures]
  [:div
   [:table
    [:thead>tr
     [:th "Home"]
     [:th]
     [:th "Away"]]
    [:tbody
     (doall
      (map (fn [x] ^{:key (str (:home-club x) (:away-club x))} [pick-row x @table-state])
           sorted-fixtures))]]
   [:div
    {:on-click #(confirm-pick! table-state)
     :className "pick-button"}
    [ui/raised-button
     {:label "Confirm"
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
            table-state (r/atom (zipmap table-keys table-vals))]
        [:div#fixtures
         [:h2 "Fixtures"]
         [table-and-confirm-button table-state sorted-fixtures]]))))
