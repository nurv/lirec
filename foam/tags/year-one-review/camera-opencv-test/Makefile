all: face_detect

face_detect: face_detect.c
	$(CC) $(CFLAGS) -o $@ $^ `pkg-config --cflags --libs libunicap libucil opencv glib-2.0`


clean:
	rm -f *.o
	rm -f *~
	rm -f face_detect
