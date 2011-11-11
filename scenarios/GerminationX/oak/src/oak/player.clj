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
     oak.forms
     oak.fruit))

(defn make-player [id name fbid]
  (hash-map
   :version 1
   :id id
   :fbid fbid
   :name name ; only for login - do not use directly
   :layer 0
   :seeds '()
   :messages '()
   :seeds-capacity 5
   :seeds-left 5
   :next-refresh 0
   :picked-by '()
   :has-picked '()
   :plant-count 0
   :flowered-plants ()
   :log (log-add-note
         (make-log 20)
         (make-note "welcome" (list "ok")))))

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
  (if (> (current-time) (:next-refresh player))
    (modify
     :seeds-left
     (fn [s]
       (min (+ 1 s) (:seeds-capacity player)))
     (modify :next-refresh (fn [r] (+ (current-time) (* 5 60 1000))) player))
    player))

(defn player-get-allowed-layer
  "map the level to the layer allowed"
  [player]
  (cond
   (= (:layer player) 0) "cover"
   (= (:layer player) 1) "shrub"
   (= (:layer player) 2) "tree"
   :else "all"))

(defn player-levelup [player]
  (modify
   :layer
   (fn [layer]
     (let [score (count (:flowered-plants player))]
       (cond
        (and (= layer 0) (>= score level0up)) 1 ; cover -> shrub
        (and (= layer 1) (>= score level1up)) 2 ; shrub -> tree
        (and (= layer 2) (>= score level2up)) 3 ; tree -> all
        :else layer)))
   player))

(defn player-add-surprises [player leveledup-player id-gen]
  (modify ; add surprise seeds on levelup
   :seeds
   (fn [seeds]
     (cond
      (and (= (:layer player) 0) (= (:layer leveledup-player) 1))
      (cons (make-fruit (id-gen) "boletus" "fungi") seeds)
      (and (= (:layer player) 1) (= (:layer leveledup-player) 2))
      (cons (make-fruit (id-gen) "chanterelle" "fungi") seeds)
      (and (= (:layer player) 2) (= (:layer leveledup-player) 3))
      (cons (make-fruit (id-gen) "flyagaric" "fungi") seeds)
      :else seeds))
   player))

(defn player-add-notes [player leveledup-player]
  (modify ; add notes on levelup
   :log 
   (fn [log]
      (log-remove-msgs
       (cond
        (and (= (:layer player) 0) (= (:layer leveledup-player) 1))
        (log-add-note log (make-note "levelup1" (list "ok"))) 
        (and (= (:layer player) 1) (= (:layer leveledup-player) 2))
        (log-add-note log (make-note "levelup2" (list "ok")))
        (and (= (:layer player) 2) (= (:layer leveledup-player) 3))
        (log-add-note log (make-note "levelup3" (list "ok")))
        :else log)
       "i_have_flowered_internal"))
   player))
 
; call after add notes (will clear messages) and finding new
; flowered plants
(defn player-add-flowered-msgs
  "detect new flowered plants messages and add them to the log"
  [player leveledup-player]
  (modify 
   :log 
   (fn [log]
     (reduce
      (fn [log new-fp]
        ; rename the message code
        (log-add-msg
         log (merge new-fp {:code "i_have_flowered"})))
      log
      (diff
       (:flowered-plants player)
       (:flowered-plants leveledup-player))))
   player))

(defn set-add-message-to-flowered
  "add message to flowered list, if there isn't one already"
  [flowered msg]
  (if (reduce ; does it exist?
       (fn [r fm]
         (if (and (not r) (= (first (:extra fm))
                             (first (:extra msg))))
           true r))
       false
       flowered)
    flowered
    (cons msg flowered)))

(defn player-update-flowered-plants [player leveledup-player]
  (modify ; copy the flowered messages to the flowered-plants list
   :flowered-plants
   (fn [fp]
     (if ; if we have just gone up a level, clear the flowered plants
         (not (= (:layer player) (:layer leveledup-player)))
       ()
       (reduce ; otherwise look for new flowered plants
        (fn [fp msg]
          (if (= (player-get-allowed-layer player)
                 (plant-type->layer (:from msg))) ; if the plant is in the right layer
            (set-add-message-to-flowered fp msg)
            fp))
        fp
        (log-find-msgs (:log player) "i_have_flowered_internal"))))
   player))

(defn player-update [player id-gen]
  (let [leveledup-player (player-levelup player)]
    (merge
     (player-add-surprises
      (player-add-flowered-msgs
       (player-add-notes
        (player-update-flowered-plants
         (player-update-seeds player)
         leveledup-player)
        leveledup-player)
       leveledup-player)
      leveledup-player id-gen)
     {:layer (:layer leveledup-player)})))