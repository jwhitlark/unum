;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.apps.wake-on-lan
  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.seq-utils)
  (:use clojure.test)

  (:import (java.net InetAddress DatagramPacket DatagramSocket))

  (:use org.unum.net)
  (:use org.unum.datastore)
  )

;TODO:
;   check if wake on lan is enabled, (prob. require multi-method based on OS)
;   enable wake on lan for self, if needed.  (via command line: ethtool -s eth0 wol g) ?
;   wake a specific machine (will require mmq command channel to exist, need to tell all unum members to try to wake it, respond with results)

;DONE:
;   look up a machine's mac address by name from mds
;   Wake a specific machine from this host, (instead of all hosts)
;   make sure this machine store's it's info in the ds


(defn create-magic-packet [mac]
  "Returns a byte array with the first six bytes 0xff, and then the
   given mac address repeated 16 times."
  (let [mp (repeat 6 (byte 0xff))
	mc (mac-byte-to-string mac)]
    (into-array (Byte/TYPE) (flatten (conj (repeat 16 mc) mp)))))


(defn wake-by-mac [mac-address]
  "Given a mac address as a string delinated via : or -, send a wake-on-lan broadcast packet to 255.255.255.255.

  Bohr: 00:19:b9:67:67:7d
  utopia: 00:1d:72:8a:f8:9b
  example call: (wake '00:0E:62:09:23:4B')
  or            (wake '00-0E-62-09-23-4B')
"
  (let [broadcast-address (InetAddress/getByName "255.255.255.255")
	magic-packet-payload (create-magic-packet mac-address)
	PORT 9
	packet (DatagramPacket. magic-packet-payload (alength magic-packet-payload) broadcast-address PORT)]

    (doto (DatagramSocket.)
      (.send packet)
      (.close))))


(defn wake-by-name [host-name]
  (wake-by-mac (:wired-mac-address (lookup-unum-member host-name))))
