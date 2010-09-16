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
  (:import
   java.net.Socket
   java.nio.ByteBuffer
   java.nio.CharBuffer
   java.nio.channels.SocketChannel
   java.nio.charset.Charset
   java.io.IOException
   java.io.ByteArrayInputStream)
  (use clojure.xml))

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

(def buf (ByteBuffer/allocateDirect 4096))

(defn read-msg [sc]
  (.clear buf)
  (let [r (.read sc buf)]
    (if (> r 0)
      (do
        (.flip buf)
        (let [bytearr (byte-array (.remaining buf))]
          (.get buf bytearr)
          (new String bytearr)))
      false)))
  
(defn send-msg [sc msg]
  (println "sending:" msg)
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

(defn parse-xml [str]
  (parse (ByteArrayInputStream. (.getBytes str "UTF-8"))))