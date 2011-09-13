*****************
*		*
* MyPleo Readme *
*		*
*****************

This workspace contains the code for MyPleo, an artificial pet prototype with two embodiments: a robotic one, consisting of a modified Pleo robot (PhyPleo); and a virtual one, consisting of an Android application for an HTC Desire (ViPleo). MyPleo can switch between embodiments (PhyPleo and ViPleo), only being active in one at a time. PhyPleo's behaviour was defined in Pawn script using the SDK for Pleo 1.1.1. ViPleo, on the other hand, is an Android application written in Java that has a module using the ShiVa3d graphical engine. This module is responsible for the pet interaction in ViPleo and is internally scripted in Lua. The Android application is responsible for communicating with PhyPleo. For a detailed description of the prototype please refer to MyPleo System Description manual (available at http://trac.lirec.org/browser/scenarios/MyFriend/MyPleo/miniBundle).

The workspace is divided in the following folders:
- phypleo - containing the code for the pawn script behaviour of PhyPleo;
- Pleo - containing the eclipse project of the Android application;
- viPleoShivaModule - containing the Shiva3D project of the shiva module;
- viPleoShivaModuleOutput - folder used in the deployment of the Shiva module;

For configuration and installation instructions, please refer to MyPleo Installation and Deployment manual (available at http://trac.lirec.org/browser/scenarios/MyFriend/MyPleo/miniBundle).

All the code has been licensed under GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007 (http://www.gnu.org/copyleft/gpl.html).

This work is partially supported by the European Community (EC) and was funded by the EU FP7 ICT-215554 project LIREC (LIving with Robots and IntEractive Companions), and FCT (INESC-ID multiannual funding) through the PIDDAC Program funds. The authors are solely responsible for the content of this publication. It does not represent the opinion of the EC, and the EC is not responsible for any use that might be made of data appearing therein.

