(ns takarabako.core
  (:require [goog.dom :as gdom]
            [takarabako.components :refer [Dashboard]]
            [takarabako.io :as io]
            [takarabako.parser :refer [read mutate]]
            [om.next :as om]))

(def reconciler
  (om/reconciler
   {:state (atom {:finances/collection []})
    :parser (om/parser {:read read
                        :mutate mutate})}))

(om/add-root!
 reconciler
 Dashboard
 (gdom/getElement "app"))
