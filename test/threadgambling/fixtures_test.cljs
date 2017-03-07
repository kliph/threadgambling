(ns threadgambling.fixtures-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as rui]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1]]
            [reagent.core :as r]
            [threadgambling.fixtures :as fixtures]
            [threadgambling.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (binding [c (tu/new-container!)]
                        (test-fn)
                        (tu/unmount! c))))

(defn test-container [component]
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   component])

(defn click [node]
  (.click node)
  (r/flush))

(with-redefs [s/app-state (r/atom {:fixtures [{:home-club {:name "Tottenham"}
                                               :away-club {:name "Everton"}
                                               :date "2017-Jan-1"}
                                              {:home-club {:name "Morecambe"}
                                               :away-club {:name "Handsome Pigeons"}
                                               :date "2017-Jan-1"}
                                              {:home-club {:name "Leicester"}
                                               :away-club {:name "Man City"}
                                               :date "2017-Jan-1"}]
                                   :account {:picked #{"Tottenham" "Leicester"}}})]
  (deftest fixtures-test
    (testing "Clicking pickable toggles to picked and back"
      (let [_ (r/render (test-container [fixtures/fixtures-page]) c)
            pickable (sel1 c [:.pickable])]
        (is (= "Everton" (dommy/text pickable)))
        (click pickable)
        (is (= "picked" (dommy/class pickable)))
        (click pickable)
        (is (= "pickable" (dommy/class pickable)))))
    (testing "Clicking other pickable when one is picked makes that one picked and the other pickable")
    (testing "Clicking disabled does nothing")
    (testing "Clicking confirm does nothing")
    (testing "Clicking confirm with something picked confirms it")))
