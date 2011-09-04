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
   ring.adapter.jetty
   ring.middleware.file
   oak.world
   oak.remote-agent
   oak.io
   oak.island
   oak.game-world
   oak.vec2
   oak.plant
   oak.tile
   oak.forms
   oak.player)
  (:import
   java.util.concurrent.Executors
   java.util.Date)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as json])) 

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

;(def my-game-world (ref (game-world-load state-filename)))
(def my-game-world (ref (make-game-world 30 1)))
(game-world-save (deref my-game-world) "test.txt")

(append-spit log-filename (str (str (Date.)) " server started\n"))

(defn tick []
  (Thread/sleep 1000)
  ;(println ".")
  ;(game-world-print (deref my-game-world))
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
                       time 1)))))
  (recur))

;(tick)

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
            (dosync
             (ref-set my-game-world
                      (game-world-add-player
                       (deref my-game-world) name fbid)))
            (json/encode-to-str
             (game-world-find-player
              (deref my-game-world)
              (game-world-find-player-id
               (deref my-game-world) name))))
          :else
          (do
            (append-spit
             log-filename
             (str (Date.) " " name " is logging in\n"))
            (json/encode-to-str
             (game-world-find-player
              (deref my-game-world) id))))))

  (GET "/player/:player-id/:iefix" [player-id iefix]
       (json/encode-to-str
        (game-world-find-player
         (deref my-game-world)
         (parse-number player-id))))
  
  (GET "/get-tile/:tilex/:tiley/:iefix" [tilex tiley iefix]
       (let [tiles (game-world-get-tile-with-neighbours
                     (deref my-game-world)
                     (make-vec2 (parse-number tilex)
                                (parse-number tiley)))]
         
         (json/encode-to-str tiles)))

  (GET "/get-msgs/:id/:iefix" [id iefix]
       (let [id (parse-number id)]
         (if (< id 1)
           (json/encode-to-str (:msgs (:log (deref my-game-world))))
           (do
             (let [player (game-world-find-player
                           (deref my-game-world) id)]
               (if player
                 (json/encode-to-str (:msgs (:log player)))
                 (json/encode-to-str {:error (str "no player " id " found")})))))))
           
  (GET "/make-plant/:tilex/:tiley/:posx/:posy/:type/:owner-id/:size/:iefix"
       [tilex tiley posx posy type owner-id size iefix]
       (append-spit
        log-filename
        (str
         (str (Date.)) " " (game-world-id->player-name
                            (deref my-game-world)
                            (parse-number owner-id)) " has created a " type " at tile "
         tilex "," tiley " position " posx "," posy "\n"))
       (dosync
        (ref-set my-game-world
                 (game-world-modify-player
                  (game-world-add-entity
                   (deref my-game-world)
                   (make-vec2 (parse-number tilex) (parse-number tiley))
                   (make-plant
                    ((:id-gen (deref my-game-world)))
                    (make-vec2 (parse-number posx)
                               (parse-number posy))
                    type (parse-number owner-id) size))
                  (parse-number owner-id)
                  (fn [player]
                    (player-inc-plant-count player)))))
       (game-world-save (deref my-game-world) state-filename)
       (json/encode-to-str '("ok")))

  (GET "/pick/:tilex/:tiley/:plant-id/:player-id/:iefix" [tilex tiley plant-id player-id iefix]
       (append-spit
        log-filename
        (str (Date.) " " (game-world-id->player-name
                          (deref my-game-world)
                          (parse-number player-id))
             " has picked a seed\n"))
       (let [player-id (parse-number player-id)]
         (if (game-world-can-player-pick?
              (deref my-game-world) player-id)
         (do
           (dosync
            (ref-set my-game-world
                     (game-world-modify-tile
                      (game-world-player-pick
                       (deref my-game-world) player-id)
                      (make-vec2 (parse-number tilex)
                                 (parse-number tiley))
                      (fn [tile]
                        (tile-modify-entity
                         tile
                         (parse-number plant-id)
                         (fn [plant]
                           (plant-picked
                            plant
                            (game-world-find-player
                             (deref my-game-world)
                             player-id))))))))
           (json/encode-to-str {:ok true})) 
         (json/encode-to-str {:ok false}))))
       
  (GET "/hiscores/:iefix" [iefix]
       (json/encode-to-str
        (map
         (fn [s]
           (list (first s) (second s)))
         (game-world-hiscores (deref my-game-world)))))
  
  (GET "/spirit-sprites/:name/:iefix" [name iefix]
       ;(update-islands (str "./" name) (str "./" name))
       (read-islands (str "./public/" name)))

  (GET "/spirit-info/:iefix" [iefix]
       (json/encode-to-str (:spirits (deref my-game-world))))

  (GET "/perceive/:iefix" [iefix]
       (world-perceive-all (deref fatima-world))
       (json/encode-to-str '("ok")))
  
  (comment 
  (GET "/add-object/:obj/:iefix" [obj iefix]
       (println (str "adding " obj))
       (dosync (ref-set myworld (world-add-object (deref myworld)
                                                  (load-object obj))))))
  
  (route/not-found "<h1>Page not found</h1>"))
  
(let [pool (Executors/newFixedThreadPool 2)
      tasks (list
             (fn []
               (run-jetty (wrap-file main-routes "public") {:port 8001}))
             (fn []
               (tick))
             )]
  (doseq [future (.invokeAll pool tasks)]
    (.get future))
  (.shutdown pool))

