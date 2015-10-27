(ns takarabako.core
  (:require [goog.dom :as gdom]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]))

(enable-console-print!)

(def app-state
  (atom
   {:finances/collection
    [{:type :outcome
      :company "Rewe"
      :date "2015-10-01"
      :value 10.02}
     {:type :outcome
      :company "Aldi"
      :date "2014-10-02"
      :value 5.67}
     {:type :income
      :company "URZ"
      :date "2015-10-01"
      :value 500.00}
     {:type :outcome
      :company "Studentenwerk"
      :date "2015-10-10"
      :value 25.00}]}))

;;---------------------------------------------------------------------------------------------------- 
;; PARSERS

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

;;---------------------------------------------------------------------------------------------------- 
;; COMPONENTS

(defui FinanceList
  static om/IQueryParams
  (params [this]
    {:type :outcome})
  static om/IQuery
  (query [this]
    '[(:finances/net nil) (:finances/collection {:type ?type}) ])
  Object
  (render [this]
    (let [{:keys [finances/collection finances/net]} (om/props this)]
      (dom/div nil
               (dom/table nil
                          (dom/thead
                           nil
                           (dom/tr nil
                                   (dom/th nil "Date")
                                   (dom/th nil "Company")
                                   (dom/th nil "Value")))
                          (apply dom/tbody
                                 nil
                                 (map
                                  (fn [{:keys [company date value]}]
                                    (dom/tr nil
                                            (dom/td nil date)
                                            (dom/td nil company)
                                            (dom/td nil value)))
                                  collection)))
               (dom/h1 nil "Net value: " net)))))

(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read})}))

(om/add-root!
 reconciler
 FinanceList
 (gdom/getElement "app"))
