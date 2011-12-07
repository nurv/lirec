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
   oak.profile
   oak.id-gen)
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

(defn game-world-add-tile
  "add a tile to the game (this is done gradually as people plant)
   leaving a sparse list"
  [game-world tile time delta]
  (db-add! :tiles tile)
  (game-world-modify-tile ; run update on the tile to init the plant
   game-world
   (:pos tile)
   (fn [tile]
     (tile-update tile time delta (:rules game-world)
                  (game-world-get-tile-with-neighbours
                    game-world (:pos tile))))))

(defn game-world-add-entity
  "add a new entity into the world, and summon the right
   spirit to come and look at it"
  [game-world tile-pos entity time delta]
   (let [tile (game-world-get-tile game-world tile-pos)]
    (game-world-summon-spirit
     (if (not tile)
       (game-world-add-tile game-world (make-tile tile-pos (list entity)) time delta)
       (game-world-modify-tile ; run update on the tile to init the plant
        game-world
        tile-pos
        (fn [tile]
          (tile-update (tile-add-entity tile entity)
                       time delta (:rules game-world)
                       (game-world-get-tile-with-neighbours
                         game-world (:pos tile))))))
     tile-pos
     entity)))

(defn make-game-world
  "make a world in the old-fashioned way for building a new
   database with"
  [num-plants area]
  (let [id-gen (make-id-generator)]
    (reduce
     (fn [world plant]
       (game-world-add-entity
        world
        (:tile plant)
        plant 0 1))
     (hash-map
      :version 1
      :log (make-log 100)
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

(defn game-world-db-build!
  "build a database from the world"
  [game-world]
  (db-build-collection! :players (map
                                  (fn [player]
                                    (merge player
                                           {:flowered-plants ()}
                                           {:seeds-capacity 5}
                                           {:seeds-left 5}
                                           {:log (merge (:log player)
                                                        {:notes ()})}))
                                  (:players game-world)))
  (db-add-index! :players [:id])
  
  (db-build-collection! :tiles
                        (map
                         (fn [tile]
                           (merge
                            tile
                            {:index (str (:x (:pos tile)) ","
                                         (:y (:pos tile)))}))
                         (:tiles game-world)))

 ; todo - add new tick time to plants
  
  (db-add-index! :tiles [:index]))

(defn upgrade-plant-owner-names! [world]
  (db-map!
   (fn [tile]
     (modify
      :entities
      (fn [entities]
        (map
         (fn [plant]
           (let [owner-name (game-world-id->player-name
                             world (:owner-id plant))]
             (println "upgrading plant" (:id plant) "to include owner" owner-name)
             (merge plant {:owner owner-name})))
         entities))
      tile))
   :tiles))

(defn upgrade-log-one-time-msgs! [world]
  ; logs on plants
  (db-map!
   (fn [tile]
     (modify
      :entities
      (fn [entities]
        (map
         (fn [plant]
           (modify
            :log
            (fn [log]
              (println "upgrading plant" (:id plant) "to include one-time msgs")
              (merge log {:one-time-msgs ()}))
            plant))
         entities))
      tile))
   :tiles)
  ; logs on players
  (db-map!
   (fn [player]
     (modify
      :log
      (fn [log]
        (println "upgrading player" (:name player) "to include one-time msgs")
        (merge log {:one-time-msgs ()}))
      player))
   :players))

(defn game-world-set-db-version! [world current to]
  (db-update!
   :game current
   (merge current {:value to})))

(defn game-world-upgrade-db! [world]
  (let [current-version (first (db-get :game {:name "version"}))]
    (when (or (not current-version)
              (> db-version (:value current-version)))
      (cond
       (= (:value current-version) 0)
       (do
         (upgrade-plant-owner-names! world)
         (upgrade-log-one-time-msgs! world)
         (game-world-set-db-version!
          world current-version 1)
         (game-world-upgrade-db! world))))))

(defn make-empty-game-world
  "make a world for use with the database"
  []
  (let [id-gen (make-id-generator)
        world (hash-map
               :version 1
               :log (make-log 100)
               :id-gen id-gen
               :spirits ()
               :summons {}
               :rules (load-companion-rules "rules.txt"))]
    (game-world-upgrade-db! world)
    world))

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
       (make-id-generator)) ; convert the id into the function
     w)))

(defn game-world-process-msg
  "replace id numbers with strings for
   the client to make sense of - add information"
  [game-world msg]
  (let [extra (if (or
                   ; in these messages, we want to replace the id
                   ; of the other player in the extra field with
                   ; their name 
                   (= (:code msg) "your_plant_doesnt_like")
                   (= (:code msg) "i_am_detrimented_by")
                   (= (:code msg) "i_am_detrimental_to")
                   (= (:code msg) "i_am_benefitting_from")
                   (= (:code msg) "i_am_beneficial_to")
                   (= (:code msg) "needs_help")
                   (= (:code msg) "ive_asked_x_for_help")
                   (= (:code msg) "gift_sent") ; this msgs doesn't get here as sent by core
                   (= (:code msg) "gift_received") ; this msgs doesn't get here as sent by core
                   ;(= (:code msg) "thanks_for_helping")
                   )
                (do
                  ;(println "fixup for" (:code msg))
                  (cons ; add the name to the start of the list
                   (game-world-id->player-name
                    game-world (first (:extra msg)))
                   (:extra msg)))
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
     server-db-items))))

(defn game-world-post-logs-to-players
  "dispatch messages to the players"
  [game-world msgs]
  ; todo, reduce over messages and send to player
  ; rather than this way around (map over players)
  (prof
   :post-logs-map!
   (db-map!
    (fn [player]
      (modify
       :log
       (fn [log]
         (reduce
          (fn [log msg]
            (if (= (:player msg) (:id player))
              (log-add-msg-ignore-one-time log msg)
              log))
          log
          msgs))
       player))
    :players))
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
        (prof :tile-update
              (tile-update tile time delta (:rules game-world)
                           (game-world-get-tile-with-neighbours
                             game-world (:pos tile)))))
      :tiles
      time server-db-items) 
     (modify
      :rules ; load the rules each update so they can be changed
      (fn [r] ; easily while running
        (load-companion-rules "rules.txt"))
      game-world))))

(defn game-world-update-players
  "do things that need updating for players"
  [game-world time]
  (prof
   :update-players
   (db-partial-map! 
    (fn [player]
      (player-update player (:id-gen game-world)))
    :players
    time
    server-db-items))
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
              msgs) time delta) time))))

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
  [game-world name f]
  (modify
   :spirits
   (fn [spirits]
     (map
      (fn [spirit]
        (if (= name (:name spirit))
          (f spirit) spirit))
      spirits))
   game-world))

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
  (let [player (game-world-find-player game-world player-id)]
    (and (< (count (:seeds player)) 5)
         (> (:seeds-left player) 0))))

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
      (modify ; will have checked > 0 already
       :seeds-left (fn [s] (- s 1))
       player)))))
