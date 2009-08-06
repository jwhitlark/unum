(ns org.unum
  ;(:use clojure.contrib.str-utils)
  (:use zookeeper)
  (:use clojure.set)
;  (:require '(org.enclojure.repl.main :as enclojure))
;  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
;  (:import (org.apache.zookeeper.data Stat ACL))
;  (:import (java.net InetAddress))
  )

(connect)

;Register the hostname in static
;Register the hostname in current

(defn sleep [secs]
  (Thread/sleep (* secs 1000)))

; This is duplicated with the register stuff lower, find a way to collapse it.
(defn create-named-znode [pth]
  (fn []
    (let [path pth]
      (when-not (znode-exists? path)
	(create-perm-znode path "")))))

(def create-account-znode
     (create-named-znode "/unum/accounts"))

(def create-service-znode
     (create-named-znode "/unum/services"))


(defn initial-setup []
  (do
    (create-account-znode)
    (create-service-znode)))

(defn register []
  (let [path (str "/unum/static/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (when-not (znode-exists? path)
      (create-perm-znode path now))))

(defn logon []
  (let [path (str "/unum/current/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (create-temp-znode path now)))


(defn unum-test []
  (println "Greetings from" hostname)

  (println (format "Verifying /baz does not exist: %s" (znode-exists? "/baz")))
  (println (format "Verifying /unum exists: %s" (znode-exists? "/unum")))

  (set-znode-data "/unum" "initial set")
  (println (get-znode-data "/unum"))

  (set-znode-data "/unum" "change ok")
  (println (get-znode-data "/unum"))

  (create-temp-znode "/mytest" "a")
  (println "setting watch on /mytest")
  (watch-znode "/mytest" (watcher))
  (set-znode-data "/mytest" "b")


  ;Infinite loop
  (loop [] (recur))
)


;(unum-test)
(initial-setup)
(register)
(logon)
(let [perm (get-znode-children "/unum/static")
      cur (get-znode-children "/unum/current")]
  (do
    (println perm)
    (println cur)
    (println (difference perm cur))))

;(loop [] (recur))

; xmlrpc server on socket
(def server (new redstone.xmlrpc.simple.Server 8080))
(def h (proxy [redstone.xmlrpc.XmlRpcInvocationHandler] []
       (invoke [method-name arguments]
          (cond
            (= method-name "add") (+ (nth arguments 0) (nth arguments 1))
            (= method-name "get") (get-znode-data (nth arguments 0))
            true (throw (new Exception "No such method"))))))
(doto (.getXmlRpcServer server) (.addInvocationHandler "test" h))
(.start server)


; Telnet repl on socket
(import '(java.net ServerSocket Socket)
        '(java.io PushbackReader BufferedReader
                  InputStreamReader OutputStreamWriter
                  PrintWriter))

(let [a (ServerSocket. 4445)]
  (loop []
    (let [b (.accept a)]
      (with-open [in (-> b .getInputStream InputStreamReader. BufferedReader. PushbackReader.)
              out (-> b .getOutputStream OutputStreamWriter. PrintWriter.)]
        (binding [*in* in *out* out *err* out]
          (clojure.main/repl))))
    (recur)))
