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
   oak.player
   oak.defs
   oak.db
   oak.fatima-names)
  (:require
   clojure.contrib.math))

(defn make-spirit [id remote-agent]
  (println (str "creating spirit for " (remote-agent-name remote-agent)))
  (hash-map
   :version 0
   :tile (make-vec2 0 0)
   :pos (make-vec2 5 5)
   :id id
   :name (remote-agent-name remote-agent)
   :offerings ()
   :emotions (emotion-map)
   :emotionalloc {:tile (make-vec2 0 0)
                  :pos (make-vec2 0 0) }
   :fatactions ()
   :fatemotions ()
   :highest-emotion "NONE"
   :log (make-log 10)))

(defn spirit-highest-emotion [spirit]
  (first
   (reduce
    (fn [r emotion]
      (if (> (second emotion)
             (second r))
        emotion r))
    [:NONE 0]
    (:emotions spirit))))
          
(defn spirit-count [spirit]
  (println (str "emotions: " (count (:emotions spirit))))
  (println (str "fatemotions: " (count (:fatactions spirit))))
  (println (str "fatactions: " (count (:fatemotions spirit)))))

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

(defn spirit-pick-helper-player [player-id needed-layer]
  ; pick any player - should be based on needed layer
  (let [found (db-get-random-one :players {:id {:$ne player-id}
                                           :layer (layer->num needed-layer)})]
    (if (empty? found)
      false
      (first found))))

(defn spirit-ask-for-help [spirit plant diagnosis]
  (let [player (spirit-pick-helper-player (:owner-id plant) (:layer plant))]
    (if (and player
             (> (count (:needed_plants diagnosis)) 0))
      (do
        (modify :log
                (fn [log]
                  (log-add-msg
                   (log-add-msg
                    log
                    (make-spirit-msg ; ask for help
                     :needs_help
                     spirit
                     (:id player)
                     (:tile plant)
                     (:pos plant)
                     (list
                      (:owner-id plant)
                      (:type plant)
                      (rand-nth (:needed_plants diagnosis)))))
                   ; tell owner we are asking
                   (make-spirit-msg
                    :ive_asked_x_for_help
                    spirit
                    (:owner-id plant)
                    (:tile plant)
                    (:pos plant)
                    (list
                     (:id player)
                     (:type plant)
                     (:state plant)))))
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
                  :your_plant_doesnt_like
                  spirit
                  (:owner-id plant)
                  (:tile plant)
                  (:pos plant)
                  (list
                   (:owner-id harmful)
                   (:type plant)
                   (:type harmful)))))
              (if (> (count (:needed_plants diagnosis)) 0)
                (log-add-msg
                 log
                 (make-spirit-msg
                  :your_plant_needs
                  spirit
                  (:owner-id plant)
                  (:tile plant)
                  (:pos plant)
                  (list
                   (:type plant)
                   (rand-nth (:needed_plants diagnosis)))))
                (do
                  (println (:type plant) " doesn't need any other plants?")
                  log))))
          spirit))

(defn spirit-diagnose [spirit plant rules tiles]
  (let [diagnosis
        (plant-diagnose
         plant
         (tile-get-neighbours (:tile spirit) (:id plant) (:pos plant) tiles)
         rules)]
    (spirit-send-diagnosis
     (if (< 5 (rand-int 10)) ; sometimes ask for help
       (spirit-ask-for-help spirit plant diagnosis)
       spirit)
     diagnosis plant rules)))

(defn make-praise-msg [type spirit plant]
  (make-spirit-msg
   type
   spirit
   (:owner-id plant)
   (:tile plant)
   (:pos plant)
   (list (:type plant))))

(defn spirit-praise [spirit plant]
  (modify :log
          (fn [log]
            (log-add-msg
             log
             ; we can:t exactly be sure why the fatima agent
             ; has triggered the praise action, but we can make
             ; an educated guess by looking at the plant
             
             ; if it:s not the same type as the spirit
             (if (not (= (:name spirit)
                         (layer->spirit-name (:layer plant))))
               (make-praise-msg :spirit_helper_praise spirit plant)
               ; it:s the same type
               (cond
                (= (:state plant) "grow-a")
                (make-praise-msg :spirit_growing_praise spirit plant)

                (= (:state plant) "fruit-a")
                (make-praise-msg :spirit_flowering_praise spirit plant)
                
                (= (:state plant) "fruit-c")
                (make-praise-msg :spirit_fruiting_praise spirit plant)

                ; i give up!
                :else (make-praise-msg :spirit_general_praise spirit plant)))))
          spirit))

(defn spirit-update-from-actions [spirit tiles rules]
  (modify
   :fatactions (fn [fatactions] ()) ; clear em out
   (reduce
    (fn [spirit action]
      (let [toks (.split (:msg action) " ")
            type (nth toks 0)
            fullname (nth toks 1)
            id (fatima-name->id fullname)
            subject (fatima-name-remove-id fullname)
            reason (fatima-subject->reason subject)]
        (if id
          (let [e (tiles-find-entity tiles id)]
            (if e
              (modify :pos (fn [pos] (:pos e))
                      (cond
                       ;(= type "look-at") (spirit-looking-at spirit tile e)
                       (and
                        (= type "diagnose")
                        (not (= reason "detriment")))
                       ; don't want to diagnose plants that are detriments to our plants
                       (spirit-diagnose spirit e rules tiles)
                       (= type "praise") (spirit-praise spirit e)
                       :else spirit))
              spirit)) ; can happen if we have moved away from the tile
          (do
            (println "could not find id from fatima name" fullname)
            spirit))))
    spirit
    (:fatactions spirit))))

(defn spirit-update-highest-emotion [spirit]
  (modify
   :highest-emotion
   (fn [e]
     (spirit-highest-emotion spirit))
   spirit))

(defn spirit-clear
  "clear stuff that need"
  [spirit]
  (modify :log (fn [log] (make-log 10))
          spirit
          ;(modify :offerings (fn [offerings]
          ;                     (println "clearing offering")
          ;                     ()) spirit)
          ))

(defn spirit-update-location
  "read the location from the fatima agent"
  [spirit remote-agent]
  (modify :tile (fn [t] (:tile remote-agent)) spirit))
  
(defn spirit-update [spirit remote-agent tiles rules]
  (spirit-update-highest-emotion
   (spirit-update-from-actions
    (spirit-update-emotionalloc
     (spirit-update-emotions
      (spirit-update-fatdebug
       (spirit-update-location
        (spirit-clear spirit)
        remote-agent)
       remote-agent)
      remote-agent)
     remote-agent tiles) tiles rules)))
          

  