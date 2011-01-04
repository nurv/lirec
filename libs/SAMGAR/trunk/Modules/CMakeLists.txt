cmake_minimum_required(VERSION 2.6)
message(STATUS "[  ----     Samgar Modules     ----  ]")
project(SamgarModules)

option(EXAMPLES_ENABLE "Enable examples" OFF)
if(EXAMPLES_ENABLE STREQUAL OFF)
  message(STATUS "[ to enable examples run with parameter: -D EXAMPLES_ENABLE=ON   ]")
else(EXAMPLES_ENABLE STREQUAL OFF)
  message(STATUS "[ to disable examples run with parameter: -D EXAMPLES_ENABLE=OFF ]")
endif(EXAMPLES_ENABLE STREQUAL OFF)

option(SAMGAR2PLAYER_ENABLE "Enable Samgar2Player" OFF)
if(SAMGAR2PLAYER_ENABLE STREQUAL OFF)
  message(STATUS "[ to enable Samgar2Player module and client: -D SAMGAR2PLAYER_ENABLE=ON   ]")
else(SAMGAR2PLAYER_ENABLE STREQUAL OFF)
  message(STATUS "[ to disable Samgar2Player module and client: -D SAMGAR2PLAYER_ENABLE=OFF ]")
endif(SAMGAR2PLAYER_ENABLE STREQUAL OFF)

# where the SamgarModule library is, value set when checking this library
set(SAMGARMODULE_DIR ${PROJECT_SOURCE_DIR}/SamgarModule)


add_subdirectory(SamgarModule)

# prepare examples
if(EXAMPLES_ENABLE STREQUAL ON)
  add_subdirectory(Examples)
endif(EXAMPLES_ENABLE STREQUAL ON)

# compile Samgar2Player
if(SAMGAR2PLAYER_ENABLE STREQUAL ON)
  add_subdirectory(Samgar2Player)
endif(SAMGAR2PLAYER_ENABLE STREQUAL ON)
