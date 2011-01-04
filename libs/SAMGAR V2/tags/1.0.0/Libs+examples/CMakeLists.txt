
FIND_PACKAGE(YARP)
FIND_PACKAGE( OpenCV REQUIRED )

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)
include_directories 	(TesterFiles)
include_directories 	(Release)
include_directories     (/usr/local/include/opencv)
link_directories 	    (Release) 

add_library (SamgarLIB SourceFiles/SAMGARMAIN.cpp HeaderFiles/UserVars.h HeaderFiles/SamgarMainClass.h 
HeaderFiles/ConnectFunct.h HeaderFiles/cxtypes)  ## this should create a libary

ADD_EXECUTABLE(SamgarSend        TesterFiles/SamgarSend.cpp)
ADD_EXECUTABLE(SamgarRecive 	 TesterFiles/SamgarRecive.cpp)
ADD_EXECUTABLE(SamgarSendImage   TesterFiles/SamgarSendImage.cpp )
ADD_EXECUTABLE(SamgarReciveImage   TesterFiles/SamgarReciveImage.cpp )
ADD_EXECUTABLE(SamgarMigrate     TesterFiles/SamgarMigrate.cpp)

target_link_libraries	(SamgarSend      SamgarLIB )
target_link_libraries	(SamgarRecive    SamgarLIB )
target_link_libraries	(SamgarMigrate   SamgarLIB )

# if theres a problem with linker check high gui for cv is enabled
#"C:\Program Files (x86)\OpenCV\lib\highgui.lib"

target_link_libraries	(SamgarSendImage   SamgarLIB ${OpenCV_LIBS} highgui)
target_link_libraries	(SamgarReciveImage SamgarLIB ${OpenCV_LIBS} highgui)
