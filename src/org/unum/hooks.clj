;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.hooks
  (:use [clojure.contrib.logging])
  )

;; Note that these are NOT the same kind of hooks as provided by
;; robert.hooke.  If we're using emacs jargon, the functions defined
;; here are hooks, and the functions defined in robert.hooke are
;; closer to advice.  I suppose I'll just have rename this stuff to
;; events or something like that :'-(

;; see http://www.gnu.org/software/emacs/manual/html_node/emacs/Hooks.html

;; events are just keywords, like :host-joined

(def event-queue (ref []))
(def event-dispatch (ref {}))

;; ---------- Define hooks ----------
(def valid-hooks (ref [:host-registered-hook
		       :host-joined-hook
		       :host-disconnected-hook
		       :kill-unum-hook]))

;; In transaction, conj event to event-queue

;; A single thread reads event-queue, blocking if it's empty.  When it
;; gets an event, it looks it up in event-dispatch, and calls all the
;; functions, (if any), that are in the list.

(defn run-hook [hook]
  (do
    (info "running" hook)
    (dorun (map pcalls (@event-dispatch hook)))))

(defn list-valid-hooks []
  (keys @event-dispatch))

;; do we want list all, also?

(defn fire-event [event]
  ;; Add exception handling for illegal events
  (dosync
   (info (str "event " event "fired"))
   (alter event-queue conj event)))

(defn add-hook [hook fn]
  ;;; Add exception handling for illegal events
  (dosync
   (info (str "hook " hook " has had function " fn " added."))
   (alter event-dispatch assoc hook (conj (get @event-dispatch hook []) fn))))

;; (defn remove-hook [hook fn]
;;   (dosync
;;    (alter ...)

(defn clear-hook [hook]
  (dosync
   (alter event-dispatch assoc hook [])))

;; ---------- Start hook thread ----------
(defn hook-thread []
  (loop []
    (dosync
     (let [event (first @event-queue)]
       (if event
	 (do
	   (info (str "event " event "found in queue"))
	   (run-hook event)
	   (ref-set event-queue (rest @event-queue)))))))
  (recur))

(defn do-hooks []
  (.start (Thread. hook-thread)))


