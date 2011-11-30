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
   
(ns oak.game-data
  (:use
   clojure.contrib.duck-streams
   oak.forms
   oak.io
   oak.game-world
   oak.player)
  (:import
   java.util.Date))

(def log-filename "public/log.txt")

(append-spit log-filename (str "\"" (str (Date.)) "\", \"n/a\", \"server started\", \"n/a\"\n"))

(defn make-location [tile pos]
  {:tile tile
   :pos pos})

(defn location->string [loc]
  (if (nil? loc)
    "n/a"
    (str (:x (:tile loc)) ", "
         (:y (:tile loc)) ", "
         (:x (:pos loc)) ", "
         (:y (:pos loc)))))

(defn _game-log [player action location extra-list]
  (append-spit
   log-filename
   (str
    "\"" (str (Date.)) "\", "
    "\"" (:name player) "\", "
    "\"" action "\", "
    (location->string location) ", "
    (apply str (map
                (fn [d]
                  (str "\"" d "\", "))
                extra-list))
    "\n")))

(defn game-log
  ([player action] (_game-log player action nil ()))
  ([player action location]
     (_game-log player action location ()))
  ([player action location extra-list]
     (_game-log player action location extra-list)))
     
