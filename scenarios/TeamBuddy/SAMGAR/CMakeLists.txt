PROJECT(Example)


SET(YARP_DIR "$ENV{YARP_ROOT}")
FIND_PACKAGE(YARP)
FIND_PACKAGE( OpenCV REQUIRED )

include_directories(HeaderFiles)
include_directories(SourceFiles)
include_directories(TesterFiles)
include_directories(C:/Program Files/MobileRobots/Aria/include)
include_directories(C:/Program Files/MobileRobots/Aria/lib)

link_directories(C:/Program Files/MobileRobots/Aria/lib) 

add_library(SamgarLIB SourceFiles/SAMGARMAIN.cpp HeaderFiles/UserVars.h HeaderFiles/SamgarMainClass.h 
HeaderFiles/ConnectFunct.h HeaderFiles/cxtypes)  ## this should create a libary

ADD_EXECUTABLE(SamgarSend        TesterFiles/SamgarSend.cpp)
ADD_EXECUTABLE(SamgarRecive 	 TesterFiles/SamgarRecive.cpp)
ADD_EXECUTABLE(SamgarSendImage   TesterFiles/SamgarSendImage.cpp )
#ADD_EXECUTABLE(SamgarReciveImage   TesterFiles/SamgarReciveImage.cpp )
ADD_EXECUTABLE(SamgarMigrate     TesterFiles/SamgarMigrate.cpp)
ADD_EXECUTABLE(SamgarMove     TesterFiles/SamgarMove.cpp)
ADD_EXECUTABLE(SamgarFaceDetect     TesterFiles/SamgarFaceDetect.cpp)



target_link_libraries(SamgarSend      SamgarLIB )
target_link_libraries(SamgarRecive    SamgarLIB )
#target_link_libraries(SamgarFaceDetect    SamgarLIB )
target_link_libraries(SamgarMigrate   SamgarLIB )
target_link_libraries(SamgarMove Aria)

target_link_libraries(SamgarMove SamgarLIB)


# if theres a problem with linker check high gui for cv is enabled
#"C:\Program Files (x86)\OpenCV\lib\highgui.lib"

target_link_libraries(SamgarSendImage   SamgarLIB ${OpenCV_LIBS} highgui)
target_link_libraries(SamgarFaceDetect SamgarLIB ${OpenCV_LIBS} cv)
target_link_libraries(SamgarFaceDetect SamgarLIB ${OpenCV_LIBS} highgui)
target_link_libraries(SamgarFaceDetect SamgarLIB ${OpenCV_LIBS} cxcore)
target_link_libraries(SamgarFaceDetect SamgarLIB ${OpenCV_LIBS} cvaux)