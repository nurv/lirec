# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)

# find pkg-config
find_package(PkgConfig REQUIRED)

# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})

# create samgar module library
add_library(SamgarModule SamClass.cpp)
