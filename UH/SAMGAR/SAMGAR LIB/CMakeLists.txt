
FIND_PACKAGE(YARP)

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)
include_directories 	(TesterFiles)
include_directories 	(Release)
link_directories 	(Release) 




add_library (SamgarLIB SourceFiles/SAMGARMAIN.cpp HeaderFiles/UserVars.h HeaderFiles/SamgarMainClass.h 
HeaderFiles/ConnectFunct.h HeaderFiles/cxtypes)  ## this should create a libary

ADD_EXECUTABLE(Tester TesterFiles/Test.cpp)
ADD_EXECUTABLE(Tester2 TesterFiles/Test2.cpp)

target_link_libraries	(Tester SamgarLIB ${OpenCV_LIBS})
target_link_libraries	(Tester2 SamgarLIB ${OpenCV_LIBS})
target_link_libraries	(SamgarKey SamgarLIB)


