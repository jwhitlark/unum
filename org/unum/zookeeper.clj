(ns org.unum.zookeeper
  ;(:use clojure.contrib.str-utils)
  (:use clojure.set)
  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
  (:import (org.apache.zookeeper.data Stat ACL))
  (:import (java.net InetAddress))
)

;; ---------- exception aliases ----------
; hmm, doesn't work this way, dammit, catch resolves wrong name
(def zk-connection-loss-exception org.apache.zookeeper.KeeperException$ConnectionLossException)
(def zk-node-exists-exception org.apache.zookeeper.KeeperException$NodeExistsException)

; Would you use a watcher as a ref, atom, agent, or something else?
; perhaps just pass in the function that updates?

(def hostname (.getHostName (InetAddress/getLocalHost)))

(defn- connect
  "Connect to a zookeeper server, defaults to '10.17.74.1:2181'"
  ;TODO: add chroot suffix option?  i.e. '10.17.74.1:2181/chroot/path'
  ;TODO: allow session timeout to be specified?
  ;TODO: pass in a watcher instead of nil?  have a watcher passed back?
  ;Question: should this be a multimethod to switch on type?
  ([] (connect "10.17.74.1:2181"))
;; TODO: following doesn't have any effect, why?
  ([zk_server] (def zk (try
			(ZooKeeper. zk_server 3000 nil)
			(catch org.apache.zookeeper.KeeperException$ConnectionLossException _ (do
								(println "Zookeeper not reachable.")
								(System/exit 1)))))))



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
  (String. (.getData zk path false (Stat.))))

(defn get-znode-children [path]
 (set (.getChildren zk path false)))

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

(def create-account-znode
     (create-named-znode "/unum/accounts"))

(def create-service-znode
     (create-named-znode "/unum/services"))


(defn- initial-setup []
  (do
    (create-account-znode)
    (create-service-znode)))

(defn- register []
  (let [path (str "/unum/static/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (when-not (znode-exists? path)
      (create-perm-znode path now))))

;org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /unum/current/godel
(defn- logon []
  (let [path (str "/unum/current/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (try
     (create-temp-znode path now)
     (catch  org.apache.zookeeper.KeeperException$NodeExistsException _ (do
					 (println "Node Exists, try again in a few seconds")
					 (System/exit 1))))
    ))


(defn do-zookeeper [zookeeper-host]
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
