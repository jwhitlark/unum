;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


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
