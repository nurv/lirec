************************
MotionDirectionDetection
************************

This module allows for the overall motion direction of a person walking in an orthogonal direction to the camera to be
computed. Specifically, this program can detect whether (1) a person is moving to the camera's left, (2) a person is moving
to the camera's right, or (3) there is no motion in the camera's field of view.

The code is based on the motion templates routines provided by OpenCV.
Using motion templates requires the silhouette of the user. Given the silhouette of the user, a motion template is built using
a motion history image (mhi) and an indication of the overall motion can be derived by computing the gradient of the mhi.

As the human silhouette is the starting point to compute the overall motion direction using motion templates, note that
the performance of the system depends on how well the human silhouette is extracted from the background.

The MotionDirectionDetection module is comprised of the following classes:

**CaptureFrame** 
This class contains functions to capture a video from camera or avi file.

**BackgroundSubtr**
This class contains functions to extract the user's silhouette from the background.
The silhouette is extracted from the background by subtracting the first valid frame of the video to the current frame.

**MotionDirection** 
This class contains functions to compute the overall motion direction of the user.
The execute() function is the central function of the module. It calls the functions to capture the video, extract the silhouette
from the background, compute the overall motion direction, classify the type of movement and add this information to a list.
The update_mhi() function builds the motion templates and computes the overall motion direction by taking the gradient of the
motion history image.
The getMovementType() function returns a message code depending on the overall motion direction of the user: NO_MOTION (0), 
ENTERING_THE_ROOM (1) or LEAVING_THE_ROOM (2).

**MessageStorage**
This class contains functions to store information about the type of movement in a list.
The function addMessage() adds the latest message code and timestamp to a list and stores the last N message codes and timestamps
in the list.
The function getMessage() allows for the latest message code and related timestamp to be accessed.


The main program calls the function execute() of the class MotionDirectionDetection, which allows the user to specify whether 
to capture from camera or file.

Note that the initial frames of the videos should contain only the background and not the user, as the silhouette is obtained
by subtracting the first valid frame to the current frame.

The values of some of the parameters used in this module may change according to your application and setup. 
Examples include the threshold values in the cvThreshold() function used in the function to extract the silhouette from the 
background (extractBackground()); the value assigned to the "threshold" variable in the update_mhi() function (which is used
to disregard small motion); the number (currently set to 1000) that the norm of the image including the silhouette is
divided by before being assigned to the variable "count" in the function update_mhi(); the values of the variables MHI_DURATION,
MIN_TIME_DELTA, MAX_TIME_DELTA.

The code has been written and tested in Windows, but it should work under Linux as well.

You will need to install OpenCV on your machine.

Questions to: ginevra@dcs.qmul.ac.uk  




