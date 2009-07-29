(ns org.unum
  ;(:use clojure.contrib.str-utils)
  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
  (:import (org.apache.zookeeper.data Stat ACL))
  (:import (java.net InetAddress))
  )

(def hostname (.getHostName (InetAddress/getLocalHost)))
(def zk (ZooKeeper. "10.17.74.1:2181" 3000 nil))
;(. ZooDefs$Ids OPEN_ACL_UNSAFE)
;(def ZD (ZooDefs/Ids.))
;(.Ids temp_ids)

;Register the hostname in static
;Register the hostname in current

(defn sleep [secs]
  (Thread/sleep (* secs 1000)))

(defn getUnumData [path]
  (String. (.getData zk path false (Stat.))))

(defn createPermZNode [path data]
  (.create zk path (.getBytes data) ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/PERSISTENT))

(defn createTempZNode [path data]
  (.create zk path (.getBytes data) ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/EPHEMERAL))

; try java.net.NetworkInterface  getNetworkInterfaces


; (CreateMode/EPHEMERAL)

(println "Greetings from" hostname)

(println (getUnumData "/unum"))

(.setData zk "/unum" (.getBytes "It worked!") -1)

(println (getUnumData "/unum"))

;(println (.exists zk "/unum" false))
(println (.exists zk "/foobar" false))

;(.create zk "/foobar" (.getBytes "new data") ZooDefs$Ids/OPEN_ACL_UNSAFE CreateMode/EPHEMERAL)
(createTempZNode "/foobar" "my test")
(sleep 5)

(println (.exists zk "/foobar" false))
