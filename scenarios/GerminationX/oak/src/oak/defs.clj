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

(ns oak.defs
  (:require
   clojure.contrib.math))

(def tile-size 5)
(def season-length (* 60 3))
(def min-health 10)
(def max-health 90)
(def start-health 100)
(def min-neighbours 2)
(def max-neighbours 5)
(def seeds-duration (* 60 60 1000))

(defn plant-type->layer [type]
  (cond
   (= type "dandelion") "cover"
   (= type "clover") "cover"
   (= type "aronia") "shrub"
   (= type "apple") "tree"
   (= type "cherry") "tree"))

(defn plant-type->id [type]
  (cond
   (= type "cherry") 0
   (= type "apple") 1
   (= type "plant-002") 1 ; temp apple tree
   (= type "aronia") 2
   (= type "plant-003") 2 ; temp aronia
   (= type "dandelion") 3
   (= type "plant-001") 3 ; temp dandelion
   (= type "clover") 4))

(def plant-types '("cherry" "apple" "aronia" "dandelion" "clover"))

(def plant-layers '(rhizosphere cover herbaceous shrub vertical tree canopy))

(defn plant-type-id->name [type]
  (nth plant-types type))

(defn layer->spirit-name [layer]
  (cond
   (= layer "canopy") "CanopySpirit"
   (= layer "vertical") "VerticalSpirit"
   (= layer "cover") "CoverSpirit"
   (= layer "tree") "TreeSpirit"
   (= layer "shrub") "ShrubSpirit"
   :else "UnknownSpirit"))
