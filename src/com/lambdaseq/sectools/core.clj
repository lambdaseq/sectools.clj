(ns com.lambdaseq.sectools.core)

; ALL PHASES HERE 

(import '[java.net Socket InetSocketAddress])
;Java networking classes so we can construct sockets and connect to destination addresses

; Purpose:
; Probe a host across an inclusive range of TCP ports by attempting to
; open a client connection to each one. Classify each port as open if
; a TCP connect succeeds within the timeout, otherwise closed

; Return values:
; :open   [open1 open2 ...] for ports that accepted a TCP connection
; and :closed [closed1 closed2 ...] for ports that timed out, refused, or errored

; Arities:
; (scan-ports host start-port end-port) and by default the timeout time is 200ms
; (scan-ports host start-port end-port timeout-ms) user given timeout time

; Notes:
; TCP connect success menas something is listening or at least the port isn't filtered
; Any timeout, ECONNREFUSED, network unreachable, etc. means the port is closed

(defn scan-ports
  "Attempt TCP connections to ports in the inclusive range [start-port end-port]
   on HOST. Return a vector with :open ports[...] :closed ports [...].
   Arities:
   (scan-ports host start-port end-port) by default the timeout time is 200 ms
   (scan-ports host start-port end-port timeout-ms)  custom timeout time"
  ; FUNCTION ARGUMENTS ARE: HOST OR IP 
  ; A RANGE OF PORTS TO SCAN FOR THE GIVEN HOST/IP
  ; AND OPTIONAL THE TIMEOUT TIME
  ; 3 Arity with default timeout time
  ([host start-port end-port]
   (scan-ports host start-port end-port 200))
  ; 4 Arity with custom timeout time
  ([host start-port end-port timeout-ms] 
   (let [ports (range start-port (inc end-port))] 
     (reduce
      (fn [acc port]
        ; Try to connect to a client socket within timeout time
        (try
          (with-open [sock (Socket.)]
            ; Perform the TCP connect attempt
            ; If connection succeeds within timeout time, no exception is thrown
            (.connect sock (InetSocketAddress. host (int port)) timeout-ms)
            ; Success. Classify port as open update the open vector
            (update acc :open conj port))
          ; Any Exception like timeout, refused, network error, etc. means we
          ; treat the port as closed and update the closed vector
          (catch java.io.IOException _ 
            (update acc :closed conj port))))
      ; Initialize the vectors before scanning any ports
      {:open [] :closed []}
      ; The sequence of ports to scan
      ports))))

