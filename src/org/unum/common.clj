(ns org.unum.common
  (:use [clojure.contrib.logging])
  (:use [org.unum.hooks])
  (:import [java.io File])
  (:import (java.net InetAddress))
  )

(def user-home (java.lang.System/getProperty "user.home"))
(def event-time-limit 2000)


(defmacro with-timeout [ms & body]
  `(let [f# (future ~@body)]
     (.get f# ~ms java.util.concurrent.TimeUnit/MILLISECONDS)))

(defn exit [& args]
  (try
   ;; the with-timeout should probably be built into
   ;; fire-event
   (with-timeout event-time-limit
     (fire-event :kill-unum-hook))
   (finally (System/exit 0))))


(defn sleep [secs]
  (Thread/sleep (* secs 1000)))

(defn file-exists?
  "Check to see if a file exists"
  [file]
  (.exists (java.io.File. file)))


(defn load-rc []
  "I used to define a namespace in the rc file, but then it gets
tricky to reload it.  Note: see http://bc.tech.coop/blog/docs/user.clj
for a possible different approach to reloading a
namespace.  (Currently stuff just goes in user.)"
  (let [rc-path (str user-home "/.unum/unumrc")]
    (if (file-exists? rc-path)
	 (do
	   (load-file rc-path)
 	   (info (str rc-path " executed in namespace " *ns*)))
	 (warn (format "Can't load unumrc file: %s not found." rc-path)))))


;; (defn reload-and-test [lib]
;;   (do (require lib :reload)
;;       (run-tests lib)))
