(ns takarabako.parser
  (:require [om.next :as om]))

;;--------------------------------------------------------------------------------
;; READS

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :finances/collection
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (filter #(= type (:type %)) (:finances/collection @state))})

(defmethod read :finances/summary
  [{:keys [state] :as env} key {:keys [type]}]
  {:value (->> (:finances/collection @state)
               (filter #(= type (:type %)))
               (map :value)
               (reduce +))})

(defmethod read :finances/net
  [{:keys [state] :as env} key params]
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

(defmethod mutate 'finances/add
  [{:keys [state] :as env} key params]
  {:value [:finances/collection]
   :action
   #(swap! state update-in [:finances/collection] conj params)})

(defmethod mutate 'finances/set
  [{:keys [state] :as env} key {:keys [data]}]
  {:action
   #(swap! state assoc-in [:finances/collection] data)})
