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

(ns oak.game-world
  (:use
   oak.io
   oak.forms
   oak.vec2
   oak.plant
   oak.tile
   oak.rand
   oak.spirit
   oak.fruit
   oak.log
   oak.player
   oak.defs
   oak.db
   oak.profile)
  (:require
   clojure.contrib.math))

(defn game-world-get-tile
  "get a single tile from the game world"
  [game-world pos]
  (prof
   :get-tile
   (let [tiles (db-get :tiles {:pos.x (:x pos) :pos.y (:y pos)})]
     (if (not (empty? tiles)) (first tiles) false))))

(defn game-world-get-tile-with-neighbours
  "get a tile and it:s immediate neighbours"
  [game-world pos]
  (prof
   :get-tiles
   (db-get :tiles {:pos.x {:$lt (+ (:x pos) 2)  
                           :$gt (- (:x pos) 2)}
                   :pos.y {:$lt (+ (:y pos) 2)  
                           :$gt (- (:y pos) 2)}})))

(defn game-world-add-tile
  "add a tile to the game (this is done gradually as people plant)
   leaving a sparse list"
  [game-world tile]
  (db-add! :tiles tile)
  game-world)

(defn game-world-modify-tile
  "run function f on the tile at position pos,
   giving the tile as the only argument"
  [game-world pos f]
  (db-find-update! f :tiles {:pos.x (:x pos) :pos.y (:y pos)})
  game-world)

; now controlled by fatima
(defn game-world-summon-spirit
  "move the spirit corresponding to this entities
  layer to a new tile, not guaranteed to work, as
  a random summons is honored"
  [world tile-pos entity]
  (let [spirit-name (layer->spirit-name (:layer entity))]
    (println "summoning" spirit-name)
    (modify
     :summons
     (fn [s]       
       (merge s {spirit-name
                 (if (contains? s spirit-name)
                   (cons tile-pos (get s spirit-name))
                   (list tile-pos))}))
     world)))

(defn game-world-add-entity
  "add a new entity into the world, and summon the right
   spirit to come and look at it"
  [game-world tile-pos entity]
   (let [tile (game-world-get-tile game-world tile-pos)]
    (game-world-summon-spirit
     (if (not tile)
       (game-world-add-tile game-world (make-tile tile-pos (list entity)))
       (game-world-modify-tile
        game-world
        tile-pos
        (fn [tile]
          (tile-add-entity tile entity))))
     tile-pos
     entity)))

(defn make-game-world
  "make a world in the old-fashioned way for building a new
   database with"
  [num-plants area]
  (let [id-gen (make-id-generator 10000)]
    (reduce
     (fn [world plant]
       (game-world-add-entity
        world
        (:tile plant)
        plant))
     (hash-map
      :version 1
      :log (make-log 10)
      :id-gen id-gen
      :spirits ()
      :players (list (make-player 97 "Charlie" -1)
                     (make-player 98 "Percy" -1)
                     (make-player 99 "Alan" -1))
      :summons {}
      :rules (load-companion-rules "rules.txt"))
     (repeatedly
      num-plants
      (fn [] (make-random-plant
              (id-gen)
              (make-vec2
               (Math/round (* (rand-gaussian) area))
               (Math/round (* (rand-gaussian) area)))))))))

(defn gamw-world-db-build!
  "build a database from the world"
  [game-world]
  (db-build-collection! :players (:players game-world))
  (db-add-index! :players [:id])
  
  (db-build-collection! :tiles
                        (map
                         (fn [tile]
                           (merge
                            tile
                            {:index (str (:x (:pos tile)) ","
                                         (:y (:pos tile)))}))
                         (:tiles game-world)))
  (db-add-index! :tiles [:index]))

(defn make-empty-game-world
  "make a world for use with the database"
  []
  (let [id-gen (make-id-generator 10000)]
     (hash-map
      :version 1
      :log (make-log 10)
      :id-gen id-gen
      :spirits ()
      :summons {}
      :rules (load-companion-rules "rules.txt"))))

(defn game-world-save [game-world filename]
  (spit filename 
        (modify :id-gen
                (fn [id-gen]
                  (- (id-gen) 1)) ; save the current id
                game-world)))

(defn game-world-load [filename]
  (let [w (read-string (slurp filename))
        v (:version w)]
    (println (str "loading world version " v))
    (modify
     :id-gen
     (fn [id]
       (make-id-generator id)) ; convert the id into the function
     w)))

(defn game-world-find-player-id
  "get the player id from the name (should only be used when logging in)"
  [game-world name]
  (:id (first (db-get :players {:name name}))))

(defn game-world-find-player
  "find player by id"
  [game-world id]
  (first (db-get :players {:id id})))

(defn game-world-id->player-name [game-world id]
  "helper to get the name from a player id"
  (:name (game-world-find-player game-world id)))

(defn game-world-process-msg
  "replace id numbers with strings for
   the client to make sense of - add information"
  [game-world msg]
  (let [extra (if (or
                   ; in these messages, we want to replace the id
                   ; of the other player in the extra field with
                   ; their name 
                   (= (:code msg) :your_plant_doesnt_like)
                   (= (:code msg) :i_am_detrimented_by)
                   (= (:code msg) :i_am_detrimental_to)
                   (= (:code msg) :i_am_benefitting_from)
                   (= (:code msg) :i_am_beneficial_to)
                   (= (:code msg) :needs_help)
                   (= (:code msg) :ive_asked_x_for_help)
                   ;(= (:code msg) :thanks_for_helping)
                   )
                (cons
                 (game-world-id->player-name
                  game-world (first (:extra msg)))
                 (rest (:extra msg)))
                (:extra msg))]    
    (cond
     (= (:type msg) "plant")
     (merge
      msg
      {:display (game-world-id->player-name
                 game-world (:player msg))
       :owner (game-world-id->player-name
               game-world (:owner msg))
       :extra extra})
     
     (= (:type msg) "spirit")
     (merge
      msg
      {:display (game-world-id->player-name
                 game-world (:player msg))
       :extra extra})
     
     :else msg)))

(defn game-world-collect-all-msgs
  "get messages from all the tiles/plants and spirits"
  [game-world time]
  (prof
   :collect-all-messages
   (map
    (fn [msg]
      (game-world-process-msg game-world msg))
    (db-partial-reduce
     (fn [r tile]
       (let [msgs (concat r (tile-get-log tile))]
         (db-update! :tiles tile (tile-clear-log tile))
         msgs))
     (reduce
      (fn [r spirit]
        (concat r (:msgs (:log spirit))))
      ()
      (:spirits game-world))
     :tiles
     time
     4))))

(defn game-world-post-logs-to-players
  "dispatch messages to the players"
  [game-world msgs]
  (db-map!
   (fn [player]
     (modify
      :log
      (fn [log]
        (reduce
         (fn [log msg]
           (if (= (:player msg) (:id player))
             (log-add-msg log msg)
             log))
         log
         msgs))
      player))
   :players)
  game-world)

(defn game-world-get-decayed-owners
  "find the owners of decayed plants so we can update the count"
  [game-world]
  (db-reduce
   (fn [r tile]
     (concat r (tile-get-decayed-owners tile)))
   ()
   :tiles))

; need to do this before tile update, when decayed plants
; are removed from the game
(defn game-world-update-player-plant-counts
  "update the plant counts for each player"
  [game-world]
  (let [decayed (game-world-get-decayed-owners game-world)]
    (db-map!
     (fn [player]
       (modify
        :plant-count
        (fn [c]
          (- c (count-items decayed (:id player))))
        player))
     :players))
  game-world)

(defn game-world-update-tiles
  "update the tiles"
  [game-world time delta]
  (prof
   :update-tiles
   (do
     (db-partial-map!
      (fn [tile]
        (tile-update tile time delta (:rules game-world)
                     (game-world-get-tile-with-neighbours
                       game-world (:pos tile))))
      :tiles
      time 4) ; only do 4 tiles per tick
     (modify
      :rules ; load the rules each update so they can be changed
      (fn [r] ; easily while running
        (load-companion-rules "rules.txt"))
      game-world))))

(defn game-world-update-player-seeds
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

(defn game-world-update-players
  "do things that need updating for players"
  [game-world]
  (prof
   :update-players
   (db-map!
    (fn [player]
      (game-world-update-player-seeds player))
    :players))
  game-world)

(defn game-world-clear
  "clear out the incremental stuff"
  [game-world]
  (modify :summons (fn [s] {}) game-world))

(defn game-world-update
  "main update"
  [game-world time delta]
  (let [msgs (game-world-collect-all-msgs game-world time)]
    (modify :log (fn [log]
                   (reduce
                    (fn [log msg]
                      (log-add-msg log msg))
                    log
                    msgs))
            (game-world-update-players
             (game-world-update-tiles
              (game-world-post-logs-to-players
               ;(game-world-update-player-plant-counts
               (game-world-clear game-world);)
              msgs) time delta)))))

(defn game-world-find-spirit
  "get the spirit from it's name"
  [world name]
  (reduce
   (fn [r spirit]
     (if (and (not r) (= name (:name spirit)))
       spirit r))
   false
   (:spirits world)))

(defn game-world-modify-spirit
  "modify a spirit from it's name"
  [world name f]
  (map
   (fn [spirit]
     (if (= name (:name spirit))
       (f spirit) spirit))
   (:spirits world)))

(defn game-world-modify-player
  "replace the specified player with the result of f"
  [game-world id f]
  (db-find-update! f :players {:id id})
  game-world)

(defn game-world-add-player
  "make a new player"
  [game-world name fbid]
  (db-add! :players
           (make-player ((:id-gen game-world)) name fbid))
  game-world)

(defn game-world-find-plant
  "find a plant from it's tile pos and id"
  [game-world tile-pos plant-id]
  (tile-find-entity
   (game-world-get-tile game-world tile-pos)
   plant-id))
  
(defn game-world-can-player-pick?
  "can the player pick from a plant?"
  [game-world player-id]
  (> (:seeds-left (game-world-find-player game-world player-id)) 0))

(defn game-world-player-pick
  "pick fruit from a plant"
  [game-world player-id tile-pos plant-id]
  (game-world-modify-player
   game-world
   player-id
   (fn [player]
     (modify
      :seeds
      (fn [seeds]
        (let [plant (game-world-find-plant
                     game-world
                     tile-pos
                     plant-id)]
          (max-cons (make-fruit
                     ((:id-gen game-world))
                     (:type plant) (:layer plant))
                    seeds max-player-fruit)))
      (modify
       :seeds-left (fn [s] (- s 1))
       (if (and (= (:next-refresh player) 0)
                (>= (:seeds-capacity player)
                    (:seeds-left player)))
         (modify
          :next-refresh
          (fn [r] (+ (current-time) seeds-duration))
          player)
         player))))))

