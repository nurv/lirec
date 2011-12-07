;; Copyright (C) 2011 FoAM vzw
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

(ns oak.fatima-names
  (:use
   oak.io))

; convert foofooname#999# to 999
(defn fatima-name->id [name]
  (if (.contains name "#")
    (parse-number
     (.substring name
                 (+ 1 (.indexOf name "#"))
                 (.length name)))
    false))

; convert foofooname#999# to foofooname
(defn fatima-name-remove-id [name]
  (if (.contains name "#")
    (.substring name
                0
                (.indexOf name "#"))
    name))

; shrub-detriment => detriment
; shrub-ill-a => ill-a
(defn fatima-subject->reason [name]
  (if (.contains name "-")
    (.substring name
                (+ 1 (.indexOf name "-"))
                (.length name))
    "unknown"))

; shrub-detriment => shrub
; shrub-ill-a => shrub
(defn fatima-subject->name [name]
  (if (.contains name "-")
    (.substring name
                0
                (.indexOf name "-"))
    "unknown"))

