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
   oak.profile)
  (:import
   java.util.concurrent.Executors
   java.util.Date)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as dojson])) 

(def state-filename "state.txt")
(def log-filename "public/log.txt")
(def game-world-tick 1)

(def fatima-world
     (ref
      (make-world
       46874
       "data/characters/minds/language/agent/en/language-set-1"
       "data/characters/minds/Actions.xml"
       (list))))

(def my-game-world (ref (game-world-load state-filename)))
;(def my-game-world (ref (make-game-world 100 1)))
(game-world-db-build! (sym-replace2 (deref my-game-world)))
;(game-world-save (deref my-game-world) "test.txt")

(def my-game-world (ref (make-empty-game-world)))

(append-spit log-filename (str (str (Date.)) " server started\n"))

(mail "dave@fo.am" "friendly message from gx" "I have started!")

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
                       time 1))))))

(defn tick []
  (Thread/sleep 1000)
  ;(println ".")
  ;(game-world-print (deref my-game-world))

  (try
    (profile (run))
    ;(throw (Exception. "Testing error catching"))
    (catch Exception e
      (println "Oops ... an error ocurred.")
      (.printStackTrace e)
      (mail "dave@fo.am"
            "friendly message from gx"
            (.getStackTrace e))
      )
    (finally
     ))
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
            (append-spit
             log-filename
             (str (Date.) " new player " name " has registered\n"))
            (mail "dave@fo.am" (str "gx: new player" name) "")
            (dosync
             (ref-set my-game-world
                      (game-world-add-player
                       (deref my-game-world) name fbid)))
            (json
             (game-world-find-player
              (deref my-game-world)
              (game-world-find-player-id
               (deref my-game-world) name))))
          :else
          (do
            (append-spit
             log-filename
             (str (Date.) " " name " is logging in\n"))
            (json
             (game-world-find-player
              (deref my-game-world) id))))))

  (GET "/pull/:player-id/:tilex/:tiley/:iefix"
       [player-id tilex tiley iefix]
       (let [tiles (game-world-get-tile-with-neighbours
                     (deref my-game-world)
                     (make-vec2 (parse-number tilex)
                                (parse-number tiley)))]
         (json
          {:player
           (game-world-find-player
            (deref my-game-world)
            (parse-number player-id))
           :tiles
           (map tile-strip tiles)
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
             fruit-id (parse-number fruit-id)]
         (append-spit
          log-filename
          (str
           (str (Date.)) " " (game-world-id->player-name
                              (deref my-game-world)
                              owner-id) " has created a " type " at tile "
                              tilex "," tiley " position " posx "," posy "\n"))
         (if (player-has-fruit?
              (game-world-find-player
               (deref my-game-world) owner-id)
              fruit-id)
           (dosync
            (ref-set my-game-world
                     (game-world-modify-player
                      (game-world-add-entity
                       (deref my-game-world)
                       tile-pos
                       (make-plant
                        ((:id-gen (deref my-game-world)))
                        tile-pos pos type owner-id size))
                      owner-id
                      (fn [player]
                      (player-inc-plant-count
                       (player-remove-fruit player fruit-id))))))
           (append-spit
            log-filename
            (str
             (str (Date.)) " " (game-world-id->player-name
                                (deref my-game-world)
                                owner-id) " tried to plant a "
                                type " without a seed!\n")))
         (json '("ok"))))

  (GET "/pick/:tilex/:tiley/:plant-id/:player-id/:iefix" [tilex tiley plant-id player-id iefix]
       (let [player-id (parse-number player-id)
             tile-pos (make-vec2 (parse-number tilex) (parse-number tiley))
             plant-id (parse-number plant-id)]
         (if true ;(comment game-world-can-player-pick?
              ;(deref my-game-world) player-id)
           (do
             (append-spit
              log-filename
              (str (Date.) " " (game-world-id->player-name
                                (deref my-game-world) player-id)
                   " has picked a seed\n"))
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
             receiver-id (parse-number receiver-id)]
         (append-spit
          log-filename
          (str (Date.) " " (game-world-id->player-name
                            (deref my-game-world) player-id)
               " is giving a fruit to "
               (game-world-id->player-name
                (deref my-game-world) receiver-id) "\n"))
         (dosync
          (let [fruit (player-get-fruit
                       (game-world-find-player
                        (deref my-game-world)
                        player-id)
                       fruit-id)]
            (if fruit
              (do
                (println receiver-id)
                (ref-set my-game-world
                         (game-world-modify-player
                          (game-world-modify-player
                           (deref my-game-world) receiver-id
                           (fn [player]
                             (modify
                              :seeds
                              (fn [fruits]
                                (max-cons fruit fruits max-player-fruit))
                              player)))
                          player-id
                          (fn [player]
                            (player-remove-fruit player fruit-id))))
                (json '("ok")))
              (json '("fail")))))))

  (GET "/offering/:player-id/:fruit-id/:spirit/:iefix"
       [player-id fruit-id spirit iefix]
       (let [player-id (parse-number player-id)
             fruit-id (parse-number fruit-id)]
         (append-spit
          log-filename
          (str (Date.) " " (game-world-id->player-name
                            (deref my-game-world) player-id)
               " is offering a fruit to " spirit "\n"))
         (dosync
          (let [fruit (player-get-fruit
                       (game-world-find-player
                        (deref my-game-world)
                        player-id)
                       fruit-id)]
            (if fruit
              (do
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

