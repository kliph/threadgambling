(ns threadgambling.state
  (:require [reagent.core :as r]))

(def app-state (r/atom {:page :sign-in
                        :signed-in false
                        :account {:name "Colin Smith"
                                  :team "South Philly Kittens"
                                  :email "foo@example.com"
                                  :picked #{"Tottenham" "Leicester"}}
                        :results [{:date "Jan 1 2017"
                                   :round 1
                                   :team "Spurs"
                                   :points 1}
                                  {:date "Jan 7 2017"
                                   :round 2
                                   :team "Everton"
                                   :points 0}
                                  {:date "Jan 13 2017"
                                   :round 3
                                   :team "Man U"
                                   :points 1}
                                  {:date "Jan 22 2017"
                                   :round 4
                                   :team "Spurs"
                                   :points 2}
                                  {:date "Jan 28 2017"
                                   :round 5
                                   :team "Man City"
                                   :points 3}
                                  {:date "Feb 4 2017"
                                   :round 6
                                   :team "Chelsea"
                                   :points 4}
                                  {:date "Feb 10 2017"
                                   :round 7
                                   :team "-"
                                   :points "-"}
                                  {:date "Feb 18 2017"
                                   :round 8
                                   :team "-"
                                   :points "-"}]
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
                        :fixtures (r/atom {:fetched false})}))
