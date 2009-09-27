(ns org.unum
  ;(:use clojure.contrib.str-utils)
  (:use zookeeper)
  (:use clojure.set)
  (:import (java.net ServerSocket Socket))
  (:import (java.io PushbackReader BufferedReader
		    InputStreamReader OutputStreamWriter
		    PrintWriter))

;  (:import (org.apache.zookeeper ZooKeeper Watcher CreateMode ZooDefs$Ids))
;  (:import (org.apache.zookeeper.data Stat ACL))
;  (:import (java.net InetAddress))
  )

; Create/define/load basic unum infrastructure (hooks, advice, etc.), then load .unum
; (load-file (str (java.lang.System/getProperty "user.home") "/.unum/unumrc"))

; Put the system tray stuff in this file
;(java.awt.SystemTray/isSupported)
;(def tray (java.awt.SystemTray/getSystemTray))
; java.awt.TrayIcon
; java.awt.Image

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

;org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists for /unum/current/godel
(defn logon []
  (let [path (str "/unum/current/" hostname)
	now (str (System/currentTimeMillis))] ;in milliseconds!
    (try
     (create-temp-znode path now)
     (catch org.apache.zookeeper.KeeperException$NodeExistsException _ (do
									 (println "Node Exists, try again in a few seconds")
									 (System/exit 1))))
     ))


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
(try
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
    (println (difference perm cur))))

(loop [] (recur))

; dbus stuff  -- This is dead; the java dbus library sucks
;(import '(org.freedesktop.dbus DBusConnection))
;(def bus (. DBusConnection (getConnection (DBusConnection/SESSION))))
;following line does something, but not sure if it's what we want.
;(def tomboy (.getRemoteObject bus "org.gnome.Tomboy" "/org/gnome/Tomboy/RemoteControl"))

; DBusConnection.requestBusName(String)

; xmlrpc server on socket
;; (def server (new redstone.xmlrpc.simple.Server 8080))
;; (def h (proxy [redstone.xmlrpc.XmlRpcInvocationHandler] []
;;        (invoke [method-name arguments]
;;           (cond
;; ;            (= method-name "add") (+ (nth arguments 0) (nth arguments 1))
;;             (= method-name "add") (apply + arguments)
;;             (= method-name "get") (get-znode-data (first arguments))
;;             true (throw (new Exception "No such method"))))))
;; (doto (.getXmlRpcServer server) (.addInvocationHandler "test" h))
;; (.start server)


; Telnet repl on socket

;; (defn repl-on-socket []
;;   (let [a (ServerSocket. 4445)]
;;     (loop []
;;       (let [b (.accept a)]
;; 	(with-open [in (-> b .getInputStream InputStreamReader. BufferedReader. PushbackReader.)
;; 		    out (-> b .getOutputStream OutputStreamWriter. PrintWriter.)]
;; 	  (binding [*in* in *out* out *err* out]
;; 	    (clojure.main/repl))))
;;       (recur))))
;; (.start (Thread. repl-on-socket))
;org.apache.zookeeper.KeeperException$NodeExistsException:
