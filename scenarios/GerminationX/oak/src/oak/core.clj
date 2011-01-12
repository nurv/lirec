(ns oak.core
  (:use
   compojure.core
   ring.adapter.jetty
   ring.middleware.file
   oak.world
   oak.remote-agent
   oak.io
   oak.island)
  (:import java.util.concurrent.Executors)
  (:require [compojure.route :as route]
            [org.danlarkin.json :as json])) 

(def myworld
     (ref
      (make-world
       46874
       "data/characters/minds/language/agent/en/language-set-1"
       "data/characters/minds/Actions.xml"
       (list "WiltedVine"
             "AppleTree"))))

;(world-crank (deref myworld))

(defroutes main-routes
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

