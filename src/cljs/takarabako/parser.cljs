(ns takarabako.parser
  (:require [om.next :as om]
            [takarabako.io :as io]))

;;--------------------------------------------------------------------------------
;; READS

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} key params]
  (println "READ:" key)
  (if-let [[_ v] (find @state key)]
    {:value v}
    {:value []}))

(defmethod read :dashboard/collection
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (:finances/collection @state)})

(defmethod read :dashboard/collection
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (:finances/collection @state)})

(defmethod read :finances/selected
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (filter #(= type (:type %)) (:finances/collection @state))})

(defmethod read :finances/summary
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (->> (:finances/collection @state)
               (filter #(= type (:type %)))
               (map :value)
               (reduce +))})

(defmethod read :finances/net
  [{:keys [state] :as env} key _]
  {:value (let [income (->> (:finances/collection @state)
                            (filter #(= (:type %) :income))
                            (map :value)
                            (reduce +))
                outcome (->> (:finances/collection @state)
                            (filter #(= (:type %) :outcome))
                            (map :value)
                            (reduce +))]
            (- income outcome))})

;;--------------------------------------------------------------------------------
;; MUTATIONS

(defmulti mutate om/dispatch)

(defmethod mutate 'input/ws
  [{:keys [state] :as env} key {:keys [ws]}]
  {:action
   #(swap! state assoc-in [:input/ws] ws)})

(defmethod mutate 'finances/add
  [{:keys [state] :as env} key params]
  {:value [:finances/collection]
   :action
   #(do
      (io/send! state {:type :add :data params :meta nil})
      (swap! state update-in [:finances/collection] conj params))})

(defmethod mutate 'finances/set
  [{:keys [state] :as env} key {:keys [data]}]
  {:value [:finances/collection]
   :action
   #(swap! state assoc-in [:finances/collection] data)})
