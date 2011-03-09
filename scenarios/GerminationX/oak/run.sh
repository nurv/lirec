java -classpath $( echo lib/*.jar . | sed 's/ /:/g'):src clojure.main src/oak/core.clj &
sleep 40
echo starting agents...
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false TreeSpirit F TreeSpirit TreeSpirit strength:4 hurt:false pose:standing &
sleep 1
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false CoverSpirit F CoverSpirit CoverSpirit strength:8 hurt:false pose:standing &
sleep 1
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false ShrubSpirit M ShrubSpirit ShrubSpirit strength:9 hurt:false pose:standing &

