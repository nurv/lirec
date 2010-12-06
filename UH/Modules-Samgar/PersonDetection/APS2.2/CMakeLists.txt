
# YARP needs CMake 2.6 or greater
cmake_minimum_required(VERSION 2.6)

project( APS2 )

# find YARP
find_package(YARP REQUIRED)

# find OpenCV
find_package(OpenCV REQUIRED)

# add YARP include directories
include_directories(${YARP_INCLUDE_DIRS})
include_directories(${OpenCV_INCLUDE_DIRS})

include_directories (C:/Programs/gearbox-9.11/src)
#include_directories (C:/Programs/SAMGAR/SAMGARLIB/HeaderFiles)
#include_directories (C:/Programs/ROBOTIS/USB2Dynamixel/import)
include_directories (SourceFiles)
#include_directories ("$ENV{SM_API_PATH}/include")
include_directories (C:/Programs/MobileRobots/Aria/include)

link_directories (C:/Programs/gearbox-9.11/src/flexiport/Release)
link_directories (C:/Programs/gearbox-9.11/src/hokuyo_aist/Release)
#link_directories (C:/Programs/SAMGAR/SAMGARLIB)
#link_directories (C:/Programs/ROBOTIS/USB2Dynamixel/import)
#link_directories ("$ENV{SM_API_PATH}/bin")
link_directories (C:/Programs/MobileRobots/Aria/lib)
link_directories (C:/Programs/ACE_wrappers/lib)

# create samgar module library
#add_library ( SamgarLIB SourceFiles/SamClass.cpp SourceFiles/SamClass.h )
#add_library ( SamModules SourceFiles/SamModules.cpp SourceFiles/SamModules.h )
add_library ( SamModules SourceFiles/SamModules.cpp SourceFiles/SamModules.h SourceFiles/SamClass.cpp SourceFiles/SamClass.h )

# set up our program
#add_executable ( SamExamples SourceFiles/SamExample.cpp SourceFiles/SamModules.cpp SourceFiles/SamModules.h )
#add_executable ( VideoSend SourceFiles/VideoSend.cpp SourceFiles/VideoSend.h )
#add_executable ( VideoGet  SourceFiles/VideoGet.cpp SourceFiles/VideoGet )

# link with YARP libraries
#target_link_libraries ( SamExamples SamgarLIB ${YARP_LIBRARIES} ACE )
#target_link_libraries ( VideoSend SamgarLIB ${YARP_LIBRARIES} ${OPENCV_LIBRARIES} ACE )
#target_link_libraries ( VideoGet SamgarLIB ${YARP_LIBRARIES} ${OPENCV_LIBRARIES} ACE )




add_executable ( vCamera   SourceFiles/vCamera.cpp )
target_link_libraries ( vCamera  SamModules  highgui210.lib )

add_executable ( vLaser  SourceFiles/vLaser.cpp)
target_link_libraries ( vLaser hokuyo_aist SamModules flexiport )

add_executable ( ld-main SourceFiles/ld-main.cpp SourceFiles/LegsDetector.cpp SourceFiles/LegsDetector.h SourceFiles/Utility.h )
target_link_libraries ( ld-main SamModules cv210.lib cxcore210 highgui210)

add_executable ( fd-main SourceFiles/fd-main.cpp )
target_link_libraries ( fd-main SamModules cv210.lib cxcore210 highgui210)

#add_executable ( ft-main SourceFiles/ft-main.cpp SourceFiles/utils.h SourceFiles/stdafx.h )
#target_link_libraries ( ft-main SamModules smft31.lib cv210.lib cxcore210.lib)

add_executable ( pd-main SourceFiles/pd-main.cpp)
target_link_libraries ( pd-main SamModules )

#add_executable ( pe-main SourceFiles/pe-main.cpp)
#target_link_libraries ( pe-main SamModules  )

#add_executable ( vPTU SourceFiles/vPTU.cpp SourceFiles/dynamixel_joint_drive.cpp SourceFiles/dynamixel_joint_drive.h )
#target_link_libraries ( vPTU  dynamixel.lib SamModules )

add_executable ( vRobot SourceFiles/vRobot.cpp SourceFiles/ActionApproach.h )
target_link_libraries ( vRobot aria.lib SamModules )

