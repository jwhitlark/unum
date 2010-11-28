;   Copyright (c) Jason Whitlark. 2009,2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.net
  (:use clojure.contrib.logging)
  (:use clojure.contrib.str-utils)
  (:use clojure.test)

  (:import (java.net InetAddress NetworkInterface DatagramSocket DatagramPacket))
)

;; Constants
(def hostname (.getHostName (InetAddress/getLocalHost)))
(def announce-msg "Unum announce:")
(def default-udp-announce-port 51423)

;; State
(def announce-listener-socket (ref nil))

;; Alter state
(defn init-announce-listener-socket []
  (dosync (ref-set announce-listener-socket (DatagramSocket. default-udp-announce-port))))

(defn close-announce-listener-socket []
  (.close @announce-listener-socket)
  (dosync (ref-set announce-listener-socket nil)))

;; Functions

(defn mac-string-to-byte [mac-string]
  "Convert a string representation of a mac address into an array of bytes."
  (map #(byte (Integer/parseInt % 16)) (re-split #"[\:-]" mac-string)))


(defn mac-byte-to-string [mac-bytes]
  "Convert an array of bytes into the string representation of a mac address.
   FIXME: Doesn't handle 0 properly.  i.e. '(0x00:0xad:...  ends up as '0:ad:..."
  (let [v  (apply vector
                  (map #(Integer/toHexString (bit-and % 0xff)) mac-bytes))]
    (apply str (interpose ":" v))))


(defn my-interfaces []
  "Returns a seq containing all the NetworkInterface(s) of this machine."
  (enumeration-seq (NetworkInterface/getNetworkInterfaces)))


(defn my-wired-mac-address []
  "FIXME: Currently just grabs eth0's mac address."
  (mac-byte-to-string (.getHardwareAddress (NetworkInterface/getByName "eth0"))))


(defn- get-unum-interface []
  "Grab the unum n2n0 interface."
  (NetworkInterface/getByName "n2n0"))


(defn my-unum-mac-address []
  "Grab the mac address from n2n0, if it exists.  Returns nil if it doesn't."
  (let [unum-interface (get-unum-interface)]
    (if (nil? unum-interface)
      nil
      (mac-byte-to-string (.getHardwareAddress unum-interface)))))


(defn my-unum-ip-address []
  "Grab the ipv4 address from n2n0, if it exists.  Returns nil if it doesn't."
  (let [unum-interface (get-unum-interface)]
    (if (nil? unum-interface)
      nil
      (let [addresses (filter #(instance? java.net.Inet4Address %) (enumeration-seq (.getInetAddresses (get-unum-interface))))
	    first-as-string (.getHostAddress (first addresses))]
	first-as-string
))))
;; TODO: HARDCODED: Arthur, can you whip this up?
(defn my-unum-broadcast-address []
  "Determine the broadcast address of the primary n2n interface, n2n0"
  "10.17.74.255")

(defn broadcast-unum-annonunce-udp [address port]
  "Takes address as a string, and port as an integer."
  (let [target_address (InetAddress/getByName address)
      msg (.getBytes (str announce-msg hostname))
      sock (DatagramSocket.)
      packet (DatagramPacket. msg (count msg) target_address port)]
  (doto sock
	(.send packet)
	(.close))))

(defn receive-single-udp-packet [socket]
  (let [buffer (byte-array 4096)
	packet (DatagramPacket. buffer (count buffer))]
    (do
      (.receive socket packet)
      (let [msg (String. buffer 0 (.getLength packet))
	    source-addr (.getAddress packet)]
	(debug (str "Received unum announce (" msg ") from " source-addr))
	{:message msg :source-ip source-addr}
	    ))))


(defn listen-for-unum-announce-udp [callback]
  "Start a thread that listenes for unum announce packets on a specific interface, and calls the callback when they are received."
  (.start (Thread. #((loop []
      (info "Starting listener for unum udp announce packets")
      (callback (receive-single-udp-packet @announce-listener-socket))
      (recur))))))




;; Tests
;; FIXME: char cannot be cast to number?
;; (deftest test-mac-byte-to-string
;;   (is (= (mac-byte-to-string "01:AD:43") '(1 -83 67))))

(deftest one-failing-test-to-make-sure-it-can
  (is (= true false)))
