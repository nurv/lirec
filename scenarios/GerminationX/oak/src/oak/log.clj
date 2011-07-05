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

(ns oak.log
  (:use
   oak.forms))

(def msg-picked 1)
(def msg-ill 2)

(defn make-msg [from to msg-id subjects]
  (hash-map
   :time (current-time)
   :from from
   :to to
   :msg-id msg-id
   :subjects subjects))

(defn make-log [max]
  (hash-map
   :msgs ()
   :max max))

(defn log-add-msg [log msg]
  (modify
   :msgs
   (fn [msgs]
     (max-cons msg msgs (:max log)))
   log))
