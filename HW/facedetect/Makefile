FILE=FaceDetect
LIB= -lcv -lcxcore -lcvaux -lhighgui -lstdc++
INC=-I /usr/local/lib -I /usr/local/include/opencv


link: $(FILE).o
	g++ -o $(FILE) $(FILE).o $(LIB)

$(FILE).o: $(FILE).cpp
	g++ -c $(FILE).cpp $(INC)

clean:
	-rm FaceDetect.o FaceDetect
