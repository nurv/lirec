To start this example you will need to obtain cmion (https://svn.lirec.eu/libs/cmion).
Copy architectureconfiguration.xml to cmionlauncher folder. Also need to add this project
folder to cmion launcher classpath.

For sending and receiving sms a gsm modem is required. The competency library is currently
configured for the modem in use at Heriot Watt (MultiTech GPRS modem). For changing modem
settings edit the competency library entries for sms related competencies.

You will also need the Greta ECA (embodied conversational agent) for Windows, which you can 
download (in a modified version for this scenario) from here:

http://www.macs.hw.ac.uk/~michael/greta-in-the-wild.zip

unzip and start through bin/greta_realtime.bat

Set the IP-adress or host name of the machine that runs greta in the competency library for
the GretaBMLSender competency.

For FaceDetection you will need to use Samgar and 2 Samgar Modules from the Team Buddy Scenario 
(https://svn.lirec.eu/scenarios/TeamBuddy), namely SamgarSendImage and SamgarFaceDetect.
In the Samgar Gui connect SamgarSendImage.VideoOut to SamgarFaceDetect.VideoIn. After you start Cmion, a 
cmion module should appear in the Samgar gui and it should have a port Facedetect. After that has happened, 
Cmion.FaceDetect to SamgarFaceDetect.Out in the Samgar Gui.