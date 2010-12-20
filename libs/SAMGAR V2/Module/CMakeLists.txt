# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)

# find OpenCV
find_package(OpenCV REQUIRED)

# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})
include_directories(${OpenCV_INCLUDE_DIRS})


# create samgar module library
add_library(SamModule SamClass.cpp)

# set up our program
add_executable(SamExamples SamExample.cpp ExampleOne.cpp)
add_executable(VideoSend VideoSend.cpp)
add_executable(VideoGet  VideoGet.cpp)

# link with YARP libraries
target_link_libraries(SamExamples SamModule ${YARP_LIBRARIES})
target_link_libraries(VideoSend SamModule ${YARP_LIBRARIES} ${OpenCV_LIBS})
target_link_libraries(VideoGet SamModule ${YARP_LIBRARIES} ${OpenCV_LIBS})
