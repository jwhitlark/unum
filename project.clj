(defproject unum "0.1.0"  ;;SEE: http://semver.org/ for versioning strategy
  :description "From many, one."
  :main org.unum
  :resources-path "resources"
  :dev-dependencies [[autodoc "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.1.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.0-SNAPSHOT"]
		 [clojure-http-client "1.0.0-SNAPSHOT"]
		 [org.apache.activemq/activemq-core "5.3.0"]
		 [org.clojars.the-kenny/clojure-couchdb "0.1.3"]
		 [org.clojure/swank-clojure "1.0"]
		 ])

;; Stuff we don't need to include UNLESS we remove one of the above.
;;		 [log4j "1.2.14"] - part of activemq
;;               [jmdns "1.0"] ; - part of activemq
