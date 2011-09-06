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

(defn make-plant-msg [code plant for-player-id extra]
  (hash-map
   :type 'plant                      ; it's from a plant         
   :time (current-time)              ; when the message was sent (not used)
   :player for-player-id             ; for getting to right player
   :display 'none                    ; *** will be replaced by name
   :owner (:owner-id plant)          ; *** will be replaced by name
   :from (:type plant)               ; which plant to display
   :emotion 'its-a-plant             ; not used here
   :tile (:tile plant)               ; where the plant is
   :pos (:pos plant)                 ; where the plant is
   :extra extra                      ; random info for text (depending on context)
   :code code                        ; which message is this
   ))

(defn make-spirit-msg [code spirit for-player-id tile pos extra]
  (hash-map
   :type 'spirit                     ; it's from a spirit         
   :time (current-time)              ; when the message was sent (not used)
   :player for-player-id             ; for getting to right player
   :display 'none                    ; *** will be replaced by name
   :owner 'none                      ; not used
   :from (:name spirit)              ; which spirit to display
   :emotion (:highest-emotion spirit); how it's said!
   :tile tile                        ; subject location
   :pos pos                          ; subject location
   :extra extra                      ; random info for text (depending on context)
   :code code                        ; which message is this
   ))

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
