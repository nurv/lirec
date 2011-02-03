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
   oak.tile)
  (:import
   java.util.concurrent.Executors
   java.util.Date)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as json])) 

(defn game-world-save [game-world fn]
  (serialise game-world fn))

(defn game-world-load [fn]
  (deserialise fn))

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
(def my-game-world (ref (make-game-world 100 1)))

(append-spit log-filename (str (str (Date.)) " server started\n"))

(defn tick []
  (Thread/sleep 1000)
  ;(println ".")
  ;(game-world-print (deref my-game-world))
  (let [time (/ (.getTime (java.util.Date.)) 1000.0)]
    (dosync (ref-set fatima-world
                     (world-run
                      (game-world-sync->fatima
                       (deref fatima-world)
                       (deref my-game-world)
                       time) time)))
    (dosync (ref-set my-game-world
                     (game-world-update
                      (game-world-sync<-fatima
                       (deref my-game-world)
                       (deref fatima-world))
                      time 1))))
  (recur))

;(tick)

(defroutes main-routes
  (GET "/get-tile/:tilex/:tiley" [tilex tiley]
       (let [tile (game-world-get-tile (deref my-game-world)
                                       (make-vec2 (parse-number tilex)
                                                  (parse-number tiley)))]
         (if tile
           (json/encode-to-str tile)
           (json/encode-to-str '()))))

  (GET "/make-plant/:tilex/:tiley/:posx/:posy/:type/:owner/:size"
       [tilex tiley posx posy type owner size]
       (append-spit
        log-filename
        (str
         (str (Date.)) " " owner " has created a " type " at tile "
         tilex "," tiley " position " posx "," posy "\n"))
       (dosync
        (ref-set my-game-world
                 (game-world-add-entity
                  (deref my-game-world)
                  (make-vec2 (parse-number tilex) (parse-number tiley))
                  (make-plant (make-vec2 (parse-number posx) (parse-number posy)) type owner size))))
       ;(game-world-save (deref my-game-world) state-filename)
       ;(println (deref my-game-world))
       (json/encode-to-str '("ok")))
  
  (GET "/spirit-sprites/:name" [name]
       (update-islands (str "./" name) (str "./" name))
       (read-islands (str "./public/" name)))

  (GET "/spirit-info" []
       (json/encode-to-str (map
                            (fn [a]
                              {:name (remote-agent-name a)
                               :emotions (remote-agent-emotions a)
                               :actions (remote-agent-done a)})
                            (world-agents (deref fatima-world)))
                           :indent 2))
  (GET "/perceive" []
       (world-perceive-all (deref fatima-world))
       (json/encode-to-str '("ok")))
  
  (comment 
  (GET "/add-object/:obj" [obj]
       (println (str "adding " obj))
       (dosync (ref-set myworld (world-add-object (deref myworld)
                                                  (load-object obj))))))
  
  (route/not-found "<h1>Page not found</h1>"))
  
(let [pool (Executors/newFixedThreadPool 2)
      tasks (list (fn []
                    (run-jetty (wrap-file main-routes "public") {:port 8001}))
                  (fn []
                    (tick)))]
    
  (doseq [future (.invokeAll pool tasks)]
    (.get future))
  (.shutdown pool))

