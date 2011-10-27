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

(ns oak.tile
  (:use
   oak.vec2
   oak.plant
   oak.forms
   oak.defs))

; tiles are containers for all entities, their size is defined
; by tile-size in defs.clj. chunked together in 3X3 units for
; sending to the client

(defn make-tile [pos entity-list]
  (hash-map
   :version 0
   :season "summer"
   :pos pos
   :index (str (:x pos) "," (:y pos))
   :entities entity-list))

(defn tile-strip
  "remove crud for download"
  [tile player-layer]
  (modify
   :entities
   (fn [entities]
     (map (fn [plant] (plant-strip plant player-layer)) entities))
   tile))
  
(defn tile-distance
  "calculate distance over different tiles"
  [tilea posa tileb posb]
  (let [diff (vec2-mul (vec2-sub tileb tilea) tile-size)]
    (vec2-dist posa (vec2-add diff posb))))

(defn tile-count
  "searchin: fer memleaks"
  [tile]
  (println (str "entities: " (count (:entities tile))))
  (comment doseq [i (:entities tile)]
    (plant-count i)))

(defn tile-position-taken?
  "is there a plant at this position?"
  [tile pos]
  (reduce
   (fn [r e]
     (if (and (not r) (vec2-eq? pos (:pos e)))
       true
       r))
   false
   (:entities tile)))

(defn tile-add-entity
  "add entity to the tile, using it:s position,
  does nothing if position is already taken"
  [tile entity]
  (if (not (tile-position-taken? tile (:pos entity)))
    (merge tile {:entities (cons entity (:entities tile))})
    tile))

(defn tile-find-entity
  "search for entity using id"
  [tile id]
  (reduce
   (fn [r e]
     (if (and (not r) (= id (:id e)))
       e r))
   false
   (:entities tile)))

(defn tile-modify-entity
  "run proc f on entity id, giving entity as sole argument"
  [tile id f]
  (modify
   :entities
   (fn [entities]
     (map
      (fn [e]
        (if (= id (:id e))
          (f e) e))
      entities))
   tile))

(defn tile-get-neighbours
  "get all neighbouring entities within range
   across neihbouring tile boundaries"
  [centre-tile-pos id pos neighbours]
  (reduce
   (fn [r tile] ; for each neighbours (includes current tile)
     (reduce
      (fn [r other] ; for each entity
        (if (and (< (tile-distance
                     centre-tile-pos pos
                     (:pos tile) (:pos other))
                    plant-influence-distance)
                 (not (= id (:id other)))) ; don:t return ourselves
          (cons other r) r))
      r
      (:entities tile)))
   ()
   neighbours))

(defn tiles-find-entity
  "search multiple tiles for entity using id"
  [tiles id]
  (reduce
   (fn [r tile]
     (reduce
      (fn [r e]
        (if (and (not r) (= id (:id e)))
          e r))
      r
      (:entities tile)))
   false
   tiles))

(defn tiles-find-entity-with-tile
  "search multiple tiles for entity using id
   return list containing tile and entity"
  [tiles id]
  (reduce
   (fn [r tile]
     (reduce
      (fn [r e]
        (if (and (not r) (= id (:id e)))
          (list tile e)  r))
      r
      (:entities tile)))
   false
   tiles))


(defn tile-get-decayed-owners
  "returns all the entities who have decayed, for removal"
  [tile]
  (reduce
   (fn [r e]
     (if (= (:state e) "decayed")
       (cons (:owner-id e) r) r))
   ()
   (:entities tile)))

(defn tile-update
  "update all the entities and calculate season change for this tile"
  [tile time delta rules neighbouring-tiles]
  (let [st (/ (mod time season-length) season-length)
        season (cond
                (< st 0.25) "spring"
                (< st 0.50) "summer"
                (< st 0.75) "autumn"
                :else "winter")]
    (modify :entities
            (fn [entities]
              (map
               (fn [e]
                 ;; todo dispatch on entity type
                 (plant-update
                  e time delta
                  (tile-get-neighbours
                   (:pos tile) (:id e) (:pos e) neighbouring-tiles)
                  rules
                  season))
               (filter
                (fn [e]
                  (not (= (:state e) "decayed")))
                entities)))
            (modify :season (fn [s] season) tile))))

(defn tile-clear-log
  "clears logs for everything"
  [tile]
  (modify
   :entities
   (fn [entities]
     (map
      (fn [e]
        (plant-clear-log e))
      entities))
   tile))

(defn tile-clear-events
  "clears events from everything"
  [tile]
  (modify
   :entities
   (fn [entities]
     (map
      (fn [e]
        (plant-clear-events e))
      entities))
   tile))

(defn tile-get-log
  "gets the message logs for all the entities"
  [tile]
  (reduce
   (fn [r e]
     (concat r (:msgs (:log e))))
   ()
   (:entities tile)))
  
  