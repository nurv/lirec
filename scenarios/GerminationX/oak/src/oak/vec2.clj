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

(ns oak.vec2)

(defrecord vec2 [x y])

(defn vec2-x [vec2] (:x vec2))
(defn vec2-y [vec2] (:y vec2))

(defn make-vec2 [x y]
  (vec2. x y))

(defn vec2-eq? [vec2 other]
  (and (= (vec2-x vec2) (vec2-x other))
       (= (vec2-y vec2) (vec2-y other))))