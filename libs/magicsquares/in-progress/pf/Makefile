TARGET  := pf

SRCS    := src/main.cpp\
	src/App.cpp\
	src/ParticleFilter.cpp

# for the minute, go out and up to link to the vision lib
CCFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused -DTIXML_USE_STL 
LDFLAGS =
LIBS    = `pkg-config --libs opencv` 

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
