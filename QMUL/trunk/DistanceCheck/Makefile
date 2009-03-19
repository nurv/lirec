CC = g++
CV_INCLUDE_PATH = /usr/local/include/opencv
CV_LINK_PATH = /usr/local/lib
LIBS = -lcv -lcxcore -lhighgui
CCFLAGS = -I$(CV_INCLUDE_PATH) -L$(CV_LINK_PATH) -ggdb -Wall -O3 -ffast-math -Wno-unused

all: DistanceCheck

DistanceCheck: distanceCheck.o Face.o stdafx.o
	$(CC) $(CCFLAGS) $(LIBS) stdafx.o distanceCheck.o Face.o -o DistanceCheck

distanceCheck.o: src/distanceCheck.cpp
	$(CC) $(CCFLAGS) -c src/distanceCheck.cpp

Face.o: src/Face.cpp
	$(CC) $(CCFLAGS) -c src/Face.cpp

stdafx.o: src/stdafx.cpp
	$(CC) $(CCFLAGS) -c src/stdafx.cpp

clean:
	rm -rf *.o DistanceCheck


