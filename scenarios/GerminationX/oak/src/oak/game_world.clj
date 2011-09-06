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
   oak.remote-agent
   oak.spirit
   oak.world
   oak.log
   oak.player
   oak.defs)
  (:require
   clojure.contrib.math))

(defn game-world-get-tile
  "get a single tile from the game world"
  [game-world pos]
  (reduce
   (fn [r t]
     (if (and (not r) (vec2-eq? pos (:pos t)))
       t
       r))
   false
   (:tiles game-world)))

(defn game-world-get-tile-with-neighbours
  "get a tile and it's immediate neighbours"
  [game-world pos]
  (reduce
   (fn [r t]
     (if (or (vec2-eq? (vec2-add (make-vec2 -1 1) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 0 1) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 1 1) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 -1 0) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 0 0) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 1 0) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 -1 -1) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 0 -1) pos) (:pos t))
             (vec2-eq? (vec2-add (make-vec2 1 -1) pos) (:pos t)))
       (cons t r)
       r))
   ()
   (:tiles game-world)))

(defn game-world-add-tile
  "add a tile to the game (this is done gradually as people plant)
   leaving a sparse list"
  [game-world tile]
  (merge game-world {:tiles (cons tile (:tiles game-world))}))

(defn game-world-modify-tile
  "run function f on the tile at position pos,
   giving the tile as the only argument"
  [game-world pos f]
  (modify :tiles
          (fn [tiles]
            (map
             (fn [t]
               (if (vec2-eq? (:pos t) pos) (f t) t))
             tiles))
          game-world))

(defn find-spirit [world name]
  (reduce
   (fn [r spirit]
     (if (and (not r) (= name (:name spirit)))
       spirit r))
   false
   (:spirits world)))

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

(defn make-game-world [num-plants area]
  (let [id-gen (make-id-generator 10000)]
    (reduce
     (fn [world plant]
       (game-world-add-entity
        world
        (:tile plant)
        plant))
     (hash-map
      :version 1
      :players (list (make-player 99 "Gaia" -1))
      :tiles {}
      :spirits ()
      :log (make-log 10)
      :id-gen id-gen
      :summons {}
      :rules (load-companion-rules "rules.txt"))
     (repeatedly
      num-plants
      (fn [] (make-random-plant
              (id-gen)
              (make-vec2
               (Math/round (* (rand-gaussian) area))
               (Math/round (* (rand-gaussian) area)))))))))

(defn game-world-count [game-world]
  (println (str "players: " (count (:players game-world))))
  ;(doseq [i (:players game-world)]
  ;  (player-count i))
  (println (str "tiles: " (count (:tiles game-world))))
  (doseq [i (:tiles game-world)]
    (tile-count i))
  (println (str "spirits: " (count (:spirits game-world))))
  (doseq [i (:spirits game-world)]
    (spirit-count i)))
    
(defn game-world-save [game-world filename]
  (spit filename 
        (modify :id-gen
                (fn [id-gen]
                  (- (id-gen) 1)) ; save the current id
                game-world)))

; add logs to all the things that need them
(defn game-world-fixup-add-logs [w]
  (println "fixing world by adding logs")
  (merge
   (modify
    :players
    (fn [players]
      (map
       (fn [player]
         (merge player {:log (make-log 10)}))
       players))
    (modify
     :tiles
     (fn [tiles]
       (map
        (fn [tile]
          (modify
           :entities
           (fn [plants]
             (map
              (fn [plant]
                (merge plant {:log (make-log 10)}))
              plants))
           tile))
        tiles))
     w))
    {:version 1 :log (make-log 100) :id-gen 10000}))

(defn game-world-load [filename]
  (let [w (read-string (slurp filename))
        v (:version w)]
    (println (str "loading world version " v))
    (modify
     :id-gen
     (fn [id]
       (make-id-generator id)) ; convert the id into the function
     (cond
      (< v 1) (game-world-fixup-add-logs w)
      :else w))))

(defn game-world-print [game-world]
  (doseq [tile (:tiles game-world)]
    (println (format "tile %d %d" (:x (:pos tile)) (:y (:pos tile))))
    (doseq [plant (:entities tile)]
      (println (format "plant %d %d state: %s health: %d"
                       (:x (:pos plant)) (:y (:pos plant))
                       (:state plant) (:health plant))))))

; returns the frequency of plants to owners in the world
(defn game-world-hiscores [game-world]
  (let [freq (reduce
              (fn [r tile]
                (reduce
                 (fn [r plant]
                   (let [count (get r (:owner plant))] 
                     (if count
                       (merge r {(:owner plant) (+ 1 count)})
                       (merge r {(:owner plant) 1}))))
                 r
                 (:entities tile)))
              '{}
              (:tiles game-world))]
    ; sort by values so highest is first
    (into (sorted-map-by
           (fn [key1 key2]
             (compare (get freq key2) (get freq key1))))
          freq)))

(defn game-world-find-player-id [game-world name]
  (player-list-find-player-id (:players game-world) name))

(defn game-world-find-player [game-world id]
  (player-list-find-player (:players game-world) id))

(defn game-world-id->player-name [game-world id]
  (player-list-id->player-name (:players game-world) id))

(defn game-world-process-msg
  "replace id numbers with strings for
   the client to make sense of"
  [game-world msg]
  (let [extra (if (or
                   ; in these messages, we want to replace the id
                   ; of the other player in the extra field with
                   ; their name 
                   (= (:code msg) 'your_plant_doesnt_like)
                   (= (:code msg) 'i_am_detrimented_by)
                   (= (:code msg) 'i_am_detrimental_to)
                   (= (:code msg) 'i_am_benefitting_from)
                   (= (:code msg) 'i_am_beneficial_to)
                   (= (:code msg) 'thanks_for_helping))
                 (cons
                  (game-world-id->player-name
                   game-world (first (:extra msg)))
                  (rest (:extra msg)))
                 (:extra msg))]    
    (cond
     (= (:type msg) 'plant)
     (merge
      msg
      {:display (game-world-id->player-name
                 game-world (:player msg))
       :owner (game-world-id->player-name
               game-world (:owner msg))
       :extra extra})
     
     (= (:type msg) 'spirit)
     (merge
      msg
      {:display (game-world-id->player-name
                 game-world (:player msg))
       :extra extra})
     
     :else msg)))

(defn game-world-collect-all-msgs
  "get messages from all the tiles/plants and spirits"
  [game-world]
  (map
   (fn [msg]
     (game-world-process-msg game-world msg))
   (reduce
    (fn [r tile]
      (concat r (tile-get-log tile)))
    (reduce
     (fn [r spirit]
       (concat r (:msgs (:log spirit))))
     ()
     (:spirits game-world))
    (:tiles game-world))))

(defn game-world-post-logs-to-players [game-world msgs]
  (modify
   :players
   (fn [players]
     (map
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
      players))
   game-world))

(defn game-world-get-decayed-owners [game-world]
  (reduce
   (fn [r tile]
     (concat r (tile-get-decayed-owners tile)))
   ()
   (:tiles game-world)))

; need to do this before tile update, when decayed plants
; are removed from the game
(defn game-world-update-player-plant-counts [game-world]
  (let [decayed (game-world-get-decayed-owners game-world)]
    (modify
     :players
     (fn [players]
       (map
        (fn [player]
          (modify
           :plant-count
           (fn [c]
             (- c (count-items decayed (:id player))))
           player))
        players))
     game-world)))

(defn game-world-update-tiles [game-world time delta]
;  (game-world-count game-world)
  (modify
   :rules ; load the rules each update so they can be changed
   (fn [r] ; easily while running
     (load-companion-rules "rules.txt"))
   (modify
    :tiles
    (fn [tiles]
      (map
       (fn [tile]
         (tile-update tile time delta (:rules game-world)
                      (game-world-get-tile-with-neighbours
                        game-world (:pos tile))))
       tiles))
    game-world)))

(defn game-world-update-player-seeds [player]
  (if (and
       (not (= (:next-refresh player) 0))
       (> (current-time) (:next-refresh player)))
    (modify
     :seeds-left
     (fn [s] (:seeds-capacity player))
     (modify :next-refresh (fn [r] 0) player))
    player))

(defn game-world-update-players [game-world]
  (modify
   :players
   (fn [players]
     (map
      (fn [player]
        (game-world-update-player-seeds player))
      players))
   game-world))

(defn game-world-clear
  "clear out the incremental stuff"
  [game-world]
  (modify :summons (fn [s] {}) game-world))

(defn game-world-update [game-world time delta]
  (let [msgs (game-world-collect-all-msgs game-world)]
    (modify :log (fn [log]
                   (reduce
                    (fn [log msg]
                      (log-add-msg log msg))
                    log
                    msgs))
            (game-world-update-players
             (game-world-update-tiles
              (game-world-post-logs-to-players
               (game-world-update-player-plant-counts
                (game-world-clear game-world)) msgs) time delta)))))

(defn game-world-find-spirit [game-world name]
  (reduce
   (fn [r spirit]
     (if (and (not r) (= name (:name spirit)))
       spirit r))
   false
   (:spirits game-world)))

(defn game-world-modify-player [game-world id f]
  (modify :players
          (fn [players]
            (map
             (fn [player]
               (if (= id (:id player))
                 (f player)
                 player))
             players))
          game-world))

(defn game-world-add-player [game-world name fbid]
  (modify :players
          (fn [players]
            (cons (make-player ((:id-gen game-world)) name fbid) players))
          game-world))

(defn game-world-can-player-pick? [game-world player-id]
  (> (:seeds-left (game-world-find-player game-world player-id)) 0))

(defn game-world-player-pick [game-world player-id]
  (game-world-modify-player
   game-world
   player-id
   (fn [player]
     (modify
      :seeds-left (fn [s] (- s 1))
      (if (and (= (:next-refresh player) 0)
               (>= (:seeds-capacity player)
                   (:seeds-left player)))
        (modify
         :next-refresh
         (fn [r] (+ (current-time) seeds-duration))
         player)
        player)))))

(defn game-world-sync<-fatima [game-world fatima-world]
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
                          (:rules game-world)
                          (:players game-world))
                         spirits)
                   (cons (make-spirit ((:id-gen game-world)) agent) spirits))))
             '()
             (world-agents fatima-world)))
          game-world))



(defn game-world-sync->fatima [fatima-world game-world time]
  (reduce
   (fn [fw tile]
    (reduce
     (fn [fw entity]
       (cond

      ; check for a special event
        (> (count (:event-occurred entity)) 0)
        (reduce
         (fn [fw event]
;          (println (str "detected event :" event " on " (:id entity)))
          (world-add-object
           fw
           {"name" event 
            "owner" (:layer entity)
            "position" (str (:x (:pos entity)) "," (:y (:pos entity)))
            "tile" (:pos tile)
            "type" "object"
            "time" time}))
         fw
         (:event-occurred entity))
       
        :else
        (do
          (if (or ; filter out most states
               (= (:state entity) 'grow-a)
               (= (:state entity) 'fruit-c)
               (= (:state entity) 'ill-a)
               (= (:state entity) 'ill-b)
               (= (:state entity) 'ill-c))
            ; stops duplicates for us
            (world-add-object fw
                              {"name" (str (:layer entity) "-" (:state entity) "#" (:id entity))
                               "owner" (:layer entity)
                               "position" (str (:x (:pos entity)) "," (:y (:pos entity)))
                               "tile" (:pos tile)
                               "type" "object"
                               "time" time})
            fw))))
     fw
     (:entities tile)))
   ; pick a random summons for each spirit
   (reduce
    (fn [fw summons]
      (world-summon-agent
       fw (first summons)
       (rand-nth (second summons)))) 
    fatima-world
    (:summons game-world))
   (:tiles game-world)))


