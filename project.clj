(defproject unum "0.1.0"  ;;SEE: http://semver.org/ for versioning strategy
  :description "From many, one."
  :main org.unum
  :resources-path "resources"
;  :dev-dependencies [[autodoc "0.7.0"]
;		     [leiningen/lein-swank "1.1.0"]]

  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
		 [clojure-http-client "1.0.0-SNAPSHOT"]
		 [org.apache.activemq/activemq-core "5.3.0"]
;		 [org.apache.activemq/activemq-xmpp "5.3.0"]
		 [org.clojars.the-kenny/clojure-couchdb "0.2.1"]
		 [swank-clojure "1.2.1"]
		 ])

;; Stuff we don't need to include UNLESS we remove one of the above.
;;		 [log4j "1.2.14"] - part of activemq
;;               [jmdns "1.0"] ; - part of activemq

;; Sample code using jetty, (is this the right one? or should it be org.eclipse.something...
;;	 [org.mortbay.jetty/jetty "6.1.21"]
;; (:import org.mortbay.jetty.Server
;;            [org.mortbay.jetty.servlet Context ServletHolder]
;;            org.mortbay.jetty.bio.SocketConnector
;;            org.mortbay.jetty.security.SslSocketConnector))
