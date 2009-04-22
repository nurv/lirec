# $Id: Makefile 10 2009-03-24 21:45:48Z mw $
#############################################################################
# Makefile for building: FacET library and demo
# Marek Wnuk, 10.03.2009
#############################################################################

#    FacET is a library for detecting and parameterising face components.
#    Copyright (C) 2009  Marek Wnuk <marek.wnuk@pwr.wroc.pl>
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.


####### Paths for compilation

SRC=./src
INC=./inc
OBJ=./obj
LIB=./lib
DEMO=./demo

####### Compiler, tools and options

CC            = gcc
CXX           = g++
CFLAGS = `pkg-config --cflags opencv` -I${INC} -pipe -g -Wall -W -D_REENTRANT
CXXFLAGS = `pkg-config --cflags opencv` -I${INC} -pipe -fpermissive -g -Wall -W -D_REENTRANT
LDFLAGS =  `pkg-config --libs opencv` -lm -lavcodec -lavformat -lavutil -Wl,--no-undefined
LINK          = g++
AR            = ar cqs
DEL_FILE      = rm -f

####### Project data

LIBNAME       = FacET

LIBFILE       = lib${LIBNAME}.a

LIBSOURCES    = ${SRC}/FastMatchTemplate.cpp \
		${SRC}/components.cpp \
		${SRC}/facet.cpp \
		${SRC}/face_regions.cpp \
		${SRC}/image_processing.cpp 

LIBOBJECTS    = ${OBJ}/FastMatchTemplate.o \
		${OBJ}/components.o \
		${OBJ}/facet.o \
		${OBJ}/face_regions.o \
		${OBJ}/image_processing.o 

OBJECTS       = ${LIBOBJECTS} ${DEMOOBJ}/*.o
 
DEMO          = demo

DEMOOBJ       = ${DEMO}/obj
DEMOSRC       = ${DEMO}/src
DEMOINC       = ${DEMO}/inc

LIBRARY       = ${LIB}/${LIBFILE}

TARGET        = ${DEMO}/demo


all : ${TARGET}

library : ${LIBRARY}

####### Build rules

${LIBRARY} : ${LIBOBJECTS}
	ar rvcs ${LIBRARY} ${LIBOBJECTS}


${TARGET} :  ${DEMOOBJ}/main.o ${DEMOOBJ}/timer.o ${LIBRARY} ${DEMOOBJ}/cvcap_dc1394.o ${DEMOOBJ}/capture_mw.o
	${LINK} ${LDFLAGS} -o ${TARGET} ${DEMOOBJ}/main.o ${DEMOOBJ}/timer.o ${DEMOOBJ}/cvcap_dc1394.o ${DEMOOBJ}/capture_mw.o ${LIBS} -L${LIB} -l${LIBNAME} 


clean: 
	-${DEL_FILE} ${OBJECTS}
	-${DEL_FILE} *~ core *.core

help:
	@echo 
	@echo " help     - shows this help"
	@echo " clean    - removes all products of compilation"
	@echo " library  - builds FacET library (lib/libFacET.a)"
	@echo " docs     - builds doxygen documentation"
	@echo " all      - builds demo program for FacET library"
	@echo

docs:
	doxygen doc/Doxyfile
	(cd doc/latex; make)


####### Compile library files

${OBJ}/FastMatchTemplate.o: ${SRC}/FastMatchTemplate.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
                ${INC}/FastMatchTemplate.h \
                ${INC}/facet.h 
	${CXX} -c ${CXXFLAGS} -o ${OBJ}/FastMatchTemplate.o ${SRC}/FastMatchTemplate.cpp

${OBJ}/components.o: ${SRC}/components.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
                ${INC}/facet.h 
	${CXX} -c ${CXXFLAGS} -o ${OBJ}/components.o ${SRC}/components.cpp

${OBJ}/facet.o: ${SRC}/facet.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
		/opt/opencv/include/opencv/cvaux.h \
		/opt/opencv/include/opencv/cvaux.hpp \
		/opt/opencv/include/opencv/cvvidsurv.hpp \
		/opt/opencv/include/opencv/highgui.h \
		${INC}/FastMatchTemplate.h \
                ${INC}/facet.h 
	${CXX} -c ${CXXFLAGS} -o ${OBJ}/facet.o ${SRC}/facet.cpp

${OBJ}/face_regions.o: ${SRC}/face_regions.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
		/opt/opencv/include/opencv/highgui.h \
		${INC}/facet.h \
		${INC}/FastMatchTemplate.h
	${CXX} -c ${CXXFLAGS} -o ${OBJ}/face_regions.o ${SRC}/face_regions.cpp

${OBJ}/image_processing.o: ${SRC}/image_processing.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
		/opt/opencv/include/opencv/cvaux.h \
		/opt/opencv/include/opencv/cvaux.hpp \
		/opt/opencv/include/opencv/cvvidsurv.hpp \
		/opt/opencv/include/opencv/highgui.h \
		${INC}/facet.h \
		${INC}/FastMatchTemplate.h 
	${CXX} -c ${CXXFLAGS} -o ${OBJ}/image_processing.o ${SRC}/image_processing.cpp

####### Compile demo files

${DEMOOBJ}/main.o: ${DEMOSRC}/main.cpp \
		/opt/opencv/include/opencv/cv.h \
		/opt/opencv/include/opencv/cxcore.h \
		/opt/opencv/include/opencv/cxtypes.h \
		/opt/opencv/include/opencv/cxerror.h \
		/opt/opencv/include/opencv/cvver.h \
		/opt/opencv/include/opencv/cxcore.hpp \
		/opt/opencv/include/opencv/cvtypes.h \
		/opt/opencv/include/opencv/cv.hpp \
		/opt/opencv/include/opencv/cvcompat.h \
		/opt/opencv/include/opencv/highgui.h \
		/opt/opencv/include/opencv/cvaux.h \
		/opt/opencv/include/opencv/cvaux.hpp \
		/opt/opencv/include/opencv/cvvidsurv.hpp \
		${INC}/facet.h \
		${INC}/FastMatchTemplate.h \
		${DEMOINC}/timer.h \
		${DEMOINC}/captparam.h \
		${DEMOINC}/options.h 
	${CXX} -c ${CXXFLAGS} -I${DEMOINC} -o ${DEMOOBJ}/main.o ${DEMOSRC}/main.cpp

${DEMOOBJ}/timer.o: ${DEMOSRC}/timer.cpp ${DEMOINC}/timer.h
	${CXX} -c ${CXXFLAGS} -I${DEMOINC} -o ${DEMOOBJ}/timer.o ${DEMOSRC}/timer.cpp

${DEMOOBJ}/cvcap_dc1394.o: ${DEMOSRC}/cvcap_dc1394.cpp ${DEMOINC}/cvconfig.h ${DEMOINC}/_highgui.h
	${CXX} -c ${CXXFLAGS} -I${DEMOINC} -o ${DEMOOBJ}/cvcap_dc1394.o ${DEMOSRC}/cvcap_dc1394.cpp

${DEMOOBJ}/capture_mw.o: ${DEMOSRC}/capture_mw.cpp ${DEMOINC}/capture_mw.h
	${CXX} -c ${CXXFLAGS} -I${DEMOINC} -o ${DEMOOBJ}/capture_mw.o ${DEMOSRC}/capture_mw.cpp

#######

