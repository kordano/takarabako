(ns takarabako.io
  (:require [cljs.core.async :refer [<! >!]]
            [chord.client :refer [ws-ch]]
            [om.next :as om])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn open-channel
  "Opens channel to server and dispatches incoming messages "
  [component]
  (go
    (let [uri (goog.Uri. js/location.href)
          ssl? (= (.getScheme uri) "https")
          socket-uri "ws://localhost:8090/ws"
          #_(str (if ssl?  "wss://" "ws://")
                 (.getDomain uri)
                 (str ":" (if (= (.getDomain uri) "localhost") 8090 (.getPort uri)))
                 "/ws")
          {:keys [ws-channel error]} (<! (ws-ch socket-uri))]
      (if-not error
        (do
          (println "Channel opened!")
          (om/transact! component `[(input/ws {:ws ~ws-channel})])
          (>! ws-channel {:type :init :data nil :meta nil})
          (go-loop [{{:keys [type meta data] :as message} :message err :error} (<! ws-channel)]
            (if-not err
              (when message
                (case type
                  :init (om/transact! component `[(finances/set {:data ~data})])
                  :add (println "Transaction complete!")
                  :unrelated)
                (recur (<! ws-channel)))
              (println "Channel error on response"))))
        (println "Channel error on open" error)))))

(defn send! [state data]
  (let [{:keys [input/ws]} @state]
    (go (>! ws data))))
