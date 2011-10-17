java -classpath $( echo lib/*.jar . | sed 's/ /:/g'):src clojure.main src/oak/core.clj &
sleep 40
echo starting agents...
java -cp lib/FAtiMA.jar:lib/xmlenc-0.52.jar AgentLauncher GXScenario.xml GardenScenario TreeSpirit &
sleep 1
java -cp lib/FAtiMA.jar:lib/xmlenc-0.52.jar AgentLauncher GXScenario.xml GardenScenario CoverSpirit &
sleep 1
java -cp lib/FAtiMA.jar:lib/xmlenc-0.52.jar AgentLauncher GXScenario.xml GardenScenario ShrubSpirit &
sleep 1
