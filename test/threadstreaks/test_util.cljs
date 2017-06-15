(ns threadstreaks.test-util
  (:require [cljs-react-material-ui.reagent :as rui]
            [reagent.core :as r]
            [cljs-react-material-ui.core :as ui]))

(defn test-container [component]
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   component])

(defn click [node]
  (.click node)
  (r/flush))
