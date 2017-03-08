(ns threadgambling.update-account-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.reagent :as rui]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [reagent.core :as r]
            [threadgambling.fixtures :as fixtures]
            [threadgambling.test-util :as test-util]
            [threadgambling.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (with-redefs [s/app-state
                                    (r/atom {:fixtures [{:home-club {:name "Tottenham"}
                                                         :away-club {:name "Everton"}
                                                         :date "2017-Jan-1"}
                                                        {:home-club {:name "Morecambe"}
                                                         :away-club {:name "Handsome Pigeons"}
                                                         :date "2017-Jan-1"}
                                                        {:home-club {:name "Leicester"}
                                                         :away-club {:name "Man City"}
                                                         :date "2017-Jan-1"}]
                                             :account {:picked #{"Tottenham" "Leicester"}}})]

                        (binding [c (tu/new-container!)]
                          (test-fn)
                          (tu/unmount! c)))))

(deftest change-name)

(deftest change-email)

(deftest change-team-name)
