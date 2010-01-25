;   Copyright (c) Jason Whitlark. All rights reserved.

(ns org.unum.ps
  (:use clojure.contrib.shell-out)
  (:use clojure.contrib.str-utils)
  )

(defn- ps-list []
  (drop 2 (re-split #"\n" (sh "ps" "-e"))))

(defn proc-running? [proc-name]
  (some #(.contains % proc-name) (ps-list)))

(defn start-proc [proc-name]
  ;Need to check type of system, how to start, then start in background
  ;i.e. if ubuntu, first try upstart, then just shell out
  )

; Start edge
; Get args from couchdb
; start_desktopcouch
; clojure oauth?
