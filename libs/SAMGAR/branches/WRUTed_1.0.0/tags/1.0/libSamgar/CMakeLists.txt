cmake_minimum_required(VERSION 2.6)
FIND_PACKAGE(YARP REQUIRED)
FIND_PACKAGE(OpenCV REQUIRED )

include_directories 	(inc)
include_directories     (${OpenCV_INCLUDE_DIRS})
#link_directories 	    (Release) 

add_library (SamgarLIB 
  src/Samgar.cpp 
  inc/SamgarVars.h 
  inc/Samgar.h
)

ADD_EXECUTABLE(SamgarSend        examples/SamgarSend.cpp)
ADD_EXECUTABLE(SamgarRecive 	 examples/SamgarRecive.cpp)
ADD_EXECUTABLE(SamgarSendImage   examples/SamgarSendImage.cpp )
ADD_EXECUTABLE(SamgarReciveImage examples/SamgarReciveImage.cpp )
ADD_EXECUTABLE(SamgarMigrate     examples/SamgarMigrate.cpp)
ADD_EXECUTABLE(SamgarMove        examples/SamgarMove.cpp)
ADD_EXECUTABLE(SamgarMotion      examples/SamgarMotion.cpp)
ADD_EXECUTABLE(SamgarFaceDetect  examples/SamgarFaceDetect.cpp)

target_link_libraries	(SamgarSend      SamgarLIB ${YARP_LIBRARIES} ACE)
target_link_libraries	(SamgarRecive    SamgarLIB ${YARP_LIBRARIES} ACE)
target_link_libraries	(SamgarMigrate   SamgarLIB ${YARP_LIBRARIES} ACE)
target_link_libraries	(SamgarMove      SamgarLIB ${YARP_LIBRARIES} ACE)
target_link_libraries	(SamgarMotion    SamgarLIB ${YARP_LIBRARIES} ACE)

target_link_libraries	(SamgarSendImage   SamgarLIB ${OpenCV_LIBS} ${YARP_LIBRARIES} highgui ACE) 
target_link_libraries	(SamgarReciveImage SamgarLIB ${OpenCV_LIBS} ${YARP_LIBRARIES} highgui ACE)
target_link_libraries	(SamgarFaceDetect SamgarLIB ${OpenCV_LIBS} ${YARP_LIBRARIES} highgui ACE)
