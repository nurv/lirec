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

(ns oak.forms)

(defn modify [what f thing]
  (merge thing {what (f (what thing))}))

(defn discard [l n]
  (cond
   (empty? l) '()
   (= 0 n) '()
   :else (cons (first l) (discard (rest l) (- n 1)))))

(defn max-cons [o l m]
  (cons o (discard l m)))

(defn make-id-generator [start]
  (let [i (atom start)]
    (fn [] (swap! i inc))))

; force all sequences and maps to un-lazy
(defn doall-recur [s]
  (cond
   (map? s) (reduce
             (fn [r i]
               (merge {(first i)
                       (doall-recur (second i))} r))
             {} s)
   (seq? s) (doall
             (map doall-recur
                  s))
   :else s))

(defn current-time []
  (.getTime (java.util.Date.)))