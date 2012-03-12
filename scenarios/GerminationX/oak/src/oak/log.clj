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
   :type "plant"                     ; it:s from a plant         
   :time (current-time)              ; when the message was sent (not used)
   :player for-player-id             ; for getting to right player
   :display "none"                   ; *** will be replaced by name
   :owner (:owner-id plant)          ; *** will be replaced by name
   :from (:type plant)               ; which plant to display
   :emotion :its-a-plant             ; not used here
   :tile (:tile plant)               ; where the plant is
   :pos (:pos plant)                 ; where the plant is
   :extra extra                      ; random info for text (depending on context)
   :code code                        ; which message is this
   ))

(defn make-spirit-msg [code spirit for-player-id tile pos extra]
  (hash-map
   :type "spirit"                    ; it's from a spirit         
   :time (current-time)              ; when the message was sent (not used)
   :player for-player-id             ; for getting to right player
   :display "none"                   ; *** will be replaced by name
   :owner "none"                     ; not used
   :from (:name spirit)              ; which spirit to display
   :emotion (:highest-emotion spirit); how it's said
   :tile tile                        ; subject location
   :pos pos                          ; subject location
   :extra extra                      ; random info for text (depending on context)
   :code code                        ; which message is this
   ))

(defn make-note [code options]
  (hash-map
   :code code
   :options options
   :answer false))

(defn make-log [max]
  (hash-map
   :msgs ()
   :max max
   :notes ()
   :one-time-msgs ()))

(defn log-add-msg [log msg]
  (if (list-contains? (:one-time-msgs log) (:code msg))
    log
    (modify
     :msgs
     (fn [msgs]
       (max-cons msg msgs (:max log)))
     (if (.startsWith (:code msg) "one_time")
       (modify
        :one-time-msgs
        (fn [ot]
          (cons (:code msg) ot))
        log)
       log))))

(defn log-add-msg-ignore-one-time [log msg]
  (modify
   :msgs
   (fn [msgs]
     (max-cons msg msgs (:max log)))
   log))

(defn log-contains-msg? [log code]
  (reduce
   (fn [r msg]
     (if (and (not r) (= (:code msg) code))
       true r))
   false
   (:msgs log)))

(defn log-find-msgs [log code]
  (reduce
   (fn [r msg]
     (if (= (:code msg) code)
       (cons msg r) r))
   ()
   (:msgs log)))

(defn log-remove-msgs [log code]
  (modify
   :msgs
   (fn [msgs]
     (filter
      (fn [msg]
        (not (= (:code msg) code)))
      msgs))
   log))

(defn log-add-note [log note]
  (modify
   :notes
   (fn [notes]
     (cons note notes))
   log))

; a note has been answered, record the answer
(defn log-answer-note [log code index]
  (modify
   :notes
   (fn [notes]
     (map
      (fn [note]
        (if (= (:code note) code)
          (modify
           :answer
           (fn [a]
             (if (and
                  (>= index 0)
                  (< index (count (:options note))))
               (nth (:options note) index)
               :error))
           note)
          note))
      notes))
   log))
