# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)

# find pkg-config
find_package(PkgConfig REQUIRED)

# find OpenCV
pkg_check_modules(OpenCV opencv)

# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})
include_directories(${SAMGARMODULE_DIR})
if (OpenCV_FOUND)
	include_directories(${OpenCV_INCLUDE_DIRS})
endif (OpenCV_FOUND)


# set up our program
add_executable(SamExamples SamExample.cpp ExampleOne.cpp)
if (OpenCV_FOUND)
	add_executable(VideoSend VideoSend.cpp)
	add_executable(VideoGet  VideoGet.cpp)
endif (OpenCV_FOUND)

# link with YARP libraries
target_link_libraries(SamExamples SamgarModule ${YARP_LIBRARIES} ACE)
if (OpenCV_FOUND)
	target_link_libraries(VideoSend SamgarModule ${YARP_LIBRARIES} ${OpenCV_LIBRARIES} ACE)
	target_link_libraries(VideoGet SamgarModule ${YARP_LIBRARIES} ${OpenCV_LIBRARIES} ACE)
endif (OpenCV_FOUND)
