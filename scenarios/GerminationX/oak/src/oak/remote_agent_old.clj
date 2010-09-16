(ns test-server.remote-agent
  (:import
   java.net.Socket
   java.io.File
   java.io.IOException
   FAtiMA.util.parsers.SocketListener
   FAtiMA.autobiographicalMemory.AutobiographicalMemory))

(defstruct remote-agent
  :properties
  :name
  :role
  :display-name
  :socket
  :socket-listener)

(def remote-agent-properties (accessor remote-agent :properties))
(def remote-agent-name (accessor remote-agent :name))
(def remote-agent-role (accessor remote-agent :role))
(def remote-agent-display-name (accessor remote-agent :display-name))
(def remote-agent-socket (accessor remote-agent :socket))
(def remote-agent-socket-listener (accessor remote-agent :socket-listener))

(defn remote-agent-add-property [agent property]
  (merge agent {:properties (cons property (remote-agent-properties agent))})) 

(defn remote-agent-process [msg]
  (println "process"))

(defn remote-agent-send [agent msg]
  (try 
    (let [aux (str msg "\n")
          out (. (remote-agent-socket agent) getOutputStream)]
      (. out write (. aux getBytes "UTF-8"))
      (. out flush)
      true)
    (catch IOException e
      (. e printStackTrace)
      false)))

(defn make-remote-agent [socket]
  (let [socket-listener
        (proxy [SocketListener] [socket]
          (processMessage [str]
                          (remote-agent-process  str)))]
    (. socket-listener initialize)
    (Thread/sleep 100)   
    (println (. (. socket getInputStream) available))
    ;(. (AutobiographicalMemory/GetInstance) setSelf name)
    
    (let [ra (struct remote-agent
                     []
                     "cheese"
                     "cheese"
                     "cheese"
                     socket
                     socket-listener)]
      
      (remote-agent-send ra "OK")
      (println "started agent socket")
      ra)))