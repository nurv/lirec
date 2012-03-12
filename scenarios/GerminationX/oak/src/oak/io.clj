;; Copyright (C) 2010 FoAM vzw
;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.
;;
;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns oak.io
  (:use
   clojure.xml
   clojure.contrib.io)
  (:import
   java.io.File
   java.io.FileInputStream
   java.io.FileOutputStream
   java.io.PushbackReader
   java.io.FileReader
   java.io.BufferedReader
   java.io.InputStreamReader
   java.net.Socket
   java.nio.ByteBuffer
   java.nio.CharBuffer
   java.nio.channels.SocketChannel
   java.nio.charset.Charset
   java.io.IOException
   java.io.ByteArrayInputStream))

(defn parse-number [s]
  (try (Integer/parseInt (.trim s))
       (catch NumberFormatException e nil)))

(defn parse-float [s]
  (try (Float/parseFloat (.trim s))
       (catch NumberFormatException e nil)))

;(defn msg-waiting? [socket]
;  (> (. (. socket getInputStream) available) 0))

; (defn read-msg [socket]
;  (defn read-bytes [rdr count]
;   (let [result (. rdr read)]
;     (if (= count 1)
;       '()
;       (cons (char result) (read-bytes rdr (- count 1))))))
; (apply str (read-bytes
;             (. socket getInputStream)
;             (. (. socket getInputStream) available))))

;(defn send-msg [socket msg]
; (println msg)
; (try 
;   (let [aux (str msg "\n")
;         out (. socket getOutputStream)]
;     (. out write (. aux getBytes "UTF-8"))
;     (. out flush)
;     true)
;   (catch IOException e
;     (. e printStackTrace)
;     false)))

(comment
(defn serialise
  "Print a data structure to a file so that we may read it in later."
  [data-structure #^String filename]
  (with-out-writer
    (java.io.File. filename)
    (binding [*print-dup* true] (prn data-structure))))

;; This allows us to then read in the structure at a later time, like so:
(defn deserialise [filename]
  (with-open [r (PushbackReader. (FileReader. filename))]
    (read r))))

(comment (defn serialise [o filename]
  (with-open [outp (-> (java.io.File. filename) java.io.FileOutputStream. java.io.ObjectOutputStream.)]
    (.writeObject outp o)))

(defn deserialise [filename]
  (with-open [inp (-> (java.io.File. filename) java.io.FileInputStream. java.io.ObjectInputStream.)]
    (.readObject inp))))

(defn serialise [o filename]
  (println "hello")
  (spit filename o))

(defn deserialise [filename]
  (read-string (slurp filename)))

(def buf (ByteBuffer/allocateDirect 409600))

(defn read-msg [sc]
  (.clear buf)
  (let [r (.read sc buf)]
    (if (> r 0)
      (do
        (.flip buf)
        (let [bytearr (byte-array (.remaining buf))]
          (.get buf bytearr)
          (let [msg (new String bytearr)]
            ;(println "<-- " msg)
            msg)))
      false)))

(comment defn read-msg [reader]
  (defn _ []
    (println "inner reader")
    (let [s (.readLine reader)]
      (println (str "[" s "]"))
      (if (not s)
        ""
        (str s "\n" (_)))))
  (println "read-msg in")
  (_)
  (println "read-msg out"))

(comment defn read-msg [reader]
  (let [r (.readLine reader)]
     ;(println "<----------------- " r)
    r))

(defn send-msg [sc msg]
  ;(println "--> " msg)
  (let [msg (str msg "\n")
        enc (.newEncoder (Charset/forName "US-ASCII"))]  
    (.write sc (.encode enc (CharBuffer/wrap msg)))))

(defn load-object [fname]
  (reduce
   (fn [r t]
     (let [toks (.split t " ")]
       (assoc r (first toks) (second toks))))
   {"name" fname}
   (.split (slurp (str "data/objects/" fname ".txt")) "\r\n")))

; for each object, look for a file and parse it into a hash-map
(defn load-objects [objects]
  (map
   (fn [obj]
     (load-object obj))
   objects))

(defn parse-xml [s]
  (try
    (parse (ByteArrayInputStream. (.getBytes s "UTF-8"))) 
    (catch org.xml.sax.SAXParseException e
      (println (str "xml error with " s)))))