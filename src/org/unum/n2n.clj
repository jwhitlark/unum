(ns org.unum.n2n
;  (:use [org.unum.notify])
  )

; edge -r -u nobody -g nogroup -a 10.17.74.3 -c unum_network -k draka269 -l unum.whitlark.org:10177 -d n2n0 >> /var/log/n2n

(defstruct edge-config :tun :mode-address :community :encrypt-key :encrypt-key-file
	   :netmask :supernode :re-resolve-supernote? :local-udp-port :UID :GID
	   :fork? :mac :mtu :enable-packet-forwarding? :verbose-count)

; want to specify flag, flag-name, help, default, ?
; example call (dhcp would be nice)
(defedge {:tun n2n0 :address 10.17.74.1 :community jason@whitlark.org :key-file ~/.unum/key :supernode unum.whitlark.org:10774 :UID nobody :GID nogroup :enable-packet-forwarding? true})
(start-edge n2n0)
(stop-edge n2n0)

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

