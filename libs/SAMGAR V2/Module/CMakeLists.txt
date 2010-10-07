# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)


# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})

# set up our program
add_executable(SamExamples SamExample.cpp InteruptOverlay.h SamClass.h ExampleOne.h)





# link with YARP libraries
target_link_libraries(SamExamples ${YARP_LIBRARIES} ACE)
