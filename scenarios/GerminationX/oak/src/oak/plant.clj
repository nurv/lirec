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

(ns oak.plant
  (:use
   oak.vec2)
  (:require
   clojure.contrib.math))

(defrecord plant
  [pos
   type
   layer
   state
   picked-by
   owner
   size])

(defn plant-pos [plant] (:pos plant))
(defn plant-type [plant] (:type plant))
(defn plant-layer [plant] (:layer plant))
(defn plant-state [plant] (:state plant))
(defn plant-picked-by [plant] (:picked-by plant))
(defn plant-owner [plant] (:owner plant))
(defn plant-size [plant] (:size plant))

(defn make-plant [pos type owner size]
  (plant. pos type "" "" '() owner size))

(defn make-random-plant []
  (make-plant
   (make-vec2 (Math/round (rand 10)) (Math/round (rand 10)))
   (rand-nth '("plant-001"
               "plant-002"
               "plant-003"
               "plant-004"
               "plant-005"
               "plant-006"
               "plant-007"
               "plant-008"))
   "germination x"
   (Math/round (+ 50 (rand 100)))))