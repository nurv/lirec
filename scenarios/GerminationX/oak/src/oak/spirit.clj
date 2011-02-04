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


(ns oak.spirit
  (:use
   oak.io
   oak.vec2
   oak.remote-agent
   oak.forms
   oak.tile)
  (:require
   clojure.contrib.math))

(defrecord spirit
  [tile
   pos
   name
   emotions
   actions])

(defn make-spirit [remote-agent]
  (println (str "creating spirit for " (remote-agent-name remote-agent)))
  (spirit.
   (make-vec2 0 0)
   (make-vec2 0 0)
   (remote-agent-name remote-agent)
   '() '()))

; convert foofooname#999# to 999
(defn fatima-name->id [name]
  (if (.contains name "#")
    (parse-number (.substring name
                              (+ 1 (.indexOf name "#"))
                              (.lastIndexOf name "#")))
    false))

(defn spirit-update [spirit remote-agent tile]
; for the moment take a straight copy of actions and emotions
  (let [spirit (modify :emotions
                       (fn [emotions]
                         (remote-agent-emotions remote-agent))
                       (modify :actions
                               (fn [actions]
                                 (remote-agent-done remote-agent))
                               spirit))]
    
    ; if we have some actions
    (if (not (empty? (:actions spirit)))
      (let [latest-action (first (:actions spirit))
            latest-subject (nth (.split (:msg latest-action) " ") 1)
            id (fatima-name->id latest-subject)]
        (if id
          (let [e (tile-find-entity tile id)]
            (if e
              (modify :pos (fn [pos] (:pos e)) spirit)
              spirit))
          spirit))
      spirit)))
            
          
(comment println
         (map
          (fn [relation]
            (list (:tag relation)
                  (map
                   (fn [chunk]
                     (list (:tag chunk) (:content chunk)))
                   (:content relation))))
          (:content (remote-agent-relations remote-agent))))

  