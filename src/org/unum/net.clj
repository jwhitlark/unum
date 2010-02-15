;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.net
  (:use clojure.contrib.str-utils)
  (:use clojure.test)

  (:import (java.net InetAddress NetworkInterface))
)

;; Constants
(def hostname (.getHostName (InetAddress/getLocalHost)))


(defn mac-string-to-byte [mac-string]
  "Convert a string representation of a mac address into an array of bytes."
  (map #(byte (Integer/parseInt % 16)) (re-split #"[\:-]" mac-string)))


(defn mac-byte-to-string [mac-bytes]
  "Convert an array of bytes into the string representation of a mac address.
   FIXME: Doesn't handle 0 properly.  i.e. '(0x00:0xad:...  ends up as '0:ad:..."
  (let [v  (apply vector
                  (map #(Integer/toHexString (bit-and % 0xff)) mac-bytes))]
    (apply str (interpose ":" v))))


;; Funcs
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
      (first (filter #(instance? java.net.Inet4Address %) (enumeration-seq (.getInetAddresses (get-unum-interface))))))))




;; Tests
(deftest test-mac-byte-to-string
  (is (= (mac-byte-to-string "01:AD:43") '(1 -83 67))))
