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
   oak.vec2
   oak.forms)
  (:require
   clojure.contrib.math))

(def min-health 10)
(def max-health 90)
(def start-health 20)
(def min-neighbours 2)
(def max-neighbours 5)

(defrecord plant
  [id
   pos
   type
   layer
   state
   picked-by
   owner
   size
   timer
   tick
   health])
  
(defn plant-pos [plant] (:pos plant))
(defn plant-type [plant] (:type plant))
(defn plant-layer [plant] (:layer plant))
(defn plant-state [plant] (:state plant))
(defn plant-picked-by [plant] (:picked-by plant))
(defn plant-owner [plant] (:owner plant))
(defn plant-size [plant] (:size plant))

(defn plant-type->layer [type]
  (cond
   (= type "plant-001") "cover"
   (= type "plant-002") "canopy"
   (= type "plant-003") "vertical"))
  
(defn make-plant [pos type owner size]
  (plant. (generate-id) pos type (plant-type->layer type)
          'grow-a '() owner size 0 (+ 30 (Math/floor (rand 10))) start-health))

(defn make-random-plant []
  (make-plant
   (make-vec2 (Math/floor (rand 15)) (Math/floor (rand 15)))
   (rand-nth (list "plant-001" "plant-002" "plant-003"))
   "the garden"
   (Math/round (+ 50 (rand 100)))))

; the plant state machine, advance state, based on health
(defn adv-state [state health]
  (cond
   (= state 'grow-a) (cond (> health min-health) 'grow-b :else (rand-nth (list 'grow-a 'grow-b)))
   (= state 'grow-b) (cond (> health min-health) 'grow-c :else (rand-nth (list 'grow-b 'grow-c)))
   (= state 'grow-c) (cond (> health min-health) 'grown :else (rand-nth (list 'grow-c 'grown)))
   (= state 'grown) (cond
           (> health max-health) (rand-nth (list 'grown 'fruit-a))
           (< health min-health) (rand-nth (list 'grown 'ill-a))
           :else 'grown)
   (= state 'decay-a) (cond (> health max-health) 'decay-a :else 'decay-b)
   (= state 'decay-b) (cond (> health max-health) 'decay-b :else 'decay-c)
   (= state 'decay-c) (cond (> health max-health) 'decay-c :else 'decayed)
   (= state 'fruit-a) 'fruit-b
   (= state 'fruit-b) 'fruit-c
   (= state 'fruit-c) 'grown
   (= state 'ill-a) (cond (< health min-health) 'ill-b
                (> health max-health) 'grown
                :else 'ill-a)
   (= state 'ill-b) (cond (< health min-health) 'ill-c
                (> health max-health) 'ill-a
                :else 'ill-b)
   (= state 'ill-c) (cond (< health min-health) 'decayed
                (> health max-health) 'ill-b
                :else 'ill-c)
   (= state 'decayed) 'decayed))

(defn plant-update [plant time delta neighbours]
  (modify
   :health
   (fn [health]
     (cond
      (< (count neighbours) min-neighbours) (max 0 (- health 1))
      (> (count neighbours) max-neighbours) (max 0 (- health 1))
      (= (:state plant) 'fruit-c) (max 0 (- health 10))
      :else (min 100 (+ health 1))))
   (modify
    :timer
    (fn [timer]
      (+ timer delta))
    (if (> (:timer plant) (:tick plant))
      (modify
       :state
       (fn [state] (adv-state state (:health plant)))
       (modify
        :timer (fn [t] 0) plant))
      plant))))

