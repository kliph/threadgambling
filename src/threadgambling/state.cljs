(ns threadgambling.state
  (:require [reagent.core :as r]))

(def app-state (r/atom {:page :standings
                        :standings [{:rank 1
                                     :team "Pigeons"
                                     :user "Cliff"
                                     :points "35"
                                     :streak "0"
                                     :current-pick "Hull"}
                                    {:rank 2
                                     :team "WTFT"
                                     :user "Colin"
                                     :points "32"
                                     :streak "0"
                                     :current-pick "Evernton"}
                                    {:rank 3
                                     :team "PDX All Stars"
                                     :user "Dan"
                                     :points "31"
                                     :streak "4"
                                     :current-pick "-"}]
                        :fixtures [{:home-club {:name "Tottenham"}
                                    :away-club {:name "Everton"}
                                    :date "2017-Jan-1"}
                                   {:home-club {:name "Morecambe"}
                                    :away-club {:name "Handsome Pigeons"}
                                    :date "2017-Jan-1"}
                                   {:home-club {:name "Leicester"}
                                    :away-club {:name "Man City"}
                                    :date "2017-Jan-1"}]}))
