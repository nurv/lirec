
project(KyronFinalRobot )
FIND_PACKAGE(YARP)

FIND_PACKAGE( OpenCV REQUIRED )
FIND_PACKAGE( MRPT REQUIRED core )

 
include_directories 	(C:/Users/Admin/Documents/Compilerstuff/Aria/include)    # getting the header files for the complier
include_directories 	(C:/Users/Admin/Documents/CompilerstuffAria/Aria/lib)
include_directories 	(C:/Users/Admin/Documents/gearbox-9.11)
include_directories 	(C:/Users/Admin/Desktop/RobotFinalAllWork/libs)
include_directories 	(C:/Users/Admin/Desktop/RobotFinalAllWork/SourceFiles)

include_directories 	(C:/Users/Admin/Documents/Compilerstuff/Aria/lib)
include_directories 	(C:/Users/Admin/Documents/Compilerstuff/Aria/include)

include_directories 	(C:/Users/Admin/Documents/Compilerstuff/OpenCV/lib)

include_directories 	(C:/Users/Admin/Documents/Compilerstuff/mrpt-0.9.0/bin)


link_directories 	(C:/Users/Admin/Desktop/RobotFinalAllWork/SourceFiles)       
link_directories 	(C:/Users/Admin/Desktop/RobotFinalAllWork/libs)    
link_directories 	(C:/Users/Admin/Documents/Compilerstuff/Aria/lib)  

link_directories	(C:/Users/Admin/Documents/Compilerstuff/OpenCV/lib)

link_directories 	(C:/Users/Admin/Documents/Compilerstuff/mrpt-0.9.0/bin)

ADD_EXECUTABLE(VRobot 	 SourceFiles/VirtualRobot.cpp)
ADD_EXECUTABLE(Mapper 	 SourceFiles/MapControl.cpp SourceFiles/MapHelper.cpp SourceFiles/PathPlanAndGraphics.cpp)

TARGET_LINK_LIBRARIES   (Mapper ${MRPT_LIBS} SamgarLIB)


target_link_libraries	(VRobot SamgarLIB)
target_link_libraries	(VRobot AriaVC9)
target_link_libraries	(VRobot ArNetworking)




