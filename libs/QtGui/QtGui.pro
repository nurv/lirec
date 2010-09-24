# -------------------------------------------------
# Project created by QtCreator 2010-07-02T08:35:22
# -------------------------------------------------
TARGET = QtGui
TEMPLATE = app

# yarp
INCLUDEPATH += ../libSamgar/inc \
    inc
LIBS += -lACE \
    -lYARP_OS
OBJECTS_DIR = obj
SOURCES += src/SgPortContainer.cpp \
    src/SgPort.cpp \
    src/SgObject.cpp \
    src/SgModuleContainer.cpp \
    src/SgModule.cpp \
    src/SgMigration.cpp \
    src/SgConnectionContainer.cpp \
    src/SgConnection.cpp \
    src/samgargui.cpp \
    src/main.cpp
HEADERS += inc/SgPortContainer.h \
    inc/SgPort.h \
    inc/SgObject.h \
    inc/SgModuleContainer.h \
    inc/SgModule.h \
    inc/SgMigration.h \
    inc/SgConnectionContainer.h \
    inc/SgConnection.h \
    inc/samgargui.h
FORMS += samgargui.ui
