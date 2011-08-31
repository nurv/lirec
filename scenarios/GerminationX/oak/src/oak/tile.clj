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

(defn make-tile [pos entity-list]
  (hash-map
   :version 0
   :season 'summer
   :pos pos
   :entities entity-list))

(defn tile-count [tile]
  (println (str "entities: " (count (:entities tile))))
  (comment doseq [i (:entities tile)]
    (plant-count i)))

(defn tile-position-taken? [tile pos]
  (reduce
   (fn [r e]
     (if (and (not r) (vec2-eq? pos (:pos e)))
       true
       r))
   false
   (:entities tile)))

(defn tile-add-entity [tile entity]
  (if (not (tile-position-taken? tile (:pos entity)))
    (merge tile {:entities (cons entity (:entities tile))})
    tile))

(defn tile-find-entity [tile id]
  (reduce
   (fn [r e]
     (if (and (not r) (= id (:id e)))
       e r))
   false
   (:entities tile)))

(defn tile-modify-entity [tile id f]
  (modify
   :entities
   (fn [entities]
     (map
      (fn [e]
        (if (= id (:id e))
          (f e) e))
      entities))
   tile))

(defn tile-get-neighbours [tiles pos]
  (reduce
   (fn [r tile]
     (reduce
      (fn [l e]
        (if (and (< (vec2-dist (:pos e) pos) 3)
                 (not (vec2-eq? pos (:pos e))))
          (cons e l) l))
      r
      (:entities tile)))
   ()
   tiles))

(defn tile-get-decayed-owners [tile]
  (reduce
   (fn [r e]
     (if (= (:state e) 'decayed)
       (cons (:owner-id e) r) r))
   ()
   (:entities tile)))

(defn tile-update [tile time delta rules neighbouring-tiles]
  (let [st (/ (mod time season-length) season-length)
        season (cond
                (< st 0.25) 'spring
                (< st 0.50) 'summer
                (< st 0.75) 'autumn
                :else 'winter)]
    (modify :entities
            (fn [entities]
              (map
               (fn [e]
                 ;; todo dispatch on entity type
                 (plant-update
                  e time delta
                  (tile-get-neighbours
                   neighbouring-tiles (:pos e))
                  rules
                  season))
               (filter
                (fn [e]
                  (not (= (:state e) 'decayed)))
                entities)))
            (modify :season (fn [s] season) tile))))

(defn tile-get-log [tile]
  ;(println "-------------")
  (reduce
   (fn [r e]
     ;(when (> (count (:msgs (:log e))) 0)
     ;  (println (count (:msgs (:log e)))))
     (concat r (:msgs (:log e))))
   ()
   (:entities tile)))
  
  