
FIND_PACKAGE(YARP)

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)
include_directories 	(TesterFiles)
include_directories 	(Release)
link_directories 	    (Release) 
link_directories 	    (../Libs+examples/ LIB/Trunk/)

# Gui stuff
#include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/juce/juce/bin)
#include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/juce)
#link_directories 	    (C:/Users/Kyron/Documents/Compilerstuff/juce/bin)         

include_directories 	($ENV{JUCE_DIR}/juce/bin)
include_directories 	($ENV{JUCE_DIR})
link_directories 	    ($ENV{JUCE_DIR}/bin)                 
include_directories 	(/usr/include/freetype2)

# this exacuatable needs to be made into a windows runtime instead of a command prompt runtime
# in visual studio this is found under linker/System/subSystem

ADD_EXECUTABLE( SamgarKey 
SourceFiles/Key.cpp 
SourceFiles/MainComponent.cpp
SourceFiles/MainComponent.h
SourceFiles/ModuleGui.h
#C:/Users/Kyron/Documents/Compilerstuff/juce/juce_amalgamated.cpp 
#C:/Users/Kyron/Documents/Compilerstuff/juce/juce_amalgamated.h
$ENV{JUCE_DIR}/juce_amalgamated.cpp 
$ENV{JUCE_DIR}/juce_amalgamated.h
)

#target_link_libraries	(SamgarKey SamgarLIB)

target_link_libraries	(SamgarKey X11 asound freetype GL Xinerama)
