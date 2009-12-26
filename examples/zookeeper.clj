(ns org.unum.zookeeper
  ;(:use clojure.contrib.str-utils)
  (:use clojure.set)
  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
  (:import (org.apache.zookeeper.data Stat ACL))
  (:import (java.net InetAddress))
)

;; ---------- Zookeeper state ----------
(def registered-hosts (atom nil))
(def connected-hosts (atom nil))

;; ---------- exception aliases ----------
; hmm, doesn't work this way, dammit, catch resolves wrong name
(def zk-connection-loss-exception org.apache.zookeeper.KeeperException$ConnectionLossException)
(def zk-node-exists-exception org.apache.zookeeper.KeeperException$NodeExistsException)

;; This should move elsewhere, how to handle stuff like this? can't
;; use (declare hostname) in this case, apparently
(def hostname (.getHostName (InetAddress/getLocalHost)))


;; ---------- Zookeeper primitives ----------
(defn start-registry-server
  "Run zookeeper, should this be blocking?"
  ;; TODO: run zookeeper (only accept connections from localhost and n2n network)
  ;; TODO: advertise zookeeper via zeroconf (only on n2n network)
  ([] (println "registry (should be) started here")))

(defn- connect
  "Connect to a zookeeper server, defaults to '10.17.74.1:2181'"
  ;TODO: add chroot suffix option?  i.e. '10.17.74.1:2181/chroot/path'
  ;TODO: allow session timeout to be specified?
  ;TODO: pass in a watcher instead of nil?  have a watcher passed back?
  ;Question: should this be a multimethod to switch on type?
  ;TODO: remove hard coded zk address
  ([] (connect "10.17.74.1:2181"))
  ([zk_server] (def zk (try
			(ZooKeeper. zk_server 3000 nil)
			(catch org.apache.zookeeper.KeeperException$ConnectionLossException _ (do
								(println "Zookeeper not reachable.")
								(System/exit 1)))))))

; Would you use a watcher as a ref, atom, agent, or something else?
; perhaps just pass in the function that updates?

; create and return a Watcher proxy - works! Damn, but clojure is cool.
(defn watcher
  "Takes a callback fn, that will be called when the watcher is called"
  ;destructure events: WatchedEvent: Znode change. Path: /mytest Type: NodeDataChanged
  ;Test if program will exit with an open watch.  need event loop if it will.
  ([] (proxy [Watcher] []
	  (process [event] (println (format "Saw event %s" event)))))
  ([fn] (proxy [Watcher] []
	  (process [event] (fn event)))))


(defn get-znode-data [path]
  ;; wire up th set watcher...
  (String. (.getData zk path false (Stat.))))

(defn get-znode-children
  ([path]
     (set (.getChildren zk path false)))
  ([path fn]
     ;; FIXME: wire up callback function properly, currently isn't working
     (set (.getChildren zk path (watcher fn)))))

(defn set-znode-data [path value]
  (.setData zk path (.getBytes value) -1))

(defn znode-exists? [path]
  (if (.exists zk path false) true false))

(defn watch-znode [path fn]
  (.exists zk path fn))

(defn create-perm-znode [path data]
  (.create zk path (.getBytes data) ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/PERSISTENT))

(defn create-temp-znode [path data]
  (.create zk path (.getBytes data) ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/EPHEMERAL))

(defn create-named-znode [pth]
  (fn []
    (let [path pth]
      (when-not (znode-exists? path)
	(create-perm-znode path "")))))


;; ---------- unum zookeeper functions ----------
(defn update-connected-hosts []
  "Get the connected host list, and set a watch to update it next time
it changes."
  ;; Fixme: should set a watch when it gets the children, not working at the moment
  (reset! connected-hosts (apply sorted-set (get-znode-children "/unum/current" update-connected-hosts))))

(defn update-registered-hosts []
  (reset! registered-hosts (apply sorted-set (get-znode-children "/unum/static"))))

(def create-account-znode (create-named-znode "/unum/accounts"))
(def create-service-znode (create-named-znode "/unum/services"))

(defn- initial-setup []
  "Initialize a brand new zookeeper"
  (do
    (create-account-znode)
    (create-service-znode)))


(defn- register []
  "Register this host name with zookeeper"
  ;; TODO: Add logic to ensure that multiple hosts don't try to use
  ;; the same name
  (let [path (str "/unum/static/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (when-not (znode-exists? path)
      (create-perm-znode path now))))

(defn- logon []
  "Log this host onto zookeeper."
  (let [path (str "/unum/current/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (try
     (create-temp-znode path now)
     (catch org.apache.zookeeper.KeeperException$NodeExistsException _ (do
					 (println "Node Exists, try again in a few seconds")
					 (System/exit 1))))))


(defn do-zookeeper [zookeeper-host]
  "Main entry point for zookeeper functionality."
  (try
   (connect zookeeper-host)
   (initial-setup)
   (catch org.apache.zookeeper.KeeperException$ConnectionLossException _ (do
					   (println "Unable to connect to zookeeper.")
					   (System/exit 1))))
  (register)
  (logon)
  (let [perm (get-znode-children "/unum/static")
	cur (get-znode-children "/unum/current")]
    (do
      (println perm)
      (println cur)
      (println (difference perm cur)))))



; (CreateMode/EPHEMERAL)
;(.setData zk "/unum" (.getBytes "It worked!") -1)
;(println (.exists zk "/foobar" false))
;(.create zk "/foobar" (.getBytes "new data")
;            ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/EPHEMERAL)
;(createTempZNode "/foobar" "my test")
