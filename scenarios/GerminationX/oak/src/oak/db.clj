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

(ns oak.db
  (:use
   oak.tile
   oak.vec2
   oak.forms
   oak.profile
   somnium.congomongo.coerce
   somnium.congomongo))

(mongo! :db "oak")

;-------------------------------------------------------
; general purpose bits

(defn db-build-collection!
  "build an entire collection from a list"
  [collection l]
  (doseq [item l]
    (insert! collection item)))

(defn db-add! [coll item]
  (insert! coll item))

(defn db-get [coll where]
   (prof :db-get (fetch coll :where where)))

(defn db-get-random-one [coll where]
  (prof :db-get (fetch coll
                       :limit 1
                       :skip (rand-int (- (.count (get-coll coll)) 1))
                       :where where)))

(def db-limit 200) ; max things in mem

(defn db-map!
  "run f on each item in the collection. loads in chunks
   to avoid blowing memory"
  [f coll]
  (loop [skip 0]
    (let [limit db-limit 
          items (prof :db-map-fetch (fetch coll :limit limit :skip skip))]
      (when (not (empty? items))
        (doseq [item items]
          (let [new (f item)]
            (when (not (= new item)) ; if it's changed
              (prof :db-map-update
                    (update! coll item new)))))
        (recur (+ skip limit))))))

(defn db-partial-map!
  "run f on a range of items in the collection"
  [f coll skip limit]
  (let [skip (mod (int skip) (.count (get-coll coll)))
        items (prof :db-map-fetch (fetch coll :limit limit :skip skip))]
    (when (not (empty? items))
      (doseq [item items]
        (let [new (f item)]
          (when (not (= new item)) ; if it's changed
            (prof :db-map-update
                  (update! coll item new))))))))


(defn db-reduce
  "run f on each item in the collection"
  [f ret coll]
  (loop [skip 0 ret ret]
    (let [limit db-limit ; so we can store more than RAM
          items (prof :db-reduce-fetch (fetch coll :limit limit :skip skip))]
      (if (not (empty? items))
        (recur (+ skip limit)
               (prof :db-reduce-work (reduce f ret items)))
        ret))))

(defn db-partial-reduce
  "run f on a subset of items in the collection"
  [f ret coll skip limit]
  (let [skip (mod (int skip) (.count (get-coll coll)))
        items (prof :db-reduce-fetch (fetch coll :limit limit :skip skip))]
    (if (not (empty? items))
      (prof :db-reduce-work (reduce f ret items))
      ret)))

(defn db-find-update! [f coll where]
  (prof
   :db-search-update
   (let [item (fetch-one coll :where where)]
     (when item
       (let [new (f item)]
         (when (not (= item new))
           (update! coll item new)))))))

(defn db-update! [coll old new]
  (when (not (= old new))
    (prof
     :db-update
     (update! coll old new))))

;-------------------------------------------------------
; oak specific bits

(defn db-build! [game-world]
  (println (:players game-world))

  (db-build-collection! :players (:players game-world))
  (add-index! :players [:id])
  (db-build-collection! :tiles
                        (map
                         (fn [tile]
                           (merge
                            tile
                            {:index (str (:x (:pos tile)) ","
                                         (:y (:pos tile)))}))
                         (:tiles game-world)))
  (add-index! :tiles [:index]))


