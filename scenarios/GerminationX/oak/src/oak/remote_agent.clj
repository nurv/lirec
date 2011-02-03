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

(ns oak.remote-agent
  (:use oak.io)
  (:import
   java.net.Socket
   java.io.File
   java.io.IOException
   java.util.Random
   java.io.BufferedReader
   java.io.InputStreamReader
   FAtiMA.util.parsers.SocketListener
   FAtiMA.autobiographicalMemory.AutobiographicalMemory))

(defstruct remote-agent
  :properties
  :name
  :role
  :display-name
  :socket
  :relations
  :emotions
  :said
  :done
  :random
  :reader)

(def remote-agent-properties (accessor remote-agent :properties))
(def remote-agent-name (accessor remote-agent :name))
(def remote-agent-role (accessor remote-agent :role))
(def remote-agent-display-name (accessor remote-agent :display-name))
(def remote-agent-socket (accessor remote-agent :socket))
(def remote-agent-relations (accessor remote-agent :relations))
(def remote-agent-emotions (accessor remote-agent :emotions))
(def remote-agent-said (accessor remote-agent :said))
(def remote-agent-done (accessor remote-agent :done))
(def remote-agent-random (accessor remote-agent :random))
(def remote-agent-reader (accessor remote-agent :reader))

(defn remote-agent-add-property [agent property]
  (merge agent {:properties (cons property (remote-agent-properties agent))})) 

(defn make-remote-agent [socket world]
  ;(.configureBlocking socket false)
  ;(. (AutobiographicalMemory/GetInstance) setSelf name)
  (let [reader (BufferedReader.
                (InputStreamReader.
                 (.getInputStream (.socket socket))))
        toks (.split (read-msg socket) " ")]
    (send-msg socket "OK")
    (struct
     remote-agent
     (reduce
      (fn [r prop]
        (let [tv (.split prop ":")]
          (assoc r (first tv) (second tv))))
      {}
      (nthnext toks 3))
     (nth toks 0)
     (nth toks 1)
     (nth toks 2)
     socket
     "none yet"
     "none yet"
     '()
     '()
     (new java.util.Random)
     reader
     )))

