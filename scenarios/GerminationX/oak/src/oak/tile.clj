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
   oak.forms))

(defrecord tile
  [season
   pos
   entities])

(defn tile-pos [tile] (:pos tile))
(defn tile-entities [tile] (:entities tile))

(defn make-tile [pos entity-list]
  (tile. 'summer pos entity-list))

(defn tile-position-taken? [tile pos]
  (reduce
   (fn [r e]
     (if (and (not r) (vec2-eq? pos (plant-pos e)))
       true
       r))
   false
   (tile-entities tile)))

(defn tile-add-entity [tile entity]
  (if (not (tile-position-taken? tile (plant-pos entity)))
    (merge tile {:entities (cons entity (tile-entities tile))})
    tile))

(defn tile-find-entity [tile id]
  (reduce
   (fn [r e]
     (if (and (not r) (= id (:id e)))
       e r))
   false
   (:entities tile)))

(defn tile-get-neighbours [tile pos]
  (reduce
   (fn [l e]
     (if (and (< (vec2-dist (:pos e) pos) 3)
              (not (vec2-eq? pos (:pos e))))
       (cons e l) l))
   '()
   (:entities tile)))

(defn tile-update [tile time delta rules]
  (let [st (/ (mod time season-length) season-length)
        season (cond
                (< st 0.25) 'spring
                (< st 0.50) 'summer
                (< st 0.75) 'autumn
                :else 'winter)]
    (modify :entities
            (fn [entities]
              (doall (map
                      (fn [e]
                        ;; todo dispatch on entity type
                        (plant-update
                         e time delta
                         (tile-get-neighbours tile (:pos e))
                         rules
                         season))
                      (doall (filter
                              (fn [e]
                                (not (= (:state e) 'decayed)))
                              entities)))))
            (modify :season (fn [s] season) tile))))