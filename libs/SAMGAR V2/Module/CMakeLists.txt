# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)

find_package(OpenCV)


# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})

# set up our program
add_executable(SamExamples SamExample.cpp InteruptOverlay.h SamClass.h ExampleOne.h )
add_executable(VideoSend VideoSend.cpp InteruptOverlay.h SamClass.h VideoSend.h)
add_executable(VideoGet  VideoGet.cpp InteruptOverlay.h SamClass.h VideoGet.h)



# link with YARP libraries
target_link_libraries(SamExamples ${YARP_LIBRARIES})# ACE)
target_link_libraries(VideoSend ${YARP_LIBRARIES} ${OPENCV_LIBRARIES})
target_link_libraries(VideoGet ${YARP_LIBRARIES} ${OPENCV_LIBRARIES})