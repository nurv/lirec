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

(ns oak.core
  (:use
   compojure.core
   clojure.contrib.duck-streams
   clojure.contrib.trace
   ring.adapter.jetty
   ring.middleware.file
   oak.fatima-world
   oak.fatima-bridge
   oak.remote-agent
   oak.io
   oak.island
   oak.game-world
   oak.vec2
   oak.plant
   oak.tile
   oak.forms
   oak.player
   oak.logging
   oak.db
   oak.defs
   oak.profile
   oak.log
   oak.game-data
   oak.id-gen)
  (:import
   java.util.concurrent.Executors
   java.util.Date)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as dojson])) 

(check-&-build-id-gen)

(def fatima-world
     (ref
      (make-world
       46874
       "data/characters/minds/language/agent/en/language-set-1"
       "data/characters/minds/Actions.xml"
       (list))))

; ****************************************************************
; Uncomment the two lines below and run once to create a new world
; ****************************************************************
;(def my-game-world (ref (make-game-world 100 1)))
;(game-world-db-build! (sym-replace2 (deref my-game-world)))

(def my-game-world (ref (make-empty-game-world)))

(defn run []
  (let [time (/ (.getTime (java.util.Date.)) 1000.0)]
    (dosync (ref-set fatima-world
                     (doall-recur
                      (world-run
                       (game-world-sync->fatima
                        (deref fatima-world)
                        (deref my-game-world)
                        time) time))))
    (dosync (ref-set my-game-world
                     (doall-recur
                      (game-world-update
                       (game-world-sync<-fatima
                        (deref my-game-world)
                        (deref fatima-world))
                       time server-tick))))))

(defn tick []
  (Thread/sleep (* server-tick 1000))
  (try
    (profile (run))
    (catch Exception e
      (println "Oops ... an error ocurred.")
      (.printStackTrace e))
    (finally))
  (recur))

;(tick)

(defn json [thing]
  (dojson/encode-to-str (remove-ids thing)))

(defroutes main-routes
  (GET "/login/:name/:fbid/:iefix" [name fbid iefix]
       (let [id (game-world-find-player-id
                 (deref my-game-world) name)]
         (cond
          (not id)
          (do
            (dosync
             (ref-set my-game-world
                      (game-world-add-player
                       (deref my-game-world) name fbid)))
            (let [player (game-world-find-player
                          (deref my-game-world)
                          (game-world-find-player-id
                           (deref my-game-world) name))]
              (game-log player "registered")
              (json player)))
          :else
          (let [player (game-world-find-player
                        (deref my-game-world) id)]
            (game-log player "login")
            (json player)))))
       
  (GET "/pull/:player-id/:tilex/:tiley/:iefix"
       [player-id tilex tiley iefix]
       (let [tiles (game-world-get-tile-with-neighbours
                     (deref my-game-world)
                     (make-vec2 (parse-number tilex)
                                (parse-number tiley)))
             player (game-world-find-player
                     (deref my-game-world)
                     (parse-number player-id))
             player-layer (player-get-allowed-layer player)]
         (json
          {:player player
           :tiles (if player
                    (map
                     (fn [tile]
                       ; take off the fruit we are not allowed to pick
                       (tile-strip tile player-layer))
                     tiles)
                    tiles)
           :spirits
           (:spirits (deref my-game-world))})))

  (GET "/get-msgs/:id/:iefix" [id iefix]
       (let [id (parse-number id)]
         (if (< id 1)
           (json (:msgs (:log (deref my-game-world))))
           (do
             (let [player (game-world-find-player
                           (deref my-game-world) id)]
               (if player
                 (json (:msgs (:log player)))
                 (json {:error (str "no player " id " found")})))))))
           
  (GET "/make-plant/:tilex/:tiley/:posx/:posy/:type/:owner-id/:size/:fruit-id/:iefix"
       [tilex tiley posx posy type owner-id size fruit-id iefix]
       (let [tile-pos (make-vec2 (parse-number tilex) (parse-number tiley))
             pos (make-vec2 (parse-number posx) (parse-number posy))
             owner-id (parse-number owner-id)
             fruit-id (parse-number fruit-id)
             player (game-world-find-player
                     (deref my-game-world) owner-id)]
         (game-log player "planted" (make-location tile-pos pos) (list type))
         (if (player-has-fruit? player fruit-id)
           (dosync
            (ref-set my-game-world
                     (game-world-modify-player
                      (game-world-add-entity
                       (deref my-game-world)
                       tile-pos
                       (make-plant
                        ((:id-gen (deref my-game-world)))
                        tile-pos pos type owner-id size)
                       (/ (.getTime (java.util.Date.)) 1000.0)
                       server-tick)
                      owner-id
                      (fn [player]
                      (player-inc-plant-count
                       (player-remove-fruit player fruit-id))))))
           (game-log player "plant-error!"))
         (json '("ok"))))

  (GET "/pick/:tilex/:tiley/:plant-id/:player-id/:iefix" [tilex tiley plant-id player-id iefix]
       (let [player-id (parse-number player-id)
             tile-pos (make-vec2 (parse-number tilex) (parse-number tiley))
             plant-id (parse-number plant-id)
             plant (game-world-find-plant
                    (deref my-game-world) tile-pos plant-id)
             player (game-world-find-player
                     (deref my-game-world) player-id)]
         
         (if (game-world-can-player-pick?
              (deref my-game-world) player-id)
           (do
             (game-log player "pick" (make-location tile-pos (:pos plant))
                       (list (:type plant)
                             (game-world-id->player-name
                              (deref my-game-world)
                              (:owner-id plant))))
             (dosync
              (ref-set my-game-world
                       (game-world-modify-tile
                        (game-world-player-pick
                         (deref my-game-world)
                         player-id tile-pos plant-id)
                        tile-pos
                        (fn [tile]
                          (tile-modify-entity
                           tile plant-id
                           (fn [plant]
                             (plant-picked
                              plant
                              (game-world-find-player
                               (deref my-game-world)
                               player-id))))))))
             (json {:ok true})) 
           (json {:ok false}))))
         
  (GET "/spirit-sprites/:name/:iefix" [name iefix]
       ;(update-islands (str "./" name) (str "./" name))
       (read-islands (str "./public/" name)))

  (GET "/perceive/:iefix" [iefix]
       (world-perceive-all (deref fatima-world))
       (json '("ok")))

  (GET "/gift/:player-id/:fruit-id/:receiver-id/:iefix"
       [player-id fruit-id receiver-id iefix]
       (let [player-id (parse-number player-id)
             fruit-id (parse-number fruit-id)
             receiver-id (parse-number receiver-id)
             tile (make-vec2 0 0) ; (for the messages) doesn't make sense in this context
             pos (make-vec2 0 0) ; hmmm
             sender (game-world-find-player
                        (deref my-game-world)
                        player-id)
             receiver (game-world-find-player
                        (deref my-game-world)
                        receiver-id)]
         (dosync
          (let [fruit (player-get-fruit sender fruit-id)]
            (if fruit
              (do
                (game-log sender "player-gift" nil
                          (list (:name receiver) (:type fruit)))
                ; this is ridiculous
                (ref-set my-game-world
                         (game-world-modify-player ; modify the sender
                          (game-world-modify-player ; modify the receiver
                           (deref my-game-world) receiver-id
                           (fn [player]
                             (modify
                              :seeds
                              (fn [fruits]
                                (max-cons fruit fruits max-player-fruit))
                              (player-add-msg ; add the recieved message
                               player
                               (make-spirit-msg ; make the message
                                :gift_received ; this message doesn't get processed
                                (game-world-find-spirit
                                 (deref my-game-world)
                                 (layer->spirit-name (:layer fruit)))
                                receiver-id tile pos (list (:name receiver)
                                                           (:name sender) (:type fruit)))))))
                          player-id
                          (fn [player]
                            (player-remove-fruit
                             (player-add-msg ; add the sent message
                              player
                              (make-spirit-msg ; make the message
                               :gift_sent ; this message doesn't get processed
                               (game-world-find-spirit
                                (deref my-game-world)
                                (layer->spirit-name (:layer fruit)))
                               player-id tile pos (list (:name sender)
                                                        (:name receiver) (:type fruit))))
                             fruit-id))))
                (json '("ok")))
              (json '("fail")))))))

  (GET "/answer/:player-id/:code/:index/:iefix"
       [player-id code index iefix]
       (let [index (parse-number index)]
         (dosync
          (ref-set my-game-world
                   (game-world-modify-player
                    (deref my-game-world)
                    (parse-number player-id)
                    (fn [player]
                      (modify
                       :log
                       (fn [log]
                         (log-answer-note log code index))
                       player)))))
         (json '("ok"))))
  
  (GET "/offering/:player-id/:fruit-id/:spirit/:iefix"
       [player-id fruit-id spirit iefix]
       (let [player-id (parse-number player-id)
             fruit-id (parse-number fruit-id)
             player (game-world-find-player
                     (deref my-game-world)
                     player-id)]
         (dosync
          (let [fruit (player-get-fruit player fruit-id)]
            (if fruit
              (do
                (game-log player "spirit-gift" nil
                          (list spirit (:type fruit)))
                (ref-set my-game-world
                         (game-world-modify-player
                          (game-world-modify-spirit
                           (deref my-game-world) spirit
                           (fn [spirit]
                             (modify
                              :offerings
                              (fn [offerings]
                                (max-cons fruit offerings 5))
                              spirit)))
                          player-id
                          (fn [player]
                            (player-remove-fruit player fruit-id))))
                (json '("ok")))
              (json '("fail")))))))
  
  (route/not-found "<h1>Page not found</h1>"))
  
(let [pool (Executors/newFixedThreadPool 2)
      tasks (list
             (fn []
               (tick))
             (fn []
               (run-jetty (wrap-file main-routes "public") {:port 8001}))
             )]
  (doseq [future (.invokeAll pool tasks)]
    (.get future))
  (.shutdown pool))

