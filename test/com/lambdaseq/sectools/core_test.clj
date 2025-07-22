(ns com.lambdaseq.sectools.core-test
  (:require [clojure.test :refer :all]
            [com.lambdaseq.sectools.core :as sut])
  (:import [java.net ServerSocket]))

(def open-port 7888)                
(def closed-port 7889)              

(deftest scan-known-localhost-ports

  (with-open [srv (ServerSocket. open-port)]
    (is (= {:open   [open-port]
            :closed []}
           (sut/scan-ports "localhost" open-port open-port)))

    
    (let [{:keys [open closed]}
          (sut/scan-ports "127.0.0.1" open-port closed-port 300)]
      (testing "Open ports"
        (is (some #{open-port} open)))
      (testing "Closed ports"
        (is (some #{closed-port} closed))))))



(defn ^:private free-listening-port
  "Start a temporary ServerSocket listening on an OS-assigned port and
   return a vector [socket port-number].  Caller must close the socket."
  []
  (let [socket (ServerSocket. 0)]            
    [socket (.getLocalPort socket)]))

(deftest scan-ports-detects-open-and-closed
  
  (let [[srv open-port] (free-listening-port)
        closed-port     (inc open-port)]     
    (try
      (let [{:keys [open closed]}
            (sut/scan-ports "127.0.0.1" open-port closed-port 300)]
        (testing "listening port is reported open"
          (is (some #{open-port} open)))
        (testing "non-listening port is reported closed"
          (is (some #{closed-port} closed))))
      (finally (.close srv)))))             

(deftest scan-ports-default-timeout

  (let [[srv port] (free-listening-port)]
    (try
      (is (= {:open   [port]
              :closed []}
             (sut/scan-ports "localhost" port port)))
      (finally (.close srv)))))