(ns threadgambling.fixtures-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-test.utils :as tu]
            [cljs-react-test.simulate :as sim]
            [dommy.core :as dommy :refer-macros [sel1]]
            [reagent.core :as r]
            [threadgambling.fixtures :as fixtures]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (binding [c (tu/new-container!)]
                        (test-fn)
                        (tu/unmount! c))))

(deftest fixtures-test
  (testing "deselecting"
    (let [app-state (r/atom {:fixtures [{:home-club {:name "Tottenham"}
                                         :away-club {:name "Everton"}
                                         :date "2017-Jan-1"}
                                        {:home-club {:name "Morecambe"}
                                         :away-club {:name "Handsome Pigeons"}
                                         :date "2017-Jan-1"}
                                        {:home-club {:name "Leicester"}
                                         :away-club {:name "Man City"}
                                         :date "2017-Jan-1"}]
                             :account {:picked #{"Tottenham" "Leicester"}}})
          _ (r/render [fixtures/fixtures-page] c)
          pickable (sel1 :.pickable)]
      (is (= "Everton" (:text pickable))))))
