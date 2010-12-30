TARGET  := motion

SRCS    := src/main.cpp\
	src/App.cpp

# for the minute, go out and up to link to the vision lib
CCFLAGS = -I../../src `pkg-config --cflags opencv` -ggdb -Wall -march=core2 -mfpmath=sse -O3 -ffast-math -Wno-unused -DTIXML_USE_STL 
LDFLAGS = -L../../
LIBS    = `pkg-config --libs opencv` -lYARP_dev -lYARP_sig -lYARP_OS -lACE -lmagicsquares 

CC = g++
OBJS    := ${SRCS:.cpp=.o} 
DEPS    := ${SRCS:.cpp=.dep} 
XDEPS   := $(wildcard ${DEPS}) 
.PHONY: all clean distclean 
all:: ${TARGET} 

ifneq (${XDEPS},) 
include ${XDEPS} 
endif 

${TARGET}: ${OBJS} 
	${CC} ${LDFLAGS} -o $@ $^ ${LIBS}

${OBJS}: %.o: %.cpp %.dep 
	${CC} ${CCFLAGS} -o $@ -c $< 

${DEPS}: %.dep: %.cpp Makefile 
	${CC} ${CCFLAGS} -MM $< > $@ 

clean:: 
	-rm -f *~ src/*.o ${TARGET} 

cleandeps:: clean 
	-rm -f src/*.dep

distclean:: clean
