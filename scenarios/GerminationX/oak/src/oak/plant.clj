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
   oak.log
   oak.defs)
  (:require
   clojure.contrib.math))

(defn make-plant [id tile pos type owner-id size]
  (hash-map
   :version 1
   :id id
   :tile tile
   :pos pos
   :type type
   :layer (plant-type->layer type)
   :state "planted"
   :picked-by-ids ()
   :owner-id owner-id
   :size size
   :timer 9999 ; force a tick when created
   :tick (+ plant-tick (Math/floor (rand plant-tick-var)))
   :health start-health
   :fruit 0
   :event-occurred ()
   :log (make-log 10)))

(defn plant-strip
  "remove crud that the client doesn't need - temporary measure"
  [plant player-layer]
  (let [stripped (select-keys
                  plant
                  [:id :state :type :layer :pos :fruit :owner-id])]
    ; remove the fruit if the player hasn't reached the level yet
    (if (or (= player-layer "all")
            (= player-layer (:layer plant)))
      stripped
      (merge stripped {:fruit 0}))))

(defn plant-count [plant]
  (println (str "picked-by: " (count (:picked-by-ids plant)))))

(defn make-random-plant [id tile]
  (let [type (rand-nth plant-types-wo-fungi)]
    (make-plant
     id
     tile
     (make-vec2 (Math/floor (rand tile-size))
                (Math/floor (rand tile-size)))
     type
     (rand-nth (list 97 98 99))
     (Math/round (+ 1 (rand 10))))))

(defn adv-state
  "the plant state machine, advance state, based on health and season"
  [state health season annual fungi]
  (cond
   (= state "planted") "grow-a"
   (= state "grow-a") (cond (> health min-health) "grow-b" :else (rand-nth (list "grow-a" "grow-b")))
   (= state "grow-b") (cond (> health min-health) "grow-c" :else (rand-nth (list "grow-b" "grow-c")))
   (= state "grow-c") (cond (> health min-health) "grown" :else (rand-nth (list "grow-c" "grown")))
   (= state "grown")
   (if fungi
     (cond
      (< health min-health) "decayed"
      :else "grown")
     (cond
      (< health min-health) "ill-a" 
      (and (> health max-health)
           (or (= season "spring")
               (= season "summer")))
      "fruit-a"
      (or (= season "autumn") (= season "winter"))
      "decay-a"
      :else "grown"))
   (= state "fruit-a") (if (< health min-health) "decay-a" "fruit-b")
   (= state "fruit-b") (if (< health min-health) "decay-a" "fruit-c")
   (= state "fruit-c") (if (or (= season "autumn") (= season "winter")
                              (< health min-health))
                        "decay-a" "grown")
   (= state "decay-a") (if (< health min-health) "ill-a" "decay-b")
   (= state "decay-b") (if (< health min-health) "ill-a" "decay-c")
   (= state "decay-c") (cond (and (or (= season "spring") (= season "summer"))
                                 (> health min-health))
                            (if annual "grow-a" "grown")
                            :else
                            (if (< health min-health) "ill-a" "decay-c"))
   (= state "ill-a") (cond (< health min-health) "ill-b"
                (> health max-health) "grown"
                :else "ill-a")
   (= state "ill-b") (cond (< health min-health) "ill-c" 
                (> health max-health) "ill-a"
                :else "ill-b")
   (= state "ill-c") (cond (< health min-health) "decayed"
                (> health max-health) "ill-b"
                :else "ill-c")
   (= state "decayed") "decayed"))

(defn load-companion-rules [filename]
  (read-string (slurp filename)))

(defn get-relationship [from to rules]
  (nth (nth rules (plant-type->id from))
       (plant-type->id to)))

(defn plant-add-to-log
  "helper to add a message to a plant:s log"
  [plant log type]
  (log-add-msg 
   log
   (make-plant-msg type plant (:owner-id plant) ())))

(defn plant-add-to-log-extra
  "helper to add a message to a plant:s log"
  [plant log type extra]
  (log-add-msg 
   log
   (make-plant-msg type plant (:owner-id plant) extra)))

(defn plant-clear-log
  "clear the things needed before an update"
  [plant]
  (modify
   :log
   (fn [log]
     (make-log 10))
   plant))

(defn plant-clear-events
  "clear the things needed before an update"
  [plant]
  (modify
   :event-occurred
   (fn [ev] ())
   plant))

(defn neighbours-relationship
  "look for neighbours and see if we will help or hinder
   them from recovering - return list of plants based on comp fn"
  [plant neighbours rules comp]
  (filter
   (fn [other]
     (comp (get-relationship (:type plant) (:type other) rules) 0))
   neighbours))


(defn ill-neighbours-relationship
  "look for ill neighbours and see if we will help or hinder
   them from recovering - return list of plants based on comp fn"
  [plant neighbours rules comp]
  (filter
   (fn [other]
     (and
      (or
       (= (:state other) "ill-a")
       (= (:state other) "ill-b")
       (= (:state other) "ill-c"))
      (comp (get-relationship (:type plant) (:type other) rules) 0)))
   neighbours))

(defn log-relationship
  "sends a message to both sides of the relationship"
  [log plant plants to-me to-other]
  (reduce
   (fn [log other]
     (log-add-msg 
      (log-add-msg 
       log
       (make-plant-msg ; to them
        to-other other (:owner-id other)
        (list (:owner-id plant) (:type plant))))
      (make-plant-msg ; to us
       to-me plant (:owner-id plant)
       (list (:owner-id other) (:type other)))))
   log
   plants))

(defn log-thank-owners
  "send message to owners of all plants
   thanking them for recovery"
  [log plant helpful-neighbours]
  (reduce
   (fn [log other]
     (log-add-msg 
      log
      (make-plant-msg
       :thanks_for_helping
       plant (:owner-id other)
       (list (:type other)))))
   log
   helpful-neighbours))

(defn plant-update-log
  "adds messages to the log depending on changing state"
  [plant old-state neighbours rules]    
    (modify
     :log
     (fn [log]
       (cond
        
        ; when first planted, need to inform owners of plants
        ; around us about our relationship with them
        (and (= old-state "planted")
             (= (:state plant) "grow-a"))
        (do
          (println "WAKEY WAKEY!!!")
          (plant-add-to-log
           plant
           (log-relationship
            (log-relationship
             log plant
             (ill-neighbours-relationship plant neighbours rules >)
             :i_am_beneficial_to
             :i_am_benefitting_from)
            plant
            (ill-neighbours-relationship plant neighbours rules <)
            :i_am_detrimental_to
            :i_am_detrimented_by)
           :i_have_been_planted))
          
        (and
         (not (= old-state "ill-a"))
         (not (= old-state "ill-b"))
         (= (:state plant) "ill-a"))
        (plant-add-to-log plant log :i_am_ill)
        
       ; (and (= old-state "decay-c")
       ;      (= (:state plant) "grow-a"))
       ; (plant-add-to-log plant log :i_am_regrowing)
        
        (and (= old-state "ill-c")
             (= (:state plant) "decayed"))
        (plant-add-to-log plant log :i_have_died)

        (and (= old-state "ill-a")
             (= (:state plant) "grown"))
        (log-thank-owners
         (plant-add-to-log plant log :i_have_recovered)
         plant
         (neighbours-relationship plant neighbours rules >))

        (and (not (= old-state "fruit-a"))
             (= (:state plant) "fruit-a"))
        ; we use this message to count flowered plants
        ; so add a little extra detail we need
        (plant-add-to-log-extra plant log :i_have_flowered
                                (list (:id plant))) 

        (or
         (and
          (= old-state "ill-c")
          (= (:state plant) "ill-b"))
         (and
          (= old-state "ill-b")
          (= (:state plant) "ill-a")))
        (plant-add-to-log plant log :i_am_recovering)
        
;        (and (not (= old-state :fruit-a))
;             (= (:state plant) :fruit-a))
;        (plant-add-to-log plant log :i_have_fruited)
      
        :else log))
     plant))

(defn events-from-relationship
  "append new relationship events onto the list"
  [events-list plant type plants]
  (reduce
   (fn [r other]
     (cons (str (:layer other) "-" type "#" (:id plant)) r))
   events-list
   plants))

(defn plant-update-events [plant old-state neighbours rules]    
  "add any special events that we need FAtiMA to be aware of"
  (modify
   :event-occurred
   (fn [ev]
     (cond
      ; when first planted, need to tell fatima about
      ; our relationships with the plants around us
      (and (= old-state "planted")
           (= (:state plant) "grow-a"))
      (events-from-relationship
       (events-from-relationship
        ev plant "benefit"
        (neighbours-relationship plant neighbours rules >))
       plant "detriment"
       (neighbours-relationship plant neighbours rules <))
      
      (and
       (= old-state "ill-c")
       (= (:state plant) "ill-b"))
      (cons (str (:layer plant) "-recovery-to-b#" (:id plant)) ev)
      
      (and
       (= old-state "ill-b")
       (= (:state plant) "ill-a"))
      (cons (str (:layer plant) "-recovery-to-a#" (:id plant)) ev)
      
      (and (= old-state "ill-a")
           (= (:state plant) "grown"))
      (cons (str (:layer plant) "-finished-recovery#" (:id plant)) ev)
      
      :else ev))
   plant))

(defn plant-update-from-changes
  "update the log and event-occurred from the
   current state and the last"
  [plant old-state neighbours rules]
  ; only if the state has acually changed
  (if (not (= (:state plant) old-state))
    ; update the log
    (plant-update-log
     (plant-update-events
      plant old-state neighbours rules)
     old-state neighbours rules)
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
     (if (= (:state plant) "fruit-c")
       (min max-fruit (+ f 1)) f))
   plant))

(defn plant-update-state [plant time delta season]
  (modify
   :timer
   (fn [timer]
     (+ timer delta))
   (if (> (:timer plant) (:tick plant))
     (modify
      :state
      (fn [state]
        (adv-state state
                   (:health plant)
                   season
                   ; for the moment assume cover plants
                   ; are annuals
                   (= (:layer plant) "cover")
                   (= (:layer plant) "fungi")))
      (modify
       :timer (fn [t] 0) plant))
     plant)))

(defn plant-update [plant time delta neighbours rules season]
  ;(println (str season " " (:state plant) " " (:health plant) " " (:timer plant) " " (:tick plant)))
  (let [old-state (:state plant)]
    (plant-update-from-changes
     (plant-update-health
      (plant-update-fruit
       (plant-update-state
        plant
        time delta season))
      neighbours rules)
     old-state neighbours rules)))

(defn plant-diagnose
 "returns a list containing:
  { :needed_plants ( type-name1 type-name2 ... )
    :harmful_plants ( plant1 plant2 ... ) }"
  [plant neighbours rules]
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
       ; don't want to suggest fungi here
       (if (= (plant-type->layer t) "fungi")
         (list 0 t)
         (list v t)))
     (nth rules (plant-type->id (:type plant)))
     plant-types))
   })

(defn plant-picked
  "send a message to our owner when we have been picked"
  [plant player]
  (modify
   :log
   (fn [log]
     (log-add-msg 
      log
      (make-plant-msg
       :i_have_been_picked_by
       plant (:owner-id plant)
       (list (:name player)))))
   (modify :fruit (fn [f] (- f 1)) plant)))