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
   oak.player)
  (:require
   clojure.contrib.math))

(defn game-world-get-tile [game-world pos]
  (reduce
   (fn [r t]
     (if (and (not r) (vec2-eq? pos (:pos t)))
       t
       r))
   false
   (:tiles game-world)))

(defn game-world-add-tile [game-world tile]
  (merge game-world {:tiles (cons tile (:tiles game-world))}))

(defn game-world-modify-tile [game-world pos f]
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

(defn game-world-summon-spirit [world tile-pos entity]
  (let [spirit-name (layer->spirit-name (:layer entity))]
    (modify :spirits
            (fn [spirits]
              (map
               (fn [spirit]
                 (if (= spirit-name (:name spirit))
                   (modify :tile
                           (fn [tile]
                             (println (str "summonning " spirit-name " to " tile-pos))
                             tile-pos)
                           spirit)
                   spirit))
               spirits))
            world)))

(defn game-world-add-entity [game-world tile-pos entity]
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
        (make-vec2
         (Math/round (* (rand-gaussian) area))
         (Math/round (* (rand-gaussian) area)))
        plant))
     (hash-map
      :version 1
      :players ()
      :tiles {}
      :spirits ()
      :log (make-log 100)
      :id-gen id-gen)
     (repeatedly num-plants (fn [] (make-random-plant (id-gen)))))))

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

(defn game-world-collect-all-msgs [game-world]
  (reduce
   (fn [r tile]
     (concat r (tile-get-log tile)))
   (reduce
    (fn [r spirit]
      (concat r (:msgs (:log spirit))))
    ()
    (:spirits game-world))
   (:tiles game-world)))

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
              (if (= (:to msg) (:id player))
                (log-add-msg log msg)
                log))
            log
            msgs))
         player))
      players))
   game-world))
 
(defn game-world-update-tiles [game-world time delta]
;  (game-world-count game-world)
  (let [rules (load-companion-rules "rules.txt")]
    (modify :tiles
            (fn [tiles]
              (map
               (fn [tile]
                 (tile-update tile time delta rules))
               tiles))
            game-world)))

(defn game-world-update-player-seeds [player]
  (if (and
       (not (= (:next-refresh player) 0))
       (> (current-time) (:next-refresh player)))
    (modify
     :seeds-left
     (fn [s] 3) ; todo: look at level
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

(defn game-world-update [game-world time delta]
  (let [updated (game-world-update-tiles game-world time delta)]
    (game-world-update-players
     (game-world-post-logs-to-players
      updated
      (game-world-collect-all-msgs updated)))))

(defn game-world-find-spirit [game-world name]
  (reduce
   (fn [r spirit]
     (if (and (not r) (= name (:name spirit)))
       spirit r))
   false
   (:spirits game-world)))

(defn game-world-find-player-id [game-world name]
  (reduce
   (fn [r player]
     (if (and (not r) (= name (:name player)))
       (:id player) r))
   false
   (:players game-world)))

(defn game-world-find-player [game-world id]
  (reduce
   (fn [r player]
     (if (and (not r) (= id (:id player)))
       player r))
   false
   (:players game-world)))

(defn game-world-id->player-name [game-world id]
  (:name (game-world-find-player game-world id)))

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

(defn game-world-add-player [game-world name]
  (modify :players
          (fn [players]
            (cons (make-player ((:id-gen game-world)) name) players))
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
      (if (= 1 (:seeds-left player))
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
                          (game-world-get-tile
                           game-world (:tile spirit)))
                         spirits)
                   (cons (make-spirit agent) spirits))))
             '()
             (world-agents fatima-world)))
          game-world))

(defn game-world-sync->fatima [fatima-world game-world time]
  (reduce
   (fn [fw tile]
    (reduce
     (fn [fw entity]
       (cond
        (or
         (= (:state entity) 'grow-a)
         (= (:state entity) 'fruit-a)
         (= (:state entity) 'fruit-b)
         (= (:state entity) 'fruit-c)
         (= (:state entity) 'ill-a)
         (= (:state entity) 'ill-b)
         (= (:state entity) 'ill-c))
        (do
          (world-add-object fw
                            {"name" (str (:layer entity) "-" (:state entity) "#" (:id entity))
                             "owner" (:layer entity)
                             "position" (str (:x (:pos entity)) "," (:y (:pos entity)))
                             "tile" (:pos tile)
                             "type" "object"
                             "time" time}))
        :else fw))
     fw
     (:entities tile)))
   ; update the agent's tile position
   (merge fatima-world
          {:agents
           (map
            (fn [agent]
              (merge agent
                     {:tile
                      (:tile
                       (find-spirit game-world (remote-agent-name agent)))}))
            (world-agents fatima-world))})
   (:tiles game-world)))


