# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)
FIND_PACKAGE( MRPT REQUIRED core base gui hwdrivers maps)
FIND_PACKAGE( OpenCV REQUIRED )


# add include directories
include_directories("C:/Program Files/MobileRobots/Aria/include")
include_directories("C:/Program Files/MobileRobots/Aria/lib")
include_directories(${YARP_INCLUDE_DIRS})
include_directories("C:/Program Files/OpenCV/include/opencv")

link_directories("C:/Program Files/OpenCV/lib")

link_directories("C:/Program Files/MobileRobots/Aria/lib") 


# set up our program
add_executable(SamExamples SamExample.cpp InteruptOverlay.h SamClass.h ExampleOne.h)


add_executable(Robot ROBOT.cpp InteruptOverlay.h SamClass.h MapUpdater.h VirtualRobot.h PlaySounds.h )

add_executable(Vlaser Vlaser.cpp InteruptOverlay.h SamClass.h )

add_executable(VStar VStarGazer.cpp InteruptOverlay.h SamClass.h)

add_executable(PhidgetMonitor	PhidgetSensor.cpp phidget21.h)

add_executable(GotNav GoToNavigation.cpp InteruptOverlay.h SamClass.h)

ADD_EXECUTABLE(move directMotion.cpp)

ADD_EXECUTABLE(EMYSSender EMYSSender.cpp)

add_executable(faceDetect FaceDetect.cpp InteruptOverlay.h SamClass.h)


# link with YARP libraries
target_link_libraries(SamExamples ${YARP_LIBRARIES})
target_link_libraries(VStar ${YARP_LIBRARIES})
target_link_libraries(Robot ${YARP_LIBRARIES} ${MRPT_LIBS})
target_link_libraries(Vlaser ${YARP_LIBRARIES} ${MRPT_LIBS})
target_link_libraries(PhidgetMonitor phidget21 ${YARP_LIBRARIES})
target_link_libraries(GotNav Aria ${YARP_LIBRARIES})
target_link_libraries(move Aria)
target_link_libraries(EMYSSender ${YARP_LIBRARIES})
target_link_libraries(faceDetect ${YARP_LIBRARIES} ${OpenCV_LIBS} highgui200 cv200 cxcore200 cvaux200)

