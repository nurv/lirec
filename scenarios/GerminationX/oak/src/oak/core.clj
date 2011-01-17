(ns oak.core
  (:use
   compojure.core
   ring.adapter.jetty
   ring.middleware.file
   oak.world
   oak.remote-agent
   oak.io
   oak.island
   oak.game-world
   oak.vec2
   oak.plant
   oak.tile)
  (:import
   java.util.concurrent.Executors)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as json])) 

(defn game-world-save [game-world fn]
  (serialise game-world fn))

(defn game-world-load [fn]
  (deserialise fn))

(def state-filename "state.txt")

(def myworld
     (ref
      (make-world
       46874
       "data/characters/minds/language/agent/en/language-set-1"
       "data/characters/minds/Actions.xml"
       (list "WiltedVine"
             "AppleTree"))))

(def my-game-world (ref (game-world-load state-filename)))
(println (deref my-game-world))
;(def my-game-world (ref (make-game-world)))

;(world-crank (deref myworld))

(defroutes main-routes
  (GET "/get-tile/:tilex/:tiley" [tilex tiley]
       (println (list tilex tiley))
       (let [tile (game-world-get-tile (deref my-game-world)
                                       (make-vec2 tilex tiley))]
         (if tile
           (json/encode-to-str tile)
           (json/encode-to-str '()))))
  (GET "/make-plant/:tilex/:tiley/:posx/:posy/:type/:owner"
       [tilex tiley posx posy type owner]
       (println (list tilex tiley posx posy type owner))
       (dosync
        (ref-set my-game-world
                 (game-world-add-entity
                  (deref my-game-world)
                  (make-vec2 tilex tiley)
                  (make-plant (make-vec2 posx posy) type owner))))
       (game-world-save (deref my-game-world) state-filename)
       (println (deref my-game-world)))
  (GET "/spirit-sprites" []
       (println (read-islands "./public/islands"))
       (read-islands "./public/islands"))     
  (GET "/agent-info" []
       (json/encode-to-str (map
                            (fn [a]
                              {:name (remote-agent-name a)
                               :emotions (remote-agent-emotions a)})
                            (world-agents (deref myworld)))
                           :indent 2))
  (GET "/perceive" []
       (world-perceive-all (deref myworld)))
 
  (GET "/add-object/:obj" [obj]
       (println (str "adding " obj))
       (dosync (ref-set myworld (world-add-object (deref myworld)
                                                  (load-object obj)))))
  
  (route/not-found "<h1>Page not found</h1>"))

(defn tick []
  (Thread/sleep 1000)
  (dosync
   (ref-set myworld
            (world-run (deref myworld))))
  (recur))
  
(let [pool (Executors/newFixedThreadPool 2)
      tasks (list (fn []
                    (run-jetty (wrap-file main-routes "public") {:port 8001}))
                  (fn []
                    (tick) (println "DONE TICK")))]
    
  (doseq [future (.invokeAll pool tasks)]
    (.get future))
  (.shutdown pool))

