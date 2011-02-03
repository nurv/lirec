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
   oak.vec2
   oak.remote-agent
   oak.forms)
  (:require
   clojure.contrib.math))

(defrecord spirit
  [tile
   pos
   name])

(defn make-spirit [remote-agent]
  (println (str "creating spirit for " (remote-agent-name remote-agent)))
  (spirit.
   (make-vec2 0 0)
   (make-vec2 0 0)
   (remote-agent-name remote-agent)))
  
(defn spirit-update [spirit remote-agent]
  (comment println (:name spirit))
  (comment println (remote-agent-done remote-agent))
  (comment println (map
            (fn [emotion]
              (if (= (:tag emotion) :Mood)
                (list "Mood" (:content emotion))
                (let [e (:attrs emotion)]
                  (list (:type e) (:cause e)))))
            (:content (remote-agent-emotions remote-agent))))
  (comment println
   (map
    (fn [relation]
      (list (:tag relation)
            (map
             (fn [chunk]
               (list (:tag chunk) (:content chunk)))
             (:content relation))))
    (:content (remote-agent-relations remote-agent))))
  spirit)
