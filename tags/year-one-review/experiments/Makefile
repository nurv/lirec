CC = g++
CXXFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused 
LDFLAGS =  `pkg-config --libs opencv` 

all: simple-faceident

simple-faceident: main.o ImageUtils.o
	$(CC) main.o ImageUtils.o -rdynamic -o exp $(LDFLAGS)

main.o: src/main.cpp
	$(CC) $(CXXFLAGS) -c src/main.cpp


ImageUtils.o: src/ImageUtils.cpp
	$(CC) $(CXXFLAGS) -c src/ImageUtils.cpp

clean:
	rm -rf *.o exp


