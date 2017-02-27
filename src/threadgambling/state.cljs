(ns threadgambling.state
  (:require [reagent.core :as r]))

(def app-state (r/atom {:page :fixtures
                        :fixtures [{:home-club {:name "Tottenham"}
                                    :away-club {:name "Everton"}
                                    :date "2017-Jan-1"}
                                   {:home-club {:name "Morecambe"}
                                    :away-club {:name "Handsome Pigeons"}
                                    :date "2017-Jan-1"}
                                   {:home-club {:name "Leicester"}
                                    :away-club {:name "Man City"}
                                    :date "2017-Jan-1"}]}))
