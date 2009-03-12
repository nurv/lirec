default: HelloWorld

HelloWorld: src/HelloWorld.o 
	g++ -o HelloWorld src/HelloWorld.cpp

.PHONY: clean cleanall

clean:
	rm -f src/*.o

cleanall: clean
	rm -f HelloWorld
