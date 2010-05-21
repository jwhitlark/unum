;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.proxy
;  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.logging)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.seq-utils)
  (:use clojure.contrib.server-socket)
  (:use clojure.test)

  (:import (java.net ServerSocket InetAddress InetSocketAddress)
	   (java.io IOException)
	   (java.net Socket)
	   (java.net InetSocketAddress)
	   (java.net SocketTimeoutException)
	   (java.net UnknownHostException))

  (:use org.unum.net)
  (:use org.unum.datastore)
)

(def proxied-couchdb-port 10178)

(def proxy-from (InetSocketAddress. (my-unum-ip-address) proxied-couchdb-port))

(def proxy-timeout 5000)
(def proxy-hostname "localhost")

(def proxyed-local-sockets (ref #{}))

(defn connect-to-local [port]
  "opens a socket to the given port on local host and adds the sock to the list of local connections"
  (let [sock-addr (InetSocketAddress. proxy-hostname port)
	sock (Socket.)]
    (try
     (. sock connect sock-addr proxy-timeout)
     (info "conected to " sock-addr)
     (catch IOException e false)
     (catch SocketTimeoutException e false)
     (catch UnknownHostException e false))
    (let [out (. sock getOutputStream)
	  in  (. sock getInputStream)]
      (dosync
       (commute proxyed-local-sockets conj sock))
      [in out])))

(defn forward [from to]
  (try
   (let [data (. from read)]
     (if (not= data -1)
       (do (. to write data)
	   (recur from to))))
   (catch IOException ioException (error ioException))))

(def couchdb-port 8080)

(defn forward-to-localhost [remote-in remote-out]
  (info "forwarding new connection from " remote-in)
  (let [[local-in local-out] (connect-to-local couchdb-port)
	remote-to-local (Thread. #(forward remote-in local-out))
	local-to-remote (Thread. #(forward local-in remote-out))]
    (info "starting remote-to-local")
    (. remote-to-local start)
    (info "starting local-to-remote")
    (. local-to-remote start)
    (info "waiting for threads to finish")
    (. remote-to-local join)
    (info "remote-to-local finished")
    (. local-to-remote join)
    (info "local-to-remote finished")
    (info "connection closed")))

(def proxy-server-sock (ref :not-running))

(defn get-proxyed-connections []
  "get all the local and remote sockets used by the proxy"
  (concat
   @(:connections @proxy-server-sock)
   @proxyed-local-sockets))

(defn kill-proxy-server []
  "proxy server runs in its own thread and must be killed before it will reliese the port"
   ; this is a concurrency bug. should be in dosync and use agetnt to close sockets
  (map #(. % close) (get-proxyed-connections))
  (. (:server-socket @proxy-server-sock) close)
  (dosync
   (ref-set proxy-server-sock :not-running)
   (ref-set proxyed-local-sockets #{})))


(defn proxy-server [listen-port]
  "either start a new socket server, or restart if it is already running"
  (dosync
   (if (not= @proxy-server-sock :not-running)
     (kill-proxy-server))
   (ref-set proxy-server-sock (create-server listen-port forward-to-localhost))))







(defn create-proxy-server-socket [from-addr]
  (let [sock (ServerSocket.)]
		  (do
		    (.bind sock from-addr)
		    sock)))


(defn run-proxy [server-socket]
  (loop [client-socket (.accept server-socket)]
    (let [in-data (read-lines (.getInputStream client-socket))]
      (print in-data))
    (recur (.accept server-socket))))

;; Test session, (so far)
;; (use 'clojure.contrib.duck-streams)
;; (def ss (create-proxy-server-socket proxy-from))
;; (def soc (.accept ss))
;; (def in-s (.getInputStream soc))
;; (def in-l (read-lines in-s))


;("GET / HTTP/1.1" "Host: 10.17.74.2:10178" "User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.7) Gecko/20100106 Ubuntu/9.10 (karmic) Firefox/3.5.7" "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" "Accept-Language: en-us,en;q=0.5" "Accept-Encoding: gzip,deflate" "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7" "Keep-Alive: 300" "Connection: keep-alive" "")


;; package proxyServer;

;; import java.io.BufferedReader;
;; import java.io.File;
;; import java.io.FileOutputStream;
;; import java.io.IOException;
;; import java.io.InputStream;
;; import java.io.InputStreamReader;
;; import java.io.PrintStream;
;; import java.net.ServerSocket;
;; import java.net.Socket;

;; public class MyProxyServer {

;; public static final int portNumber = 5555;

;; public static void main(String[] args){
;; MyProxyServer myProxyServer = new MyProxyServer();
;; myProxyServer.start();
;; }
;; public void start(){
;; System.out.println("Starting the MyProxyServer ...");
;; try {

;; ServerSocket serverSocket = new ServerSocket(MyProxyServer.portNumber,1);

;; /*File file = new File("test.txt");
;; FileOutputStream fileOutputStream = new FileOutputStream(file);

;; PrintStream printStream = new PrintStream(fileOutputStream);*/

;; while(true){
;; System.out.println("Inside while loop ");
;; Socket clientSocket = serverSocket.accept();
;; System.out.println("Connection to MyProxyServer is "+clientSocket.isConnected());

;; InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

;; BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
;; String command = bufferedReader.readLine();


;; //printStream.print(command);

;; bufferedReader.close();
;; //int i=bufferedReader.read();
;; //int i= clientSocket.getInputStream().read();
;; /*InputStream inputStream = clientSocket.getInputStream();
;;  while (true) {
;;  System.out.println("****");
;;          int i = inputStream.read();
;;          if (i == -1) break;
;;          System.out.println("\t\t\tInputThread");
;;          System.out.write(i);
;;        }

;;  inputStream.close();*/

;; System.out.println("Client has asked to ....\n"+command);

;; if(command.equals("Cancel")){
;; System.out.println("Shutting down the server ...");
;; break;
;; }


;; /*printStream.close();
;; fileOutputStream.close();*/
;; }



;; } catch (IOException e) {
;; // TODO Auto-generated catch block
;; e.printStackTrace();
;; }
;; }
;; }


;; corrections follow:
;; Hi Omkar

;; The other meta-data are passed as single lines (key: value)
;; after the GET/POST request. For intance:

;; GET http://www.google.com/ HTTP/1.1
;; Host: www.google.com
;; User-Agent: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.6) Gecko/20070810 Ubuntu/7.10 (gutsy) Firefox/2.0.0.6
;; ...

;; That means that the program should iterate over all the lines
;; following the GET/POST request.

;; You can do something like that:

;; while(true) {
;; System.out.println("Inside while loop ");
;; Socket clientSocket = serverSocket.accept();
;; System.out.println("Connection to MyProxyServer is "+clientSocket.isConnected());

;; InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

;; BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
;; String command = bufferedReader.readLine();

;; System.out.println("Client has asked to ....\n"+command);

;; if(command.equals("Cancel")){
;; System.out.println("Shutting down the server ...");
;; break;

;; }

;; // iterate over all the lines following the GET/POST request
;; String meta = null;
;; while((meta = bufferedReader.readLine()).length() > 0){
;; System.out.println(meta);
;; }

;; bufferedReader.close();
;; }
