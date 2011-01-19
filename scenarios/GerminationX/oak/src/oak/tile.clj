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
   oak.plant))

(defrecord tile
  [pos
   entities])

(defn tile-pos [tile] (:pos tile))
(defn tile-entities [tile] (:entities tile))

(defn make-tile [pos entity-list]
  (tile. pos entity-list))

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