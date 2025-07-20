(ns com.lambdaseq.sectools.core-test
  (:require [clojure.test :refer :all]
            [com.lambdaseq.sectools.core :as sut])
  (:import [java.net ServerSocket]))

(def open-port 7888)                ;; ο “γνωστός ανοιχτός” port
(def closed-port 7889)              ;; υποθέτουμε ότι είναι ελεύθερος

(deftest scan-known-localhost-ports
  ;; Εκκινούμε έναν προσωρινό server στο port 7888
  (with-open [srv (ServerSocket. open-port)]
    ;; 1 . Σαρώνουμε ΜΟΝΟ το 7888 ⇒ πρέπει να είναι :open
    (is (= {:open   [open-port]
            :closed []}
           (sut/scan-ports "localhost" open-port open-port)))

    ;; 2 . Σαρώνουμε και 7888 + 7889
    (let [{:keys [open closed]}
          (sut/scan-ports "127.0.0.1" open-port closed-port 300)]
      (testing "ο ανοιχτός port ανιχνεύεται σωστά"
        (is (some #{open-port} open)))
      (testing "ο κλειστός port ανιχνεύεται σωστά"
        (is (some #{closed-port} closed))))))



(defn ^:private free-listening-port
  "Start a temporary ServerSocket listening on an OS-assigned port and
   return a vector [socket port-number].  Caller must close the socket."
  []
  (let [socket (ServerSocket. 0)]            ; 0 → pick any free port
    [socket (.getLocalPort socket)]))

(deftest scan-ports-detects-open-and-closed
  ;; Spin up a dummy server so we control which port is open
  (let [[srv open-port] (free-listening-port)
        closed-port     (inc open-port)]     ; assume next port isn’t in use
    (try
      (let [{:keys [open closed]}
            (sut/scan-ports "127.0.0.1" open-port closed-port 300)]
        (testing "listening port is reported open"
          (is (some #{open-port} open)))
        (testing "non-listening port is reported closed"
          (is (some #{closed-port} closed))))
      (finally (.close srv)))))              ; always shut down the server socket

(deftest scan-ports-default-timeout
  ;; Same idea, but exercising the 3-arity (default-timeout) version
  (let [[srv port] (free-listening-port)]
    (try
      (is (= {:open   [port]
              :closed []}
             (sut/scan-ports "localhost" port port))) ; uses default 200 ms
      (finally (.close srv)))))