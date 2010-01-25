(ns org.unum.n2n
  (:use [clojure.contrib.shell-out :only (sh)])
  (:use clojure.contrib.str-utils)
  )

; (sh
;(re-seq #"\n" (sh "ps" "-e" "-f"))
; edge -r -u nobody -g nogroup -a 10.17.74.3 -c unum_network -k draka269 -l unum.whitlark.org:10177 -d n2n0 >> /var/log/n2n

;{:tun "-d" :address "-a" } ); ... for each item in defedge, call against edge-flags to turn into function call.


(defn running-edge-processes []
  (let [procs (drop 1(re-split #"\n" (sh "ps" "-f" "-Cedge")))
	proc-dicts (map #(let [[user proc _ _ _ _ _ _ & args] (re-split #" +" %)]
			   {:user user :proc proc :args args}) procs)]
    proc-dicts))


(defn is-flag? [s]
  (= "-" (first s)))

(defn edge-kw
  ;; #FIRST: finish testing this
  "Returns the keywork from edge-flags corresponding to the string passed in. i.e. '-s' returns :netmask"
  [s]
  (first (first (filter #(= (:flag (second %)) (apply str (rest s))) edge-flags))))



(loop [remaining-args (:args (first (running-edge-processes)))
	 prev-arg nil
	 parsed-args {}]
  (let [curr (first remaining-args)  ; Need to check if curr exists? (end of seq)
	curr-is-flag (is-flag? curr)
	prev-is-flag (is-flag? prev-arg)]
    (comment
      (if curr-is-flag
	(if prev-is-flag
	  (recur (rest remaining-args) curr (assoc parsed-args (edge-kw prev-arg) curr))
	  (recur (rest remaining-args) curr parsed-args))
	(recur (rest remaining-args) curr parsed-args)))))
;; #SECOND: figure out how to short circuit this when remaining-args is empty, perhaps using 	 (if (seq remaining-args)...
	 (if (is-flag? (first remaining-args))
	     (if (is-flag? last-arg)
		 ; lookup last arg in edge-flags
		 ; recur, assoc last arg keywork true
		 ; else (should never get here)
	     ; (recur (rest remaining-args) (first remaining-args) (assoc parsed-args ;edge-kw (first remaining-args))



(defstruct edge-config :tun :address :community :key :key-file :netmask :supernode
	   :re-resolve-supernode? :local-udp-port :uid :gid :foreground? :mac :mtu :multicast-forwarding? :verbose)


(def edge-flags (struct-map edge-config
     {:tun                     {:flag "d" :help "tun device name"}
      :address                 {:flag "a" :help "Set interface address. For DHCP use '-r -a dhcp:0.0.0.0'"}
      :community               {:flag "c" :help "n2n community name the edge belongs to."}
      :key                     {:flag "k" :help "Encryption key (ASCII) - also N2N_KEY=<encrypt key>. Not with -K."}
      :key-file                {:flag "K" :help "Specify a key schedule file to load. Not with -k."}
      :netmask                 {:flag "s" :help "Edge interface netmask in dotted decimal notation (255.255.255.0)."}
      :supernode               {:flag "l" :help "Supernode IP:port"}
      :re-resolve-supernode?   {:flag "b" :help "Periodically resolve supernode IP (when supernodes are running on dynamic IPs)"}
      :local-udp-port          {:flag "p" :help "Fixed local UDP port."}
      :uid                     {:flag "u" :help "User ID (numeric) to use when privileges are dropped."}
      :gid                     {:flag "g" :help "Group ID (numeric) to use when privileges are dropped."}
      :foreground?             {:flag "f" :help "Do not fork and run as a daemon; rather run in foreground."}
      :mac                     {:flag "m" :help "Fix MAC address for the TAP interface (otherwise it may be random), eg. -m 01:02:03:04:05:06"}
      :mtu                     {:flag "M" :help "Specify n2n MTU of edge interface (default 1400)."}
      :multicast-forwarding?   {:flag "r" :help "Enable packet forwarding through n2n community."}
      :verbose                 {:flag "v" :help "Make more verbose. Repeat as required."}}))

; example call (dhcp would be nice)
(defedge {:tun n2n0 :address 10.17.74.1 :community jason@whitlark.org :key-file ~/.unum/key :supernode unum.whitlark.org:10774 :UID nobody :GID nogroup :enable-packet-forwarding? true})
(start-edge n2n0)
(stop-edge n2n0)

(for (str (edge-flags arg (conf arg)))  in conf if arg not-nil

;(list-edges)
;{edge status}

(defmacro defedge [args]
  `(def ~(symbol (args :tun)) ~args))

(defn running? [edge]
  ())

(defn start-edge [edge]
  (when-not (running? edge)
    (start edge)))

(defn stop-edge [edge]
  (if (running? edge)
    (kill edge)))

;(defn list-edges [] ; allow pattern matching?
;{edge status}

(defstruct and struct-map fit in here somehow...

