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
  (:use
   oak.vec2
   oak.io)
  (:import
   java.net.Socket
   java.io.File
   java.io.IOException
   java.util.Random
   java.io.BufferedReader
   java.io.InputStreamReader))

(defn remote-agent-properties [remote-agent] (:properties remote-agent))
(defn remote-agent-name [remote-agent] (:name remote-agent))
(defn remote-agent-role [remote-agent] (:role remote-agent))
(defn remote-agent-display-name [remote-agent] (:display-name remote-agent))
(defn remote-agent-socket [remote-agent] (:socket remote-agent))
(defn remote-agent-relations [remote-agent] (:relations remote-agent))
(defn remote-agent-emotions [remote-agent] (:emotions remote-agent))
(defn remote-agent-said [remote-agent] (:said remote-agent))
(defn remote-agent-done [remote-agent] (:done remote-agent))
(defn remote-agent-random [remote-agent] (:random remote-agent))
(defn remote-agent-reader [remote-agent] (:reader remote-agent))
(defn remote-agent-tile [remote-agent] (:tile remote-agent))

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
    (hash-map
     :properties (reduce
                  (fn [r prop]
                    (let [tv (.split prop ":")]
                      (assoc r (first tv) (second tv))))
                  {}
                  (nthnext toks 3))
     :name (nth toks 0)
     :role (nth toks 1)
     :display-name (nth toks 2)
     :socket socket
     :relations "none yet"
     :emotions "none yet"
     :said '()
     :done '()
     :random (new java.util.Random)
     :reader reader
     :tile (make-vec2 0 0)
     :delayed-msgs [])))

