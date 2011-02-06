java -classpath $( echo lib/*.jar . | sed 's/ /:/g'):src clojure.main src/oak/core.clj &
sleep 40
echo starting agents...
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false CanopySpirit F CanopySpirit CanopySpirit strength:4 hurt:false pose:standing &
sleep 1
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false CoverSpirit F CoverSpirit CoverSpirit strength:8 hurt:false pose:standing &
sleep 1
java -cp lib/FAtiMA.jar FAtiMA.Agent localhost 46874 false VerticalSpirit M VerticalSpirit VerticalSpirit strength:9 hurt:false pose:standing &

