CC = g++
CFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused 
CXXFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused 
LDFLAGS =  `pkg-config --libs opencv` 

all: DistanceCheck

DistanceCheck: distanceCheck.o Face.o stdafx.o
	$(CC) $(LDFLAGS) stdafx.o distanceCheck.o Face.o -o DistanceCheck

distanceCheck.o: src/distanceCheck.cpp
	$(CC) $(CXXFLAGS) -c src/distanceCheck.cpp

Face.o: src/Face.cpp
	$(CC) $(CXXFLAGS) -c src/Face.cpp

stdafx.o: src/stdafx.cpp
	$(CC) $(CXXFLAGS) -c src/stdafx.cpp

clean:
	rm -rf *.o DistanceCheck


