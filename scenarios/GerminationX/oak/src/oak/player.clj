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
;; along with this program.  If not see <http://www.gnu.org/licenses/>.

(ns oak.player
    (:use
     oak.log
     oak.defs
     oak.forms))

(defn make-player [id name fbid]
  (hash-map
   :version 1
   :id id
   :fbid fbid
   :name name ; only for login - do not use directly
   :layer 0
   :seeds '()
   :messages '()
   :seeds-capacity 3
   :seeds-left 10
   :next-refresh 0
   :picked-by '()
   :has-picked '()
   :plant-count 0
   :flowered-plants ()
   :log (make-log 10)))

(defn player-inc-plant-count [player]
  (modify
   :plant-count
   (fn [c]
     (+ c 1))
   player))

(defn player-list-find-player-id [player-list name]
  (reduce
   (fn [r player]
     (if (and (not r) (= name (:name player)))
       (:id player) r))
   false
   player-list))

(defn player-list-find-player [player-list id]
  (reduce
   (fn [r player]
     (if (and (not r) (= id (:id player)))
       player r))
   false
   player-list))

(defn player-list-id->player-name [player-list id]
  (:name (player-list-find-player player-list id)))

(defn player-remove-fruit [player fruit-id]
  (modify
   :seeds
   (fn [seeds]
     (filter
      (fn [seed]
        (not (= (:id seed) fruit-id)))
      seeds))
   player))

(defn player-has-fruit? [player fruit-id]
  (modify
   :seeds
   (fn [seeds]
     (reduce
      (fn [r seed]
        (if (and (not r) (= (:id seed) fruit-id))
          true r))
      false
      seeds))
   player))

(defn player-get-fruit [player fruit-id]
  (reduce
   (fn [r seed]
     (if (and (not r) (= (:id seed) fruit-id))
       seed r))
   false
   (:seeds player)))

(defn player-update-seeds
  "update the player's seed picking ability"
  [player]
  (if (and
       (not (= (:next-refresh player) 0))
       (> (current-time) (:next-refresh player)))
    (modify
     :seeds-left
     (fn [s] (:seeds-capacity player))
     (modify :next-refresh (fn [r] 0) player))
    player))

(defn player-update [player]
  (let [leveledup-player 
        (modify
         :layer
         (fn [layer]
           (let [score (count (:flowered-plants player))]
             (cond
              (and (= layer 0) (> score level0up)) 1 ; cover -> shrub
              (and (= layer 1) (> score level1up)) 2 ; shrub -> tree
              (and (= layer 2) (> score level2up)) 3 ; tree -> all
              :else layer))) 
         (modify
          :flowered-plants
          (fn [fp]
            (reduce
             (fn [fp msg]
               (set-cons (first (:extra msg)) fp))
             fp
             (log-find-msgs (:log player) "i_have_flowered")))
          player))]
    
;    (modify ; add surprise seeds on levelup
;     :seeds
;     (fn [seeds]
;       (cond
;        (and (= (:layer player) 0) (= (:layer new-player) 1))
;        (cons (make-plant 
;        (and (= (:layer player) 1) (= (:layer new-player) 2))
;         (log-add-note log (make-note "levelup2" ()))
;        :else seeds)
       
    (modify ; add notes on levelup
     :log 
     (fn [log]
       (log-remove-msgs
        (cond
         (and (= (:layer player) 0) (= (:layer leveledup-player) 1))
         (log-add-note log (make-note "levelup1" ())) 
         (and (= (:layer player) 1) (= (:layer leveledup-player) 2))
         (log-add-note log (make-note "levelup2" ()))
         (and (= (:layer player) 2) (= (:layer leveledup-player) 3))
         (log-add-note log (make-note "levelup3" ()))
         :else log)
        "i_have_flowered"))
     leveledup-player)))
      