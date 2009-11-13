
FIND_PACKAGE(YARP)

include_directories 	(HeaderFiles)
include_directories 	(SourceFiles)
include_directories 	(TesterFiles)
include_directories 	(Release)
link_directories 	(Release) 

# Gui stuff
include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/juce/juce/bin)
include_directories 	(C:/Users/Kyron/Documents/Compilerstuff/juce)
link_directories 	(C:/Users/Kyron/Documents/Compilerstuff/juce/bin)         


# this exacuatable needs to be made into a windows runtime instead of a command prompt runtime
# in visual studio this is found under linker/System/subSystem

ADD_EXECUTABLE( SamgarKey 
SourceFiles/Key.cpp 
SourceFiles/MainComponent.cpp
SourceFiles/MainComponent.h
SourceFiles/ModuleGui.h
C:/Users/Kyron/Documents/Compilerstuff/juce/juce_amalgamated.cpp 
C:/Users/Kyron/Documents/Compilerstuff/juce/juce_amalgamated.h
)

#target_link_libraries	(SamgarKey SamgarLIB)


