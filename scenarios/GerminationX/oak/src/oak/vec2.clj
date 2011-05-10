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

(ns oak.vec2
  (:require clojure.contrib.math))

(defn make-vec2 [x y]
  (hash-map :x x :y y))

(defn vec2-add [a b]
  (make-vec2 (+ (:x a) (:x b))
             (+ (:y a) (:y b))))

(defn vec2-sub [a b]
  (make-vec2 (- (:x a) (:x b))
             (- (:y a) (:y b))))

(defn vec2-mul [a b]
  (make-vec2 (* (:x a) b)
             (* (:y a) b)))

(defn vec2-div [a b]
  (make-vec2 (/ (:x a) b)
             (/ (:y a) b)))

(defn vec2-mag [v]
  (Math/sqrt (+ (* (:x v) (:x v))
                (* (:y v) (:y v)))))

(defn vec2-dist [a b]
  (vec2-mag (vec2-sub a b)))

(defn vec2-eq? [a b]
  (and (= (:x a) (:x b))
       (= (:y a) (:y b))))