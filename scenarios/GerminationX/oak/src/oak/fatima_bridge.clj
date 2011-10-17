;; Copyright (C) 2011 FoAM vzw
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

(ns oak.fatima-bridge
  (:use
   oak.forms
   oak.plant
   oak.tile
   oak.remote-agent
   oak.spirit
   oak.game-world
   oak.fatima-world
   oak.profile
   oak.db))

(defn game-world-entity-events->fatima
  "send entity events to fatima"
  [fw entity tile time]
  (reduce
   (fn [fw event]
     ;(println (str "detected event :" event " on " (:id entity)))
     (world-add-object
      fw
      {"name" event 
       "owner" (:layer entity)
       "position" (str (:x (:pos entity)) "," (:y (:pos entity)))
       "tile" (:pos tile)
       "type" "object"
       "time" time}))
   fw
   (:event-occurred entity)))

(defn game-world-entity-state->fatima
  "convert the state to objects in the fatima world"
  [fw entity tile time]
  (if (or ; filter out most states
       (= (:state entity) "grow-a")
       (= (:state entity) "fruit-c")
       (= (:state entity) "ill-a")
       (= (:state entity) "ill-b")
       (= (:state entity) "ill-c"))
       ; stops duplicates for us automatically
    (world-add-object fw
                      {"name" (str (:layer entity) "-" (:state entity) "#" (:id entity))
                       "owner" (:layer entity)
                       "position" (str (:x (:pos entity)) "," (:y (:pos entity)))
                       "tile" (:pos tile)
                       "type" "object"
                       "time" time})
    fw))

(defn game-world-entity->fatima
  "process an entity into fatima objects"
  [fw entity tile time]
   (game-world-entity-state->fatima
    (game-world-entity-events->fatima fw entity tile time)
    entity tile time))

(defn game-world-tiles->fatima
  [fatima-world game-world time]
  (prof
   :game-world-tiles->fatima
   (db-partial-reduce
    (fn [fw tile]
      (let [r (reduce
               (fn [fw entity]
                 (game-world-entity->fatima fw entity tile time))
               fw
               (:entities tile))]
        ; need to clear the entity of events now
        (db-update! :tiles tile (tile-clear-events tile))
        r))
    fatima-world
    :tiles time 4)))

(defn game-world-summons->fatima
  "send a random summons fromt the list to fatima"
  [fatima-world game-world]
  (reduce
   (fn [fw summons]
     (world-summon-agent
      fw
      (first summons)
      (rand-nth (second summons))))
   fatima-world 
   (:summons game-world)))

(defn game-world-offerings->fatima
  "read the spirit offerings"
  [fatima-world game-world time]
  (reduce
   (fn [fw spirit]
     (reduce
      (fn [fw offering]
        (world-add-object fw
                          {"name" (str (:layer offering) "-offering")
                           "owner" (:name spirit)
                           "position" "nowhere"
                           "tile" "none" ; will be seen by everyone
                           "type" "object"
                           "time" time}))
      fw
      (:offerings spirit)))
   fatima-world
   (:spirits game-world)))

(defn game-world-sync->fatima
  "update fatima from the game world"
  [fatima-world game-world time]
  (game-world-summons->fatima
   (game-world-tiles->fatima
    (game-world-offerings->fatima fatima-world game-world time)
    game-world time)
   game-world))

(defn game-world-sync<-fatima
  "update the game world from fatima"
  [game-world fatima-world]
  (modify :spirits
          (fn [spirits]
            (reduce
             (fn [spirits agent]
               (let [spirit (game-world-find-spirit
                             game-world
                             (remote-agent-name agent))]
                 (if spirit
                   (cons (spirit-update
                          spirit agent
                          (game-world-get-tile-with-neighbours
                           game-world (:tile agent))
                          (:rules game-world))
                         spirits)
                   (cons (make-spirit ((:id-gen game-world)) agent) spirits))))
             '()
             (world-agents fatima-world)))
          game-world))
