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

(def tile-size 12)
(def season-length (* 60 10))
(def min-health 10)
(def max-health 90)
(def start-health 20)
(def min-neighbours 2)
(def max-neighbours 5)
(def seeds-duration (* 60 60 1000))

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

(def plant-types '("cherry" "apple" "aronia" "dandelion" "clover"))

(defn plant-type-id->name [type]
  (nth plant-types type))

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
   :state 'planted
   :picked-by-ids '()
   :owner-id owner-id
   :size size
   :timer 0
   :tick (+ (/ season-length 50) (Math/floor (rand 10)))
   :health start-health
   :fruit false
   :log (make-log 10)))

(defn plant-count [plant]
  (println (str "picked-by: " (count (:picked-by-ids plant)))))


(defn make-random-plant [id]
  (let [type (rand-nth plant-types)]
    (make-plant
     id
     (make-vec2 (Math/floor (rand tile-size))
                (Math/floor (rand tile-size)))
     type
     -1
     (Math/round (+ 1 (rand 10))))))

; the plant state machine, advance state, based on health
(defn adv-state [state health season annual]
  (cond
   (= state 'planted) 'grow-a
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
                            (if (< health min-health) 'ill-a 'decay-c))
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

(defn load-companion-rules [filename]
  (read-string (slurp filename)))

(defn get-relationship [from to rules]
  (nth (nth rules (plant-type->id from))
       (plant-type->id to)))

(defn plant-add-to-log [plant log type]
  (log-add-msg 
   log
   (make-msg
    (:id plant)
    (:type plant)
    (:owner-id plant)
    type
    ()
    'plant
    (:type plant))))

(defn plant-clear-log [plant]
  (modify
   :log
   (fn [log]
     (make-log 10))
   plant))

(defn plant-update-log [plant old-state]
  (if (not (= (:state plant) old-state))
    (modify
     :log
     (fn [log]
       (cond
        (and (= old-state 'planted)
             (= (:state plant) 'grow-a))
        (plant-add-to-log plant log 'i_have_been_planted)

        (and (= old-state 'decay-c)
             (= (:state plant) 'ill-a))
        (plant-add-to-log plant log 'i_am_ill)
        
       ; (and (= old-state 'decay-c)
       ;      (= (:state plant) 'grow-a))
       ; (plant-add-to-log plant log 'i_am_regrowing)
        
        (and (= old-state 'ill-c)
             (= (:state plant) 'decayed))
        (plant-add-to-log plant log 'i_have_died)

        (and (= old-state 'ill-c)
             (not (= (:state plant) 'ill-c)))
        (plant-add-to-log plant log 'i_have_recovered)

       ; (and (not (= old-state 'fruit-a))
       ;      (= (:state plant) 'fruit-a))
       ; (plant-add-to-log plant log 'i_have_fruited)

        :else log))
     plant)
    plant))

(defn plant-update-health [plant neighbours rules]
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
   plant))

(defn plant-update-fruit [plant]
  (modify
   :fruit
   (fn [f]
     (if (and (not f) (= (:state plant) 'fruit-c))
       true f))
   plant))

(defn plant-update-state [plant time delta season]
  (modify
   :timer
   (fn [timer]
     (+ timer delta))
   (if (> (:timer plant) (:tick plant))
     (modify
      :state
      (fn [state] (adv-state state
                             (:health plant)
                             season
                             ; for the moment assume cover plants
                             ; are annuals
                             (= (:layer plant) "cover")))
      (modify
       :timer (fn [t] 0) plant))
     plant)))

(defn plant-update [plant time delta neighbours rules season]
  ;(println (str season " " (:state plant) " " (:health plant) " " (:timer plant) " " (:tick plant)))
  (let [old-state (:state plant)]
    (plant-update-log
     (plant-update-health
      (plant-update-fruit
       (plant-update-state
        (plant-clear-log plant)
        time delta season))
      neighbours rules)
     old-state)))

; returns a list containing:
; { :needed_plants ( type-name1 type-name2 ... )
;   :harmful_plants ( plant1 plant2 ... ) }
(defn plant-diagnose [plant neighbours rules]
  {
   :harmful_plants
   (filter
    (fn [n]
      (< (get-relationship (:type plant) (:type n) rules) 0))
    neighbours)
   :needed_plants
   (reduce
    (fn [r i]
      (if (> (first i) 0)
        (cons (second i) r)
        r))
    '()
    (map
     (fn [v t]
       (list v t))
     (nth rules (plant-type->id (:type plant)))
     plant-types))
   })

(defn plant-picked [plant player]
  (modify
   :log
   (fn [log]
     (log-add-msg 
      log
      (make-msg
       (:id plant)
       (:type plant)
       (:owner-id plant)
       'i_have_been_picked_by
       (list (:name player))
       'player
       (:fbid player))))
   (modify :fruit (fn [f] false) plant)))