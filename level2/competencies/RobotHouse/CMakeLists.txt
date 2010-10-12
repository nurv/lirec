# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)
FIND_PACKAGE( MRPT REQUIRED core base gui hwdrivers maps)


# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})

# set up our program
add_executable(SamExamples SamExample.cpp InteruptOverlay.h SamClass.h ExampleOne.h)


add_executable(Robot ROBOT.cpp InteruptOverlay.h SamClass.h MapUpdater.h ODO.h VirtualRobot.h Vlaser.h PlaySounds.h VirtualCamera.h)


# link with YARP libraries
target_link_libraries(SamExamples ${YARP_LIBRARIES})
target_link_libraries(Robot ${YARP_LIBRARIES} ${MRPT_LIBS})