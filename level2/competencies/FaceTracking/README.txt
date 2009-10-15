************************************
FaceTracking
************************************

This code allows for the detection and tracking of a face in the scene.
Face detection is performed using Haar classifiers and is based on the
OpenCV face detection code.
Tracking is performed using a Camshift wrapper (see License.txt,
in the Camshift wrapper folder, for more details) based on the Camshift
algorithm provided by OpenCV, which tracks a combination of colours.

The main program waits until a face is detected in the scene (using
the function waitForFaceDetect()); when a face is detected, the tracking
is automatically initialised using the face bounding box returned by 
the Haar classifier.

You may need to modify the paths for the Haar classifiers (in the
function InitFaceDetection() of the class FaceDetection) to reflect
your directory structure.

The functions setVmin() and setSmin() allow for the setting of the
Camshift parameters. The suitable values to use may change according
to your application and setup. 

The program allows for a re-initialisation of the tracking.
The user can re-initialise the tracking process by pressing the 'r' key.
When the 'r' key is pressed, the program waits until a face is detected 
and re-initialise the tracker using the new face bounding box.

The code has been written and tested in Windows, 
but it should work under Linux as well.

You will need to install OpenCV on your machine.

Questions to: ginevra@dcs.qmul.ac.uk