;   Copyright (c) Jason Whitlark. All rights reserved.

(ns org.unum.n2n
  (:use [clojure.contrib.shell-out :only (sh)])
  (:use clojure.contrib.str-utils)
  (:use clojure.test)
  )

; NEXT STEP: figure out why (let
;                                [test-edge [:tun "n2n1" :address "10.17.75.1" :community "jason@whitlark.org" :key "berfin" :supernode "unum.whitlark.org:10774" :uid "nobody" :gid "nogroup" :multicast-forwarding? true]]
;                                  (start-edge-process (apply struct-map edge-config test-edge)))
; doesn't work.  Probably simple issue I'm not seeing...


;; Data structures and core references

(defstruct edge-config :tun :address :community :key :key-file :netmask :supernode
	   :re-resolve-supernode? :local-udp-port :uid :gid :foreground? :mac :mtu :multicast-forwarding? :verbose)


(def edge-flags (struct-map edge-config
     :tun                     {:flag "d" :help "tun device name"}
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
      :mac                     {:flag "m" :help "Fixed MAC address for the TAP interface (otherwise it may be random), eg. -m 01:02:03:04:05:06"}
      :mtu                     {:flag "M" :help "Specify n2n MTU of edge interface (default 1400)."}
      :multicast-forwarding?   {:flag "r" :help "Enable packet forwarding through n2n community."}
      :verbose                 {:flag "v" :help "Make more verbose. Repeat as required."}))


;; Functions


(defn running-edge-processes []
  (let [procs (drop 1(re-split #"\n" (sh "ps" "-f" "-Cedge")))
	proc-dicts (map #(let [[user proc _ _ _ _ _ _ & args] (re-split #" +" %)]
			   {:user user :proc proc :args args}) procs)]
    proc-dicts))


(defn flag? [s]
  (= \- (first s)))

(deftest test-flag?
  (is (true? (flag? "-f")))
  (is (false? (flag? "tun")))
  )


(defn edge-kw-from-flag
  ;TODO: could stand to be clearer, and perhaps with better error checking.
  "Returns the keywork from edge-flags corresponding to the string passed in. i.e. '-s' returns :netmask"
  [string-to-test]
  (first (first (filter #(= (:flag (second %)) (apply str (rest string-to-test))) edge-flags))))

(deftest test-edge-kw-from-flag
  (is (= (edge-kw-from-flag "-s") :netmask))
  )


(defn edge-flag-from-kw
  "Returns the flag corresponding to the keyword passed in.  i.e. :netmask returns '-s'."
  [edge-kw]
  (str "-" (:flag (edge-kw edge-flags))))

(deftest test-edge-flag-from-kw
  (is (= (edge-flag-from-kw :netmask) "-s")))


(defn make-first-edge-kw [s]
  "Simple helper func. (Named badly)"
  (edge-kw-from-flag (first s)))

(defn parse-edge-process
"Parse a single edge cmd arg vec into an edge-config struct.
;;i.e. ['-r' '-u' 'nobody' '-g' 'nogroup' '-a' '10.17.74.2' ...]
  becomes a valid edge-config struct."
;TODO: tidy this up - last two cases should be a single macro?
[edge-args]
(loop [current (take 2 edge-args)
       remaining edge-args
       parsed []]
  (cond
   (empty? current)  (apply struct-map edge-config parsed)
   (or (= 1 (count current))
       (flag? (second current)))
         (let [r (drop 1 remaining)]
	   (recur (take 2 r)
		  r
		  (conj parsed (make-first-edge-kw current) true)))
    :default
     (let [r (drop 2 remaining)]
       (recur (take 2 r)
	      r
	      (conj parsed (make-first-edge-kw current) (second current)))))))



(deftest test-parse-edge-process
  (is (= (parse-edge-process []) (struct edge-config)))
  (is (= (parse-edge-process ["-d" "n2n0"]) (struct edge-config "n2n0")))
  (is (= (parse-edge-process ["-a" "10.17.74.1" "-d" "n2n0"]) (struct edge-config "n2n0" "10.17.74.1")))
)


(defn parse-edge-processes
  "Parse a list of edges into edge-config structs"
  [edge-list]
  (map parse-edge-process edge-list))


(defn gen-edge-args
  "Generate a sequence of command line arguments from an edge-config struct."
  [edge-cfg]
  (let [non-nil-keys (filter #(get edge-cfg %) (keys edge-cfg))]
    (if (empty? non-nil-keys)
      []
      (reduce into (for [ky non-nil-keys]
		     (let [k (edge-flag-from-kw ky)
			   v (ky edge-cfg)]
		       (if (true? v)
			 [k]
			 [k v])))))))

(deftest test-gen-edge-args
  (is (= (gen-edge-args (struct edge-config)) []))
  (is (= (gen-edge-args (struct edge-config "n2n0")) ["-d" "n2n0"]))
  (is (= (gen-edge-args (struct edge-config "n2n0" "10.17.74.1")) ["-d" "n2n0" "-a" "10.17.74.1"]))
  (is (= (gen-edge-args (struct-map edge-config :tun "n2n0" :address "10.17.74.1" :multicast-forwarding? true)) ["-d" "n2n0" "-a" "10.17.74.1" "-r"]))
)


(defn start-edge-process
  "Start and edge process using the specified kw args."
  [args]
  (sh (cons "edge" (gen-edge-args args))))




;; Other ideas...

;; (start-edge n2n0) ; start edge by interface name...
;; (stop-edge n2n0)


;; ;(list-edges)
;; ;{edge status}

;; (defmacro defedge [args]
;;   `(def ~(symbol (args :tun)) ~args))

;; (defn running? [edge]
;;   ())

;; (defn start-edge [edge]
;;   (when-not (running? edge)
;;     (start edge)))

;; (defn stop-edge [edge]
;;   (if (running? edge)
;;     (kill edge))) ; kill-edge has some interesting permission problems...


;; ;(defn list-edges [] ; allow pattern matching?
;; ;{edge status}
