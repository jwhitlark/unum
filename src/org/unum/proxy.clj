;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.proxy
;  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.seq-utils)
  (:use clojure.test)

  (:import (java.net ServerSocket InetAddress InetSocketAddress))

  (:use org.unum.net)
  (:use org.unum.datastore)
)

(def proxied-couchdb-port 10178)

(def proxy-from (InetSocketAddress. (my-unum-ip-address) proxied-couchdb-port))

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
