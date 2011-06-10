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
   oak.tile)
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

(defn make-spirit [remote-agent]
  (println (str "creating spirit for " (remote-agent-name remote-agent)))
  (hash-map
   :version 0
   :tile (make-vec2 0 0)
   :pos (make-vec2 5 5)
   :name (remote-agent-name remote-agent)
   :emotions (emotion-map)
   :emotionalloc (make-vec2 5 5)
   :fatactions '()
   :fatemotions '()))

(defn spirit-count [spirit]
  (println (str "emotions: " (count (:emotions spirit))))
  (println (str "fatemotions: " (count (:fatactions spirit))))
  (println (str "fatactions: " (count (:fatemotions spirit)))))

; convert foofooname#999# to 999
(defn fatima-name->id [name]
  (if (.contains name "#")
    (parse-number (.substring name
                              (+ 1 (.indexOf name "#"))
                              (- (.length name) 1)))
    false))

(defn spirit-update [spirit remote-agent tile]

;  (println remote-agent)

  ; for the moment take a straight copy of actions and emotions
  (let [spirit
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
                           (let [obj (tile-find-entity tile id)]
                             (if obj
                               (list (:pos obj) intensity)
                               r))
                           r))
                       r))
                   r)))
             (list emotionalloc 0)
             (:content (remote-agent-emotions remote-agent)))))
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
          (modify
           :fatemotions ; copy the fatima stuff for debug output
           (fn [emotions]
             ;(println emotions)
             (remote-agent-emotions remote-agent))
           (modify
            :fatactions
            (fn [actions]
              (remote-agent-done remote-agent))
            spirit))))]
    
;    (println (:emotionalloc spirit))
    
    ; if we have some actions
    (if (not (empty? (:fatactions spirit)))
      (let [latest-action (first (:fatactions spirit))
            latest-subject (nth (.split (:msg latest-action) " ") 1)
            id (fatima-name->id latest-subject)]
        (if id
          (let [e (tile-find-entity tile id)]
            (if e
              (modify :pos (fn [pos] (:pos e)) spirit)
              spirit))
          spirit))
      spirit)))
          
          
(comment println
         (map
          (fn [relation]
            (list (:tag relation)
                  (map
                   (fn [chunk]
                     (list (:tag chunk) (:content chunk)))
                   (:content relation))))
          (:content (remote-agent-relations remote-agent))))

  