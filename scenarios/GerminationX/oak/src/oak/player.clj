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
;; along with this program.  If not see <http://www.gnu.org/licenses/>.

(ns oak.player
    (:use
     oak.log
     oak.defs
     oak.forms))

(defn make-player [id name fbid]
  (hash-map
   :version 1
   :id id
   :fbid fbid
   :name name ; only for login - do not use directly
   :layer 0
   :seeds '()
   :messages '()
   :seeds-capacity 3
   :seeds-left 10
   :next-refresh 0
   :picked-by '()
   :has-picked '()
   :plant-count 0
   :log (make-log 10)))

(defn player-inc-plant-count [player]
  (modify
   :plant-count
   (fn [c]
     (+ c 1))
   player))

(defn player-list-find-player-id [player-list name]
  (reduce
   (fn [r player]
     (if (and (not r) (= name (:name player)))
       (:id player) r))
   false
   player-list))

(defn player-list-find-player [player-list id]
  (reduce
   (fn [r player]
     (if (and (not r) (= id (:id player)))
       player r))
   false
   player-list))

(defn player-list-id->player-name [player-list id]
  (:name (player-list-find-player player-list id)))

(defn player-remove-fruit [player fruit-id]
  (modify
   :seeds
   (fn [seeds]
     (filter
      (fn [seed]
        (not (= (:id seed) fruit-id)))
      seeds))
   player))

(defn player-has-fruit? [player fruit-id]
  (modify
   :seeds
   (fn [seeds]
     (reduce
      (fn [r seed]
        (if (and (not r) (= (:id seed) fruit-id))
          true r))
      false
      seeds))
   player))

(defn player-get-fruit [player fruit-id]
  (reduce
   (fn [r seed]
     (if (and (not r) (= (:id seed) fruit-id))
       seed r))
   false
   (:seeds player)))
