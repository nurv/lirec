To start, copy architectureconfiguration.xml to cmionlauncher folder. 
Also need to add this project folder to cmion launcher classpath.

For sending and receiving sms a gsm modem is required. The competency library is currently
configured for the modem in use at Heriot Watt. For changing modem settings edit the 
competency library entries for sms related competencies.

You will also need the Greta ECA (embodied conversational agent) for Windows, which you can 
download (in a modified version for this scenario) from here:

http://www.macs.hw.ac.uk/~michael/greta-in-the-wild.zip

unzip and start through bin/greta_realtime.bat

Set the IP-adress or host name of the machine that runs greta in the competency library for
the GretaBMLSender competency.

