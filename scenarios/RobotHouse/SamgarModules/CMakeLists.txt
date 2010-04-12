
project( LirecMay2010Demo )
FIND_PACKAGE(YARP)
FIND_PACKAGE( OpenCV REQUIRED )
FIND_PACKAGE( MRPT REQUIRED core )

 
include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/Aria/include)    # getting the header files for the complier
include_directories 	(C:/Users/Kyron/Documents/CompilerstuffAria/Aria/lib)
include_directories 	(C:/Users/Kyron/Documents/gearbox-9.11)
include_directories 	(C:/Users/Kyron/Desktop/LericMay2010Demo/libs)
include_directories 	(C:/Users/Kyron/Desktop/LericMay2010Demo/SourceFiles)

include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/Aria/lib)
include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/Aria/include)


include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/OpenCV/lib)

link_directories 	(C:/Users/Kyron/Desktop/LericMay2010Demo/SourceFiles)       
link_directories 	(C:/Users/Kyron/Desktop/LericMay2010Demo/libs)    
link_directories 	(C:/Users/Kyron/Documents/Compilerstuff/Aria/lib)  

link_directories	(C:/Users/Kyron/Documents/Compilerstuff/OpenCV/lib)



#ADD_EXECUTABLE(VRobot 	 SourceFiles/VirtualRobot.cpp)
#ADD_EXECUTABLE(VRobot2 	 SourceFiles/VirtualRobot.cpp)
ADD_EXECUTABLE(VRobot3 	 SourceFiles/VirtualRobot.cpp)


ADD_EXECUTABLE(VLaser 	 SourceFiles/VirtualLaser.cpp)
ADD_EXECUTABLE(VStar  	 SourceFiles/VStarGazer.cpp )
ADD_EXECUTABLE(Waypoint  SourceFiles/WaypointGoto.cpp)
ADD_EXECUTABLE(MapBuild  SourceFiles/MapBuilder.cpp)
ADD_EXECUTABLE(GiveCord  SourceFiles/GiveCords.cpp)
ADD_EXECUTABLE(Stepper   SourceFiles/Stepper.cpp)

ADD_EXECUTABLE(Intel   SourceFiles/Intel.cpp)
ADD_EXECUTABLE(FakeGeo   SourceFiles/FakeGeo.cpp)

ADD_EXECUTABLE(ICP   SourceFiles/icp-slam_main.cpp)

ADD_EXECUTABLE(grid   SourceFiles/GridmapExperiment.cpp)




#TARGET_LINK_LIBRARIES  (WindowsCamVS ${OpenCV_LIBS})
#target_link_libraries	(VRobot Aria)
#target_link_libraries	(VRobot ArNetworking)
#target_link_libraries	(VRobot SamgarLIB)

target_link_libraries	(VRobot3 SamgarLIB)
target_link_libraries	(VRobot3 Aria)
target_link_libraries	(VRobot3 ArNetworking)

target_link_libraries	(VRobot3 _AriaPy)
target_link_libraries	(VRobot3 AriaJava)

target_link_libraries	(VLaser hokuyo_aist flexiport SamgarLIB)
#target_link_libraries	( SamgarLIB)


target_link_libraries	(VStar      SamgarLIB )
target_link_libraries	(GiveCord   SamgarLIB )
target_link_libraries	(Waypoint    SamgarLIB )
target_link_libraries	(Intel    SamgarLIB )
target_link_libraries	(FakeGeo    SamgarLIB )

TARGET_LINK_LIBRARIES(MapBuild ${MRPT_LIBS} SamgarLIB)
TARGET_LINK_LIBRARIES(ICP ${MRPT_LIBS})
TARGET_LINK_LIBRARIES(grid ${MRPT_LIBS})



