(ns zookeeper
  ;(:use clojure.contrib.str-utils)
  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
  (:import (org.apache.zookeeper.data Stat ACL))
  (:import (java.net InetAddress))
)

; Would you use a watcher as a ref, atom, agent, or something else?
; perhaps just pass in the function that updates?

(def hostname (.getHostName (InetAddress/getLocalHost)))

(defn connect
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



; (CreateMode/EPHEMERAL)

;(.setData zk "/unum" (.getBytes "It worked!") -1)

;(println (.exists zk "/foobar" false))

;(.create zk "/foobar" (.getBytes "new data") ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/EPHEMERAL)
;(createTempZNode "/foobar" "my test")
