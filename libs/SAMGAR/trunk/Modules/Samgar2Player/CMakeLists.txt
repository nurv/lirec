# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

# find YARP
find_package(YARP REQUIRED)

# find pkg-config
find_package(PkgConfig REQUIRED)

# find Player
pkg_check_modules(PLAYERCPP REQUIRED playerc++)

# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})
include_directories(${PLAYERCPP_INCLUDE_DIRS})
include_directories(${SAMGARMODULE_DIR})

# set up our program
add_executable(Samgar2Player Samgar2Player.cpp PlayerSamgarModule.cpp PlayerSamgarThread.cpp)

# link with YARP libraries
target_link_libraries(Samgar2Player SamgarModule ${YARP_LIBRARIES} ${PLAYERCPP_LIBRARIES} ACE)
