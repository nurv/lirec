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
   oak.world)
  (:require
   clojure.contrib.math))

(defrecord game-world
  [players
   tiles
   spirits])

(defn game-world-players [game-world] (:players game-world))
(defn game-world-tiles [game-world] (:tiles game-world))
(defn game-world-spirits [game-world] (:spirits game-world))

(defn game-world-get-tile [game-world pos]
  (reduce
   (fn [r t]
     (if (and (not r) (vec2-eq? pos (:pos t)))
       t
       r))
   false
   (:tiles game-world)))

(defn game-world-add-tile [game-world tile]
  (merge game-world {:tiles (cons tile (game-world-tiles game-world))}))

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

(defn layer->spirit-name [layer]
  (cond
   (= layer "canopy") "CanopySpirit"
   (= layer "vertical") "VerticalSpirit"
   (= layer "cover") "CoverSpirit"
   :else "UnknownSpirit"))

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
  (reduce
   (fn [world plant]
     (game-world-add-entity
      world
      (make-vec2
       (Math/round (* (rand-gaussian) area))
       (Math/round (* (rand-gaussian) area)))
      plant))
   (game-world. () {} ()) 
   (repeatedly num-plants (fn [] (make-random-plant)))))

(defn game-world-print [game-world]
  (doseq [tile (game-world-tiles game-world)]
    (println (format "tile %d %d" (:x (tile-pos tile)) (:y (tile-pos tile))))
    (doseq [plant (tile-entities tile)]
      (println (format "plant %d %d state: %s health: %d"
                       (:x (:pos plant)) (:y (:pos plant))
                       (:state plant) (:health plant))))))

(defn game-world-update [game-world time delta]
  (let [rules (load-companion-rules "rules.txt")]
    (modify :tiles
            (fn [tiles]
              (doall (map
                      (fn [tile]
                        (tile-update tile time delta rules))
                      tiles)))
            game-world)))
  
(defn game-world-find-spirit [game-world name]
  (reduce
   (fn [r spirit]
     (if (and (not r) (= name (:name spirit)))
       spirit r))
   false
   (:spirits game-world)))

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
                            {"name" (str (:layer entity) "-" (:state entity) "#" (:id entity) "#")
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


