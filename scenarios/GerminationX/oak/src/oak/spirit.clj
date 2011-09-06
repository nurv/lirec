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


(ns oak.spirit
  (:use
   oak.io
   oak.vec2
   oak.remote-agent
   oak.forms
   oak.tile
   oak.log
   oak.plant
   oak.player)
  (:require
   clojure.contrib.math))

(defn emotion-map []
  { "LOVE" 0
    "HATE" 0
    "HOPE" 0
    "FEAR" 0
    "SATISFACTION" 0
    "RELIEF" 0
    "FEARS-CONFIRMED" 0
    "DISAPPOINTMENT" 0
    "JOY" 0
    "DISTRESS" 0
	"HAPPY-FOR" 0
	"PITTY" 0
	"RESENTMENT" 0
	"GLOATING" 0
    "PRIDE" 0
	"SHAME" 0
	"GRATIFICATION" 0
	"REMORSE" 0
	"ADMIRATION" 0
	"REPROACH" 0
	"GRATITUDE" 0
	"ANGER" 0 })

(defn make-spirit [id remote-agent]
  (println (str "creating spirit for " (remote-agent-name remote-agent)))
  (hash-map
   :version 0
   :tile (make-vec2 0 0)
   :pos (make-vec2 5 5)
   :id id
   :name (remote-agent-name remote-agent)
   :emotions (emotion-map)
   :emotionalloc {:tile (make-vec2 0 0)
                  :pos (make-vec2 0 0) }
   :fatactions '()
   :fatemotions '()
   :highest-emotion 'NONE
   :log (make-log 10)))

(defn spirit-highest-emotion [spirit]
  (first
   (reduce
    (fn [r emotion]
      (if (> (second emotion)
             (second r))
        emotion r))
    ['NONE 0]
    (:emotions spirit))))
          
(defn spirit-count [spirit]
  (println (str "emotions: " (count (:emotions spirit))))
  (println (str "fatemotions: " (count (:fatactions spirit))))
  (println (str "fatactions: " (count (:fatemotions spirit)))))

; convert foofooname#999# to 999
(defn fatima-name->id [name]
  (if (.contains name "#")
    (parse-number (.substring name
                              (+ 1 (.indexOf name "#"))
                              (.length name)))
    false))

(defn spirit-update-emotionalloc [spirit remote-agent tiles]
  (modify
   :emotionalloc ; get the object causing the highest emotion
   (fn [emotionalloc]
     (first
      (reduce
       (fn [r emotion]
         (let [e (:attrs emotion)]
           (if e
             (let [intensity (parse-float (:intensity e))]
               (if (> intensity (second r))
                 (let [id (fatima-name->id (:direction e))]
                   (if id
                     (let [f (tiles-find-entity-with-tile tiles id)]
                       (if f
                         (list {:tile (:pos (first f))
                                :pos (:pos (second f))} intensity)
                         r))
                     r))
                 r))
             r)))
       (list emotionalloc 0)
       (:content (remote-agent-emotions remote-agent)))))
   spirit))

(defn spirit-update-emotions [spirit remote-agent]
  (modify
   :emotions ; process emotions into a useable form
   (fn [emotions]
     (reduce
      (fn [r emotion]
        (let [e (:attrs emotion)]
          (if e
            (merge r {(:type e)
                      (+ (parse-float (:intensity e))
                         (get r (:type e)))})
            r)))
      (emotion-map)
      (:content (remote-agent-emotions remote-agent))))
   spirit))

(defn spirit-update-fatdebug [spirit remote-agent]
  (modify
   :fatemotions ; copy the fatima stuff for debug output
   (fn [emotions]
     (remote-agent-emotions remote-agent))
   (modify
    :fatactions
    (fn [actions]
      (remote-agent-done remote-agent))
    spirit)))

(defn spirit-pick-helper-player [player-id players]
  (let [selection
        (filter
         (fn [p]
           (and (not (= player-id (:id p)))
                (> (:seeds-left p) 0)))
         players)]
    (if (> (count selection) 0)
      (rand-nth selection)
      false)))

(defn spirit-ask-for-help [spirit plant diagnosis players]
  (let [player (spirit-pick-helper-player (:owner-id plant) players)]
    (if player
      (do
        (modify :log
                (fn [log]
                  (log-add-msg
                   log
                   (make-spirit-msg
                    'needs_help
                    spirit
                    (:id player)
                    (:tile spirit)
                    (:pos plant)
                    (list
                     (player-list-id->player-name players (:owner-id plant))
                     (:type plant)
                     (rand-nth (:needed_plants diagnosis))))))
                spirit))
        spirit)))
             
(defn spirit-send-diagnosis [spirit diagnosis plant rules]
  (modify :log
          (fn [log]
            (if (and
                 (> (count (:harmful_plants diagnosis)) 0)
                 (< 5 (rand-int 10)))
              (let [harmful (rand-nth (:harmful_plants diagnosis))]
                (log-add-msg
                 log
                 (make-spirit-msg
                  'your_plant_doesnt_like
                  spirit
                  (:owner-id plant)
                  (:tile spirit)
                  (:pos plant)
                  (list
                   (:owner-id harmful)
                   (:type plant)
                   (:type harmful)))))
              (if (> (count (:needed_plants diagnosis)) 0)
                (log-add-msg
                 log
                 (make-spirit-msg
                  'your_plant_needs
                  spirit
                  (:owner-id plant)
                  (:tile spirit)
                  (:pos plant)
                  (list
                   (:type plant)
                   (rand-nth (:needed_plants diagnosis)))))
                log)))
          spirit))

(defn spirit-diagnose [spirit plant rules players tiles]
  (let [diagnosis
        (plant-diagnose
         plant
         (tile-get-neighbours (:tile spirit) (:id plant) (:pos plant) tiles)
         rules)]
    (spirit-ask-for-help
     (spirit-send-diagnosis spirit diagnosis plant rules)
     plant diagnosis players)))
              
(defn spirit-update-from-actions [spirit tiles rules players]
  (modify
   :fatactions (fn [fatactions] '()) ; clear em out
   (reduce
    (fn [spirit action]
      (let [type (nth (.split (:msg action) " ") 0)
            subject (nth (.split (:msg action) " ") 1)
            id (fatima-name->id subject)]
        (if id
          (let [e (tiles-find-entity tiles id)]
            (if e
              (modify :pos (fn [pos] (:pos e))
                      (cond
                       ;(= type "look-at") (spirit-looking-at spirit tile e)
                       (= type "diagnose") (spirit-diagnose spirit e rules players tiles)
                       :else spirit))
              spirit)) ; can happen if we have moved away from the tile
          (do
            (println "could not find id from fatima name" subject)
            spirit))))
    spirit
    (:fatactions spirit))))

(defn spirit-update-highest-emotion [spirit]
  (modify
   :highest-emotion
   (fn [e]
     (spirit-highest-emotion spirit))
   spirit))

(defn spirit-update [spirit remote-agent tiles rules players]
  (spirit-update-highest-emotion
   (spirit-update-from-actions
    (spirit-update-emotionalloc
     (spirit-update-emotions
      (spirit-update-fatdebug
       (modify :log
               (fn [log]
                 (make-log 10))
               (modify :tile (fn [t] (:tile remote-agent)) spirit))
       remote-agent)
      remote-agent)
     remote-agent tiles) tiles rules players)))
          
(comment println
         (map
          (fn [relation]
            (list (:tag relation)
                  (map
                   (fn [chunk]
                     (list (:tag chunk) (:content chunk)))
                   (:content relation))))
          (:content (remote-agent-relations remote-agent))))

  