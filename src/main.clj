(ns main
  (:require [aleph.tcp :as tcp]
            [manifold.deferred :as d]
            [manifold.stream :as s]))

(defn f[msg]
  (println (String. msg)))

(defn my-consume [f stream]
  (d/loop []
    (d/chain (s/take! stream ::drained)

             ;; if we got a message, run it through `f`
             (fn [msg]
               (if (identical? ::drained msg)
                 ::drained
                 (f msg)))

             ;; wait for the result from `f` to be realized, and
             ;; recur, unless the stream is already drained
             (fn [result]
               (when-not (identical? ::drained result)
                 (d/recur))))))


(defn chat []
  (let [conn (tcp/client {:host "hitchcock.freenode.net" :port 6667 })]
    (my-consume f @conn)
    @(s/put! @conn "PASS NOPASS\n\r")
    @(s/put! @conn "NICK busq1\n\r")
    @(s/put! @conn "USER busq1 0 * :Clojure client\n\r")
    @(s/put! @conn "JOIN #clojure\n\r")
    ))

;;(chat)

