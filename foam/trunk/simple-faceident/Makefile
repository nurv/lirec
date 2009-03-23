CC = g++
CFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused 
CXXFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused 
LDFLAGS =  `pkg-config --libs opencv` -lYARP_dev -lYARP_sig -lYARP_OS -lACE

all: simple-faceident

simple-faceident: main.o FaceBank.o ImageUtils.o
	$(CC) main.o FaceBank.o ImageUtils.o -rdynamic -o simple-faceident $(LDFLAGS)

main.o: src/main.cpp
	$(CC) $(CXXFLAGS) -c src/main.cpp

FaceBank.o: src/FaceBank.cpp
	$(CC) $(CXXFLAGS) -c src/FaceBank.cpp

ImageUtils.o: src/ImageUtils.cpp
	$(CC) $(CXXFLAGS) -c src/ImageUtils.cpp

clean:
	rm -rf *.o simple-faceident


