(ns takarabako.core
  (:gen-class :main true)
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]
            [org.httpkit.server :refer [send! with-channel on-close on-receive run-server]]
            [taoensso.carmine :as car :refer (wcar)]
            [clojure.core.async :refer [go <!!]]))

(defn now [] (new java.util.Date))

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {:host "127.0.0.1" :port 6379}} ~@body))


(defn create-socket-handler [state]
  (fn [request]
    (with-channel request channel
      (on-close channel (fn [status]))
      (on-receive channel (fn [data]
                            (let [action (read-string data)]
                              (println action)))))))
(defn create-routes
  "Create routes from server state"
  [state]
  (defroutes all-routes
    (route/resources "/")
    (GET "/" [] (io/resource "public/index.html"))
    (GET "/ws" [] (create-socket-handler state))
    (route/not-found "<h1>Page not found</h1>")))

(defn start-server
  "startAllServices™"
  [port]
  (let [state (atom {:server nil})]
    (create-routes @state)
    (swap! state assoc :server (run-server #'all-routes {:port port}))
    state))

(defn -main [& args]
  (let [port (second args)]
    (start-server (if port (Integer/parseInt port) 8090))
    (println "Server startet at localhost:8080")))

(comment

  (def state (start-server 8090))
  
  )

