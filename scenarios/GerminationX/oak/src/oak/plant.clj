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
   oak.forms
   oak.log)
  (:require
   clojure.contrib.math))

(def season-length (* 60 3))
(def min-health 10)
(def max-health 90)
(def start-health 20)
(def min-neighbours 2)
(def max-neighbours 5)

(defn plant-type->layer [type]
  (cond
   (= type "dandelion") "cover"
   (= type "clover") "cover"
   (= type "aronia") "shrub"
   (= type "apple") "tree"
   (= type "cherry") "tree"))

(defn plant-type->id [type]
  (cond
   (= type "cherry") 0
   (= type "apple") 1
   (= type "plant-002") 1 ; temp apple tree
   (= type "aronia") 2
   (= type "plant-003") 2 ; temp aronia
   (= type "dandelion") 3
   (= type "plant-001") 3 ; temp dandelion
   (= type "clover") 4))

(defn layer->spirit-name [layer]
  (cond
   (= layer "canopy") "CanopySpirit"
   (= layer "vertical") "VerticalSpirit"
   (= layer "cover") "CoverSpirit"
   (= layer "tree") "TreeSpirit"
   (= layer "shrub") "ShrubSpirit"
   :else "UnknownSpirit"))

(defn make-plant [id pos type owner-id size]
  (hash-map
   :version 1
   :id id
   :pos pos
   :type type
   :layer (plant-type->layer type)
   :state 'grow-a
   :picked-by-ids '()
   :owner-id owner-id
   :size size
   :timer 0
   :tick (+ (/ season-length 50) (Math/floor (rand 10)))
   :health start-health
   :fruit false
   :log (make-log
         (list (make-msg id owner-id 'i-have-been-created ())))))

(defn plant-count [plant]
  (println (str "picked-by: " (count (:picked-by-ids plant)))))


(defn make-random-plant [id]
  (let [type (rand-nth (list "aronia" "dandelion" "apple" "cherry" "clover"))]
    (make-plant
     id
     (make-vec2 (Math/floor (rand 15)) (Math/floor (rand 15)))
     type
     -1
     (Math/round (+ 1 (rand 10))))))

; the plant state machine, advance state, based on health
(defn adv-state [state health season annual]
  (cond
   (= state 'grow-a) (cond (> health min-health) 'grow-b :else (rand-nth (list 'grow-a 'grow-b)))
   (= state 'grow-b) (cond (> health min-health) 'grow-c :else (rand-nth (list 'grow-b 'grow-c)))
   (= state 'grow-c) (cond (> health min-health) 'grown :else (rand-nth (list 'grow-c 'grown)))
   (= state 'grown) (cond
                     (and (> health max-health)
                          (or (= season 'spring)
                              (= season 'summer)))
                     'fruit-a
                     (or (= season 'autumn) (= season 'winter)
                         (< health min-health))
                     'decay-a
                     :else 'grown)
   (= state 'fruit-a) (if (< health min-health) 'decay-a 'fruit-b)
   (= state 'fruit-b) (if (< health min-health) 'decay-a'fruit-c)
   (= state 'fruit-c) (if (or (= season 'autumn) (= season 'winter)
                              (< health min-health))
                        'decay-a 'grown)
   (= state 'decay-a) 'decay-b
   (= state 'decay-b) 'decay-c
   (= state 'decay-c) (cond (and (or (= season 'spring) (= season 'summer))
                                 (> health min-health))
                            (if annual 'grow-a 'grown)
                            :else
                            (if (< health min-health) 'ill-c 'decay-c))
   (= state 'ill-c) (cond (< health min-health) 'decayed
                (> health max-health) 'grown
                :else 'ill-c)
   (= state 'decayed) 'decayed))

(defn load-companion-rules [filename]
  (read-string (slurp filename)))

(defn get-relationship [from to rules]
  (nth (nth rules (plant-type->id from))
       (plant-type->id to)))

(defn plant-add-to-log [plant log type]
  (make-log
   (list
    (make-msg
     (:id plant)
     (:owner-id plant)
     type
     ()))))

(defn plant-update-log [plant old-state]
  (if (not (= (:state plant) old-state))
    (modify
     :log
     (fn [log]
       (cond
        (and (not (= old-state 'ill-c))
             (= (:state plant) 'ill-c))
        (plant-add-to-log plant log 'i-am-ill)
        
        (and (= old-state 'decay-c)
             (= (:state plant) 'grow-a))
        (plant-add-to-log plant log 'i-am-regrowing)
        
        (and (= old-state 'ill-c)
             (not (= (:state plant) 'ill-c)))
        (plant-add-to-log plant log 'i-have-recovered)
        
        (and (not (= old-state 'fruit-a))
             (= (:state plant) 'fruit-a))
        (plant-add-to-log plant log 'i-have-fruited)

        :else (make-log ())))
     plant)
    plant))

(defn plant-update [plant time delta neighbours rules season]
;  (println (str season " " (:state plant) " " (:health plant) " " (:timer plant) " " (:tick plant)))
  (modify
   :health
   (fn [health]
     health (max 0 (min 100
                        (+ health
                           (reduce
                            (fn [r n]
                              (+ r (get-relationship
                                    (:type plant) (:type n) rules)))
                            (if (empty? neighbours) -1 1)
                            neighbours)))))
   (modify
    :fruit
    (fn [f]
      (if (and (not f) (= (:state plant) 'fruit-c))
        true f))
    (modify
     :timer
     (fn [timer]
       (+ timer delta))
     (if (> (:timer plant) (:tick plant))
       (let [old-state (:state plant)]
         (plant-update-log
          (modify
           :state
           (fn [state] (adv-state state
                                  (:health plant)
                                  season
                                 ; for the moment assume cover plants
                                 ; are annuals
                                  (= (:layer plant) "cover")))
           (modify
            :timer (fn [t] 0) plant)) old-state))
         plant)))))

