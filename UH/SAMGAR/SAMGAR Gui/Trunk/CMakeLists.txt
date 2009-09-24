
FIND_PACKAGE(YARP)

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)
include_directories 	(TesterFiles)
include_directories 	(Release)
link_directories 	(Release) 

# Gui stuff
include_directories 	(C:/Users/Kyron/Desktop/juce/bin)
include_directories 	(C:/Users/Kyron/Desktop/juce)
link_directories 	(C:/Users/Kyron/Desktop/juce/bin)         


# this exacuatable needs to be made into a windows runtime instead of a command prompt runtime

ADD_EXECUTABLE( SamgarKey 
SourceFiles/Key.cpp 
SourceFiles/MainComponent.cpp
SourceFiles/MainComponent.h
SourceFiles/ModuleGui.h
C:/Users/Kyron/Desktop/juce/juce_amalgamated.cpp 
C:/Users/Kyron/Desktop/juce/juce_amalgamated.h
)

target_link_libraries	(SamgarKey SamgarLIB)


