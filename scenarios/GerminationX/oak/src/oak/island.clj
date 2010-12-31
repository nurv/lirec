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

(ns oak.island
  (:require [org.danlarkin.json :as json]))

(defn read-islands [path]
  (json/encode-to-str
   (map
    (fn [file]
      (let [toks (.split (str file) "-")]
        {:name (str file)
         :position
         {:x (nth toks 2)
          :y (first (.split (nth toks 3) "\\."))}}))
    (filter #(re-find #"\.png$" (str %)) (file-seq (java.io.File. path))))))

