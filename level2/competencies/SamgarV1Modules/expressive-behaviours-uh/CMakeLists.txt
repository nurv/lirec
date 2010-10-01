FIND_PACKAGE(YARP)

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)

link_directories		(Release)

ADD_EXECUTABLE(L1PioneerDisplay	L1PioneerDisplay.cpp)
ADD_EXECUTABLE(L2PioneerDisplay	L2PioneerDisplay.cpp)
ADD_EXECUTABLE(L2PioneerSound	L2PioneerSound.cpp)
ADD_EXECUTABLE(L2PioneerPhysical	L2PioneerPhysical.cpp)
ADD_EXECUTABLE(L2ExpressiveBehaviour	L2ExpressiveBehaviour.cpp)


target_link_libraries	(L1PioneerDisplay	SamgarLIB)
target_link_libraries	(L2PioneerDisplay	SamgarLIB)
target_link_libraries	(L2PioneerSound		SamgarLIB)
target_link_libraries	(L2PioneerPhysical	SamgarLIB)
target_link_libraries	(L2ExpressiveBehaviour	SamgarLIB)

