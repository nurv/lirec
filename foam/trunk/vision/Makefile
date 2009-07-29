TARGET  := libvision.a

SRCS    := src/Image.cpp\
	src/Rendering.cpp\
	src/Classifier.cpp\
	src/PCAClassifier.cpp\
	src/LDAClassifier.cpp\
	src/Matrix.cpp\
	src/PCA.cpp\
	src/SVD.cpp\
	src/FileTools.cpp\
	src/tinyxml.cpp\
	src/tinyxmlerror.cpp\
	src/tinyxmlparser.cpp

CCFLAGS = `pkg-config --cflags opencv` -ggdb -Wall -O3 -ffast-math -Wno-unused -DTIXML_USE_STL 

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

