;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.net
  (:use clojure.contrib.str-utils)

  (:import (java.net InetAddress NetworkInterface))
)

;; Constants
(def hostname (.getHostName (InetAddress/getLocalHost)))


;; Funcs
(defn my-interfaces []
  "Returns a seq containing all the NetworkInterface(s) of this machine."
  (enumeration-seq (NetworkInterface/getNetworkInterfaces)))


(defn my-wired-mac-address []
  "FIXME: Currently just grabs eth0's mac address."
  (mac-byte-to-string (.getHardwareAddress (NetworkInterface/getByName "eth0"))))


(defn mac-string-to-byte [mac-string]
  "Convert a string representation of a mac address into an array of bytes."
  (map #(byte (Integer/parseInt % 16)) (re-split #"[\:-]" mac)))


(defn mac-byte-to-string [mac-bytes]
  "Convert an array of bytes into the string representation of a mac address.
   FIXME: Doesn't handle 0 properly.  i.e. '(0x00:0xad:...  ends up as '0:ad:..."
  (let [v  (apply vector
                  (map #(Integer/toHexString (bit-and % 0xff)) mac-bytes))]
    (apply str (interpose ":" v))))


;; Tests
(deftest test-mac-byte-to-string
  (is (= (mac-byte-to-string "01:AD:43") '(1 -83 67))))
