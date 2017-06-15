(ns threadstreaks.state
  (:require [reagent.core :as r]
            [reagent.session :as session]))

(def app-state (r/atom {:page :sign-in
                        :signed-in false
                        :results []
                        :standings []
                        :fixtures (r/atom {:fetched false})}))
