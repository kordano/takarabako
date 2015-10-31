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

(defmulti mutate om/dispatch)

(defmethod mutate 'finances/add
  [{:keys [state] :as env} key params]
  {:value [:finances/collection]
   :action
   #(swap! state update-in [:finances/collection] conj params)})
;;---------------------------------------------------------------------------------------------------- 
;; COMPONENTS

(defn create-table [collection]
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
                     collection))))

(defui FinanceList
  static om/IQueryParams
  (params [this]
    {:type :outcome})
  static om/IQuery
  (query [this]
    '[(:finances/net nil) (:finances/collection {:type ?type}) ])
  Object
  (render [this]
    (let [{:keys [finances/collection finances/net] :as state} (om/props this)
          {:keys [company date value] :as local} (om/get-state this)]
      (dom/div nil
               (dom/div nil
                        (dom/input #js {:type :text
                                        :placeholder "Company"
                                        :value company
                                        :onChange
                                        (fn [e]
                                          (om/set-state!
                                           this
                                           (assoc local :company
                                                  (.. e -target -value))))})
                        (dom/input #js {:type :date
                                        :placeholder "Date"
                                        :value date
                                        :onChange
                                        (fn [e]
                                          (om/set-state!
                                           this
                                           (assoc local :date
                                                  (.. e -target -value))))})
                        (dom/input #js {:type :number
                                        :placeholder "Value"
                                        :value value
                                        :onChange
                                        (fn [e]
                                          (om/set-state!
                                           this
                                           (assoc local :value
                                                  (.. e -target -value))))})
                        (dom/button #js {:onClick
                                         (fn [e]
                                           (let [new-value (-> local
                                                               (assoc :type :outcome)
                                                               (update :value js/parseFloat))]
                                             (om/transact! this `[(finances/add ~new-value)])
                                             (om/set-state! this {:company "" :date "" :value ""})))}
                                    "Add"))
               (dom/h1 nil "Net value: " net)
               (create-table collection)))))

(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})}))

(om/add-root!
 reconciler
 FinanceList
 (gdom/getElement "app"))
