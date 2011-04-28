(defproject unum "0.1.0"  ;;SEE: http://semver.org/ for versioning strategy
  :description "From many, one."
  :main org.unum
  :url "https://github.com/jwhitlark/unum"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [org.apache.activemq/activemq-core "5.5.0"]
		 [org.apache.activemq/activemq-camel "5.5.0"]
		 ;;		 [activemq/activemq-transport-xstream "2.1"]
		 [com.thoughtworks.xstream/xstream "1.3.1"]
		 [org.apache.camel/camel-core "2.7.0"]
		 ;; Perhaps use clutch instead?
		 [clojure-couchdb "0.4.5"]
		 [org.slf4j/slf4j-api "1.6.1"]
		 [org.slf4j/slf4j-simple "1.6.1"]
		 [swank-clojure "1.4.0-SNAPSHOT"]
		 ])
