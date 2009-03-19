CC = g++
CV_INCLUDE_PATH = /usr/local/include/opencv
CV_LINK_PATH = /usr/local/lib
LIBS = -lcv -lcxcore -lhighgui
CCFLAGS = -I$(CV_INCLUDE_PATH) -L$(CV_LINK_PATH) -ggdb -Wall -O3 -ffast-math -Wno-unused

all: simple-faceident

simple-faceident: main.o FaceBank.o ImageUtils.o
	$(CC) $(CCFLAGS) $(LIBS) main.o FaceBank.o ImageUtils.o -o simple-faceident

main.o: src/main.cpp
	$(CC) $(CCFLAGS) -c src/main.cpp

FaceBank.o: src/FaceBank.cpp
	$(CC) $(CCFLAGS) -c src/FaceBank.cpp

ImageUtils.o: src/ImageUtils.cpp
	$(CC) $(CCFLAGS) -c src/ImageUtils.cpp

clean:
	rm -rf *.o simple-faceident


