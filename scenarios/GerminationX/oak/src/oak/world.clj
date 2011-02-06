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

(ns oak.world
  (:use
   oak.forms
   oak.remote-agent
   oak.io
   oak.vec2)
  (:import
   java.util.ArrayList
   java.net.InetSocketAddress
   java.nio.channels.ServerSocketChannel
   java.nio.channels.SocketChannel
   java.io.File
   javax.xml.parsers.SAXParser
   javax.xml.parsers.SAXParserFactory
   FAtiMA.autobiographicalMemory.AutobiographicalMemory
   FAtiMA.deliberativeLayer.plan.Effect   
   FAtiMA.deliberativeLayer.plan.Step
   FAtiMA.sensorEffector.SpeechAct
   FAtiMA.wellFormedNames.Name
   FAtiMA.wellFormedNames.Substitution
   FAtiMA.wellFormedNames.Symbol
   FAtiMA.wellFormedNames.Unifier
   FAtiMA.util.parsers.StripsOperatorsLoaderHandler
   Language.LanguageEngine))
  
(defstruct world
  :objects
  :agents 
  :scenery
  :actions
  :agent-language
  :server-socket
  :time)

(def world-objects (accessor world :objects))
(def world-agents (accessor world :agents))
(def world-scenery (accessor world :scenery))
(def world-actions (accessor world :actions))
(def world-agent-language (accessor world :agent-language))
(def world-ssc (accessor world :server-socket))
(def world-time (accessor world :time))

(defn world-add-agent [world agent]
  (merge world {:agents (cons agent (world-agents world))})) 

(defn load-operators [xml self]
		(let [op (new StripsOperatorsLoaderHandler self)
              parser (.newSAXParser (SAXParserFactory/newInstance))]
          (.parse parser (new File xml) op)
          op))

(defn make-world [port agent-language-file actions-file objects]
  (struct world
          (load-objects objects)
          []
          "garden"
          (.getOperators (load-operators actions-file, "[SELF]"))
          (new LanguageEngine "name" "M" "Victim" (new File agent-language-file))
          (let [ssc (ServerSocketChannel/open)]
            (.configureBlocking ssc false)
            (.bind (.socket ssc) (new InetSocketAddress port))
            ssc)
          0))

; return in the format needed by FAtiMA: token:value token:value ...
(defn hash-map-to-string [m]
  (apply
   str
   (map
    (fn [v]
      (str (first v) ":" (second v) " "))
    m)))

; look through agents and objects and return the properties for the named thing
(defn world-get-properties [world name]
  (reduce
   (fn [r agent]
     (if (and (not r) (= (remote-agent-name agent) name))
       (remote-agent-properties agent)
       r))
   (reduce
    (fn [r object]
      (if (and (not r) (= (get object "name") name))
        object
        r))
    false
    (world-objects world))
   (world-agents world)))

; send a message to all agents 
(defn world-broadcast-all [world msg]
  (doseq [agent (world-agents world)]
    (send-msg (remote-agent-socket agent) msg)))

; send a message to all agents except caller
(defn world-broadcast [world caller msg]
  (doseq [agent (world-agents world)]
    (when (not (= (remote-agent-name agent)
                  (remote-agent-name caller)))
      (send-msg (remote-agent-socket agent) msg))))

; send a list of all agents and objects to this agent
(defn world-perceive [world agent]
  (send-msg (remote-agent-socket agent)
               (apply str 
                      (concat
                       (list "AGENTS")
                       (map
                        (fn [agent]
                          (str " " (remote-agent-name agent)))
                        (world-agents world))
                       (map
                        (fn [object]
                          (str " " (get object "name")))
                        (world-objects world))))))

(defn world-get-object [world name]
  (reduce
   (fn [r obj]
     (if (and (not r) (= name (get obj "name")))
       obj r))
   false
   (world-objects world)))

(defn world-add-object [world object]
  ; check we haven't added it already
  (if (not (world-get-object world (get object "name")))
    (do
      (world-broadcast-all world (str "ENTITY-ADDED " (get object "name")))
      (comment println (str "added " (get object "name") " " (get object "position") " "
                    (count (world-objects world)) " objects stored"))
      (merge world {:objects (cons object (world-objects world))}))
    world))

(defn list->commas [l]
  (if (not (empty l))
    (apply str
           (concat
            (first l)
            (map (fn [t] (str "," t)) (rest l))))
    ""))

(defn convert-to-action-name [action]
  (let [action (.split action " ")]
    (Name/ParseName (apply str
                           (concat
                            (first action) "("
                            (list->commas (rest action))
                            (list ")"))))))

(defn properties-changed [world agent effects]
  (doseq [e effects]
    (let [name (.toString (.getName (.GetEffect e)))]
      (when (and (not (.startsWith name "EVENT"))
                 (not (.startsWith name "SpeechContext"))
                 (> (.GetProbability e)
                    (.nextFloat (remote-agent-random agent))))
        (world-broadcast world agent (str "PROPERTY-CHANGED " name
                                    " " (.getValue (.GetEffect e))))))))

(defn update-action-effects [world agent action]
  (doseq [s (world-actions world)]
    (let [bindings (new ArrayList)]
      (.add bindings (new Substitution (new Symbol "[SELF]")
                          (new Symbol (remote-agent-name agent))))
      (.add bindings (new Substitution (new Symbol "[AGENT]")
                          (new Symbol (remote-agent-name agent))))
      (when (Unifier/Unify (.getName s) action bindings)
        (let [gstep (.clone s)]
          (.MakeGround s bindings)
          (properties-changed world agent (.getEffects gstep)))))))
    
(defn world-process-agent [world agent msg]
  ;(println (str "world-process-agent for " (remote-agent-name agent) " got " msg))
  (let [toks (.split msg " ")
        type (nth toks 0)]
    (cond
     (.startsWith type "<EmotionalState") (merge agent {:emotions (parse-xml msg)})
     (.startsWith type "<Relations") (merge agent {:relations (parse-xml msg)})
     (.startsWith type "PROPERTY-CHANGED") agent
     (= type "look-at")
     (do
       (let [object-name (nth toks 1)]
         ;(println (remote-agent-tile agent))
         ; is the agent on the same tile as the object?
         (if (vec2-eq? (remote-agent-tile agent)
                       (get (world-get-object world object-name) "tile"))
           (do
             (send-msg (remote-agent-socket agent)
                       (str "LOOK-AT " object-name " "
                            (hash-map-to-string
                             (world-get-properties world (nth toks 1)))))
             (merge agent {:done (max-cons {:time (world-time world)
                                            :msg msg}
                                           (remote-agent-done agent) 4)}))
           agent)))
     (= type "say")
     (do (println "say")
         (let [say (SpeechAct/ParseFromXml (.substring msg 3))]
           (if say
             (let [s (str
                      (.getActionType say) "("
                      (.getReceiver say) ","
                      (.getMeaning say)
                      (list->commas (.GetParameters say))
                      ")")]
               (update-action-effects world agent (Name/ParseName s))
               (world-broadcast-all world (str "ACTION-FINISHED " (remote-agent-name agent)
                                               " " msg))
               (merge agent {:said (cons (str (world-time world)
                                              ": "
                                              (.getMeaning say) " to " (.getReceiver say))
                                         (remote-agent-said agent))}))
             agent)))
    
     (= type "UserSpeech") (do (println "user speech") agent)
     :else
     (do
       ;(println "action")
       ;(println msg)
       (update-action-effects
        world agent
        (convert-to-action-name
         (apply str
                (concat type
                        (if (not (empty (rest toks)))
                          (list
                           (second toks)
                           (map (fn [s] (str s " ")) (rest (rest toks))))
                          '())))))
       (world-broadcast-all
        world
        (apply str (concat "ACTION-FINISHED " (remote-agent-name agent) " "
                           (map (fn [s] (str s " ")) toks))))
       (merge agent {:done (max-cons {:time (world-time world)
                                      :msg msg}
                                     (remote-agent-done agent) 4)})))))

(defn world-check-for-new-agents [world]
  (let [chan (.accept (world-ssc world))]
    (if chan
      (try
        (let [agent (make-remote-agent chan world)
              w (world-add-agent world agent)
              name (remote-agent-name agent)]
          (println name "enters the" (world-scenery w))
          (world-broadcast w agent (str "ENTITY-ADDED " name))
          (world-perceive w agent)
          w)
        (catch Exception e (. e printStackTrace) world))
    world)))

(defn world-perceive-all [world]
  (doseq [a (world-agents world)]
    (world-perceive world a)))

(defn world-update-agent [world agent]
  (let [msgs (read-msg (remote-agent-socket agent))]
    (if msgs
      (reduce
       (fn [agent msg]
         (world-process-agent world agent msg))
       agent
       (.split msgs "\n")))))

(defn world-update-agents [world]
  (comment (println "updating: "
          (map
           (fn [agent] (remote-agent-name agent))
           (world-agents world))))
  (merge world
         {:agents
          (doall (map
                  (fn [agent]
                    (world-update-agent world agent))
                  (world-agents world)))}))

(def object-max-age 100)

(defn world-remove-old-objects [world time]
  (merge world
         {:objects
          (doall
           (filter
            (fn [obj]
              (< (- time (get obj "time")) object-max-age))
            (world-objects world)))}))

(defn world-run [world time]
  (world-update-agents
   (world-remove-old-objects
    (world-check-for-new-agents
     (merge world {:time (+ (world-time world) 1)}))
    time)))

