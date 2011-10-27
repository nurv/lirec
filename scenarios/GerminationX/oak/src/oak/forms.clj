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
  (cons o (discard l (- m 1))))

(defn list-contains? [l i]
  (cond
   (empty? l) false
   (= (first l) i) true
   :else (recur (rest l) i)))

(defn set-cons [o l]
  (if (list-contains? l o)
    l (cons o l)))

(defn make-id-generator [start]
  (let [i (atom start)]
    (fn [] (swap! i inc))))

; force all sequences and maps to un-lazy
(defn doall-recur [s]
;  (println s)
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

(defn remove-ids
  "used before sending mongo object to json"
  [s]
  (cond
   (map? s) (reduce
             (fn [r i]
               (if (not (= (first i) :_id))
                 (merge {(first i)
                         (remove-ids (second i))} r)
                 r))
             {} s)
   (seq? s) (map remove-ids s)
   :else s))

(defn sym-replace2
  "used before serialising to mongo"
  [s]
  (cond
   (symbol? s) (keyword s)
   (map? s) (reduce
             (fn [r i]
               (merge {(first i)
                       (sym-replace2 (second i))} r))
             {} s)
   (seq? s) (map sym-replace2 s)
   :else s))

(defn sym-replace
  "used before serialising to mongo"
  [s]
  (cond
   (symbol? s) (keyword (str "SYM-" s))
   (map? s) (reduce
             (fn [r i]
               (merge {(first i)
                       (sym-replace (second i))} r))
             {} s)
   (seq? s) (map sym-replace s)
   :else s))


(defn sym-unreplace
  "used after serialising in from mongo"
  [s]
  (cond
   (keyword? s) 
   (if (.startsWith (str s) ":SYM-")
     (symbol (.substring (str s) 5))
     s)
   
   (map? s)
   (reduce
    (fn [r i]
      (merge {(first i)
              (sym-unreplace (second i))} r))
    {} s)
   
   (seq? s) (map sym-unreplace s)
   :else s))

(defn current-time []
  (.getTime (java.util.Date.)))

(defn count-items [l i]
  (cond
   (empty? l) 0
   (= (first l) i) (+ 1 (count-items (rest l) i))
   :else (count-items (rest l) i)))

(defn rand-sublist [l n]
  (if (empty? l) ()
      (loop [n n
             o ()]
        (cond
         (zero? n) o
         :else 
         (recur (- n 1) (cons (rand-nth l) o))))))
   