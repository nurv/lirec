TARGET  := libmagicsquares.a

SRCS    := src/Image.cpp\
	src/Rendering.cpp\
	src/Classifier.cpp\
	src/PCAClassifier.cpp\
	src/LDAClassifier.cpp\
	src/Matrix.cpp\
	src/PCA.cpp\
	src/SVD.cpp\
	src/FileTools.cpp\
	src/FaceFinder.cpp\
	src/Geometry.cpp\
	src/ParticleFilter.cpp\
	src/RadarParticleFilter.cpp\
	src/LEDParticleFilter.cpp\
	src/tinyxml.cpp\
	src/tinyxmlerror.cpp\
	src/tinyxmlparser.cpp

CCFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -march=core2 -mfpmath=sse -O3 -ffast-math -Wno-unused -DTIXML_USE_STL 
LIBS    = `pkg-config --libs opencv` 
PYTHON_INCLUDE = -I/usr/include/python2.5

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
	ar rc ${TARGET} ${OBJS}

${OBJS}: %.o: %.cpp %.dep 
	${CC} ${CCFLAGS} -o $@ -c $< 

${DEPS}: %.dep: %.cpp Makefile 
	${CC} ${CCFLAGS} -MM $< > $@ 

clean:: 
	-rm -f *~ src/*.o ${TARGET} 

cleandeps:: clean
	-rm -f src/*.dep

distclean:: clean

python:: ${TARGET}
	swig -c++ -python src/magicsquares.i 
	g++ ${CCFLAGS} -c src/magicsquares_wrap.cxx ${PYTHON_INCLUDE}
	g++ -shared ${OBJS} magicsquares_wrap.o ${LIBS} -o _magicsquares.so
	mv _magicsquares.so python
	mv src/magicsquares.py python
	
