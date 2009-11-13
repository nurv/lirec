
FIND_PACKAGE( OpenCV REQUIRED )



ADD_EXECUTABLE(simple-faceident facedetect.cpp  )


TARGET_LINK_LIBRARIES(simple-faceident ${OpenCV_LIBS})
