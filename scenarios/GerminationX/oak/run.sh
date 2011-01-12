java -classpath $( echo lib/*.jar . | sed 's/ /:/g'):src clojure.main src/oak/core.clj 
#&
#sleep 30
#java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false Canopy M Canopy Canopy strength:4 hurt:false pose:standing &
#sleep 1
#java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false Cover M Cover Cover strength:8 hurt:false pose:standing &
#sleep 1
#java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false Vertical M Vertical Vertical strength:9 hurt:false pose:standing &
