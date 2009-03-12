CC = g++
CV_INCLUDE_PATH = /usr/local/include/opencv
CV_LINK_PATH = /usr/local/lib
LIBS = -lcv -lcxcore -lhighgui

all: simple-faceident

simple-faceident: main.o FaceBank.o ImageUtils.o
	$(CC) -I$(CV_INCLUDE_PATH) -L$(CV_LINK_PATH) $(LIBS) main.o FaceBank.o ImageUtils.o -o simple-faceident

main.o: src/main.cpp
	$(CC) -I$(CV_INCLUDE_PATH) -c src/main.cpp

FaceBank.o: src/FaceBank.cpp
	$(CC) -I$(CV_INCLUDE_PATH) -c src/FaceBank.cpp

ImageUtils.o: src/ImageUtils.cpp
	$(CC) -I$(CV_INCLUDE_PATH) -c src/ImageUtils.cpp

clean:
	rm -rf *.o simple-faceident


