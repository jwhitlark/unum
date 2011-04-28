(defproject unum "0.1.0"  ;;SEE: http://semver.org/ for versioning strategy
  :description "From many, one."
  :main org.unum
  :url "https://github.com/jwhitlark/unum"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [org.apache.activemq/activemq-core "5.4.0"]
		 [org.apache.activemq/activemq-camel "5.4.0"]
		 ;;		 [activemq/activemq-transport-xstream "2.1"]
		 [com.thoughtworks.xstream/xstream "1.3.1"]
		 [org.apache.camel/camel-core "2.4.0"]
		 [clojure-couchdb "0.4.5"]
		 [swank-clojure "1.2.1"]
		 ])
