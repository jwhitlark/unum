;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.datastore
  (:use couchdb.client)
  (:use clojure.contrib.json.write)
  (:use clojure.test)

  (:use org.unum.net)

  (:require [clojure.http.resourcefully :as resourcefully])
)

;TODO:
;  have a way to check that couchdb is installed and running
;  start replication on any connected hosts, (how to determine connection?)
;  couchdb.client does not let you easily manipulate views, add functionality, perhaps even views in clojure, since couchdb permits other languages...
;  Fix replication to handle missing databases, (currently fails, despite what the docs say)
;  Fix replication to replicate continously, (currently fails, despite what the docs say)
;  Need to create a proxy, or bind couchdb to all network addresses (yuck).

;; Constants
(def couch-loc "http://localhost:5984/")


;; Helper Funcs
(defn in [s coll]
	 (if (some #(= s %) coll)
	 true
	 false))


;; Main Funcs
(defn list-dbs []
  (database-list couch-loc))

(defn list-docs [db-name]
  (document-list couch-loc db-name))

(defn get-hname []
  hostname)

(defn init-pds []
  "Ensure personal data store exists and is set up."
  (do
    (if (not (in "unum-members" (list-dbs)))
      (database-create couch-loc "unum-members"))
    ))

(defn full-docs [db]
  (map #(document-get couch-loc db %) (list-docs db)))

(defn get-record [pred db]
  (first (filter pred (full-docs db))))

(defn get-own-record []
  (get-record #(= (:hostname %) hostname) "unum-members"))


(defn create-or-update-own-record []
  "TODO: need to version this, and provide a way to update it."
  (let [my-record (get-own-record)]
    (if (empty? my-record)
      (document-create couch-loc "unum-members"
		       {:hostname hostname
			:wired-mac-address (my-wired-mac-address)})
      (document-update couch-loc "unum-members" (:_id my-record) (assoc my-record
								   :wired-mac-address (my-wired-mac-address))))))

(defn lookup-unum-member [host-name]
  "Return the record of a given host-name from unum-members."
  (get-record #(= (:hostname %) host-name) "unum-members"))


(defn start-bidirectional-replication [first-host second-host db create continous]
  "Starts bi-directional replication between two couchdb instances."
  (let [first-url (str "http://" first-host "/_replicate" )
	first-json-payload (json-str {:source (str "http://" first-host "/" db)
				      :target (str "http://" second-host "/" db)
				      :create_target create
				      :continous continous})
	second-url (str "http://" second-host "/_replicate" )
	second-json-payload (json-str {:source (str "http://" second-host "/" db)
				       :target (str "http://" first-host "/" db)
				       :create_target create
				       :continous continous})]
    (do (resourcefully/post first-url {} first-json-payload)
	(resourcefully/post second-url {} second-json-payload))))

