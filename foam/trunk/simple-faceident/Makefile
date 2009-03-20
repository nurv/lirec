CC = g++
CV_INCLUDE_PATH = /usr/local/include/opencv
CV_LINK_PATH = /usr/local/lib
ACE_LINK_PATH = /usr/lib
LIBS = -lcv -lcxcore -lhighgui -lYARP_dev -lYARP_sig -lYARP_OS -lACE
CCFLAGS = -ggdb -Wall -O3 -ffast-math -Wno-unused -fPIC
CCCOMMANDS = -I$(CV_INCLUDE_PATH) -L$(CV_LINK_PATH) -L$(ACE_LINK_PATH) $(CCFLAGS) 

all: simple-faceident

simple-faceident: main.o FaceBank.o ImageUtils.o
	$(CC) $(CCCOMMANDS) main.o FaceBank.o ImageUtils.o -rdynamic -o simple-faceident $(LIBS)

main.o: src/main.cpp
	$(CC) $(CCCOMMANDS) -c src/main.cpp

FaceBank.o: src/FaceBank.cpp
	$(CC) $(CCCOMMANDS) -c src/FaceBank.cpp

ImageUtils.o: src/ImageUtils.cpp
	$(CC) $(CCCOMMANDS) -c src/ImageUtils.cpp

clean:
	rm -rf *.o simple-faceident


