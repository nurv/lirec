#pragma once

#include "SamClass.h"
#include <time.h>
#include <ctype.h>
#include "windows.h"
#include <stdio.h>
#include <iostream>
#include <string>
#include <stdlib.h>
#include <time.h>
#using <System.dll>

using namespace System;
using namespace System::IO::Ports;
//using namespace System::Threading;
using namespace yarp;
//using namespace System::Timers;

//
// Prototypes
//
void build_frame(array<unsigned char> ^ frame, unsigned char, int, int);
void CheckSum(array<unsigned char> ^ frame);
void emotion_pos(int emotion);
double  mouthOpenForPhoneme(std::string p);
double calculate_distance (double x1,double y1,double x2 ,double y2);
double SetTargetPos(string target);
int emotion=0;
bool bTalking;
int iabsX=500;
int iabsY=500;
bool bTrack=true;


void wait ( double seconds )
{
  clock_t endwait;
  endwait = clock () + seconds * CLOCKS_PER_SEC ;
  while (clock() < endwait) {}
}

double Rad2Deg(double x)
{
	return x*57.2957795;
};
double Deg2Rad(double x)
{
	return x/57.2957795;
};

static int expressions[7][14] =
{
	//{motors id0 to id11, vel, eye blink rate}
    { 500, 700, 730, 500, 0, 0, 500, 500, 500, 500, 500, 500, 50, 3 },//neutral,0
    { 800, 600, 630, 0, 0, 0, 800, 500, 500, 500, 500, 500, 50, 2 },//anger,1
	{ 0, 600, 630, 800, 0, 0, 800, 0, 400, 500, 500, 500, 50, 3 },//fear,2
	{ 400, 700, 730, 550, 0, 0, 500, 750, 500, 500, 500, 500, 80, 2 },//joy,3
	{ 500, 800, 830, 500, 900, 900, 0, 800, 450, 500, 500, 500, 250, 3 },//surprize,4
	{ 0, 620, 650, 800, 0, 0, 500, 500, 800, 500, 500, 500, 20, 3 },//sad,5
	{ 0, 300, 330, 800, 0, 0, 500, 500, 800, 500, 520, 520, 10, 0 }//sleep,6
};

//
//

class EmoThread: public Thread
{
	public:
		Bottle *emotion;

	virtual bool threadInit()
    {
		return 1;
	
	}

	 virtual void run() 
	 {
       // while (!isStopping()) 
		//{
		//	if(emys.emotion!=NULL)
		//{
		//	emotion = emys.emotion->get(0).asInt();
		//	for(int i=0;i<12;i++)
		//	{
		//		// frame, id, pos,vel)
		//		build_frame(frame, Convert::ToByte(i), Convert::ToInt16(expressions[emotion][i]), Convert::ToInt16(expressions[emotion][12]));
		//		serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte
		//	}

		//}
		//}
	 }

	virtual void threadRelease()
    {

	}

};

class EMYSSender : public SamClass 
{
	private:
	BufferedPort<Bottle> bEmo, bTrack, bTTS; // create buffered ports for bottles like this
	Network yarp;						   // make sure the network is ready
	

	public:
			
		unsigned char id;
		int position;
		int velocity;
		
		int iTalkPos;
		Bottle *talk;
		Bottle *emotion;
		Bottle *track;
	
		//EmoThread ThEmo;
		
		

	void SamInit(void)
	{
	
		
		RecognisePort("TTSIn");				// name the port to be shown in the gui
			
		RecognisePort("EmoIn");				// name the port to be shown in the gui
	
		RecognisePort("TrackIn");				// name the port to be shown in the gui

	

		StartModule("/EMYS");	
		bTTS.open("/EMYS_TTSIn");		// open the port
		bEmo.open("/EMYS_EmoIn");		// open the port
		bTrack.open("/EMYS_TrackIn");		// open the port
	

		//bTrack.setStrict(true);
		//bEmo.setStrict(true);
		//bTTS.setStrict(true);
		
	
	
		bTrack.setReporter(myPortStatus);	// set reporter, this is important
		bEmo.setReporter(myPortStatus);	// set reporter, this is important
		bTTS.setReporter(myPortStatus);	// set reporter, this is important
	
	
		bTalking=false;
    		
		puts("started EMYS rcv");
	}

	void SamIter(void)
	{
		//while (bTTS.getPendingReads() > 0)
			talk = bTTS.read(false);

		
		//if(talk!=NULL)
		//{	
		//	puts("got a msg in rcv");
		//	puts(talk->toString());
		//}

		//while (bEmo.getPendingReads() > 0)
			emotion = bEmo.read(false);
	
	//	if(emotion!=NULL)
	//	{	
	//		puts("got a emotion");
	//		puts(emotion->toString());
			
	//	}


		//while (bTrack.getPendingReads() > 0)
			track = bTrack.read(false);


		
	
		/*if(track!=NULL)
		{	
			puts("got a msg");
			puts(track->toString());
			
		}*/
	}
	
};



int main(array<System::String ^> ^args)
{
   
	EMYSSender emys;
	emys.SamInit();

	SerialPort ^serialport = gcnew SerialPort();
	serialport->PortName = "COM11";
	serialport->BaudRate = 57600;
	serialport->DataBits = 8;
	serialport->Parity = Parity::None;
	serialport->StopBits = StopBits::One; 
	serialport->WriteBufferSize=100;

  
	int loopcnt=0;
	bool bopen=false;
	bool bclose=false;

	//set the head to initial frame
	serialport->Open(); 
	try
	{
		array<unsigned char>^ iframe = gcnew array<unsigned char>(11); 

		for(int i=0;i<11;i++)
		{
			// frame, id, pos,vel)
			build_frame(iframe, Convert::ToByte(i), Convert::ToInt16(expressions[emotion][i]), Convert::ToInt16(expressions[emotion][12]));
			serialport->Write(iframe, 0, 11); //Send frame from 0 to 11 byte
		}
		wait(1);
	}
	catch (System::Exception^ ex)
	{
		Console::WriteLine(ex->Message+L"\n"); // if any problems with serial port
		return 0;
	}
	serialport->Close();     // Close serial port
		

	while(1)
	{
		emys.SamIter();
		//emys.talk = bTTS.read(false);
		loopcnt++;
		serialport->Open(); 
		array<unsigned char>^ frame = gcnew array<unsigned char>(11); 
	
		//int  *motorval;//=new int[12];
		
		//yarp::os::Time::delay(1);

		try
		{
		
		if(emys.emotion!=NULL)
		{
			emotion = atoi(emys.emotion->get(0).asString().c_str());
			//got target look at
			std::cout << "got data " << emys.emotion->toString() << std::endl;
			if(emotion==100)
			{
				emotion=0;
				string Target = emys.emotion->get(1).asString();
				std::cout << "got string " << Target << std::endl;

				bTrack=false;
				double ang = SetTargetPos(Target) *3.33;
					
				int iabsX = 500 + ang;
				std::cout << "target angle " << ang << " iabsX "<< iabsX << std::endl;
				

				if(Target=="screen")
				{
					build_frame(frame, Convert::ToByte("9"), Convert::ToInt16(iabsX), 200);
					serialport->Write(frame, 0, 11); 

					build_frame(frame, Convert::ToByte("6"), Convert::ToInt16(500), 150);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11);	

					bTrack=false;
				}
				else if(Target=="user")
				{
					bTrack=true;
					build_frame(frame, Convert::ToByte("6"), Convert::ToInt16(500), 150);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11);	

				}
				else
				{
					bTrack=false;
					
					build_frame(frame, Convert::ToByte("9"), Convert::ToInt16(iabsX), 200);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11); 

					build_frame(frame, Convert::ToByte("6"), Convert::ToInt16(400), 150);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11);

					wait(1);
				
					bTrack=true;
				}
				
			
			}
			else
			{
				std::cout << "got an emotion " << emotion << std::endl;

				for(int i=0;i<11;i++)
				{
					// frame, id, pos,vel)
					build_frame(frame, Convert::ToByte(i), Convert::ToInt16(expressions[emotion][i]), Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte
				}
			}

		}

		if(emys.talk!=NULL)
		{	
			puts("got a msg in talk");
			//puts(emys.talk->toString());
			bTalking=true;
			
			int loop=0;
			for(int i=1;i<emys.talk->size();i=i+3)
			{
				//std::cout << "string " << a->get(i).asString().c_str() << " open " << mouthOpenForPhoneme(a->get(i).asString().c_str()) <<std::endl;
				int iMouthPos =  mouthOpenForPhoneme(emys.talk->get(i).asString().c_str());
				double waittime = emys.talk->get(i+2).asDouble() - emys.talk->get(i+1).asDouble();
		
 
				//loop++;
				
				iMouthPos = iMouthPos*75;

				if (iMouthPos>850) iMouthPos=850;

				if (iMouthPos<500) iMouthPos=500;

				build_frame(frame, Convert::ToByte("7"), Convert::ToInt16(iMouthPos), Convert::ToInt16("200"));
				serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte*/
				/*
				if(iMouthPos>=3 && bopen==false)
				{
					build_frame(frame, Convert::ToByte("7"), Convert::ToInt16("650"), Convert::ToInt16("200"));
					serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte
					std::cout << "open " <<std::endl;
					bopen=true;
					bclose=false;
				}
				else if(iMouthPos<3 && bclose==false)
				{
					build_frame(frame, Convert::ToByte("7"), Convert::ToInt16("500"), Convert::ToInt16("200"));
					serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte
					std::cout << "close " <<std::endl;
					bclose=true;
					bopen=false;
				}
				*/
				//yarp::os::Time::delay(waittime);	
				wait(waittime-0.005);	
				//std::cout << "waittime " << waittime << " iMouthPos " << iMouthPos <<std::endl;
			}
			
		}

		if(emys.track!=NULL && bTrack)
		{	
			if(emys.track->get(0).asInt()==0)
			{
				double X = emys.track->get(1).asDouble();//4.0;
				double Y = emys.track->get(2).asDouble();//2.0;
				double Z = emys.track->get(3).asDouble();//2.0;

				//std::cout << "got data " << emys.track->get(0).asInt() << " X " << X << " Y " << Y << " Z " << Z <<std::endl;
				double ang = atan2(X,Z);
				//std::cout << "angle RAD " << Rad2Deg(ang) << " ang " << ang<< std::endl;
				//double dist = calculate_distance(iabsX, iabsY, X, Y);
				

				//build_frame(frame, Convert::ToByte("8"), Convert::ToInt16(500-Y), Convert::ToInt16(expressions[emotion][12]*2));
				//serialport->Write(frame, 0, 11); 
				//if(dist>50)
				//{
					//3.33 is motor value corresponding to one degree +/- (200-800, 500 mid point)
					iabsX = 500 + (Rad2Deg(ang)*3.33);
					iabsY = iabsY - Y;


					if(iabsY<450)
					{
						iabsY = 450;
					}
					/*	build_frame(frame, Convert::ToByte("10"), Convert::ToInt16(iabsY), Convert::ToInt16(expressions[emotion][12]));
						serialport->Write(frame, 0, 11); 

						build_frame(frame, Convert::ToByte("11"), Convert::ToInt16(iabsY), Convert::ToInt16(expressions[emotion][12]));
						serialport->Write(frame, 0, 11);

					}*/

					//build_frame(frame, Convert::ToByte("8"), Convert::ToInt16(iabsY), Convert::ToInt16(expressions[emotion][12]));
					//serialport->Write(frame, 0, 11); 



					build_frame(frame, Convert::ToByte("9"), Convert::ToInt16(iabsX), 200);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11); 

					build_frame(frame, Convert::ToByte("6"), Convert::ToInt16(500), 150);// Convert::ToInt16(expressions[emotion][12]));
					serialport->Write(frame, 0, 11);	

					

					//std::cout << "head iabsX " << iabsX << "head iabsY " << iabsY <<std::endl;
					//wait(dist/10.0);	
					wait(0.3);	
				//}

			}
			emys.track->clear();
		}
			
			//eye blink
			/*
			if(expressions[emotion][13]!=0 && loopcnt%50==0)//(loopcnt%expressions[emotion][13]==0)
			{
				build_frame(frame, Convert::ToByte("1"), Convert::ToInt16("450"), Convert::ToInt16(expressions[emotion][12]*5));
				serialport->Write(frame, 0, 11); 

				build_frame(frame, Convert::ToByte("2"), Convert::ToInt16("480"), Convert::ToInt16(expressions[emotion][12]*5));
				serialport->Write(frame, 0, 11); 

				wait(0.20);	

				build_frame(frame, Convert::ToByte(1), Convert::ToInt16(expressions[emotion][1]), Convert::ToInt16(expressions[emotion][12]*5));
				serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte

				build_frame(frame, Convert::ToByte(2), Convert::ToInt16(expressions[emotion][2]), Convert::ToInt16(expressions[emotion][12]*5));
				serialport->Write(frame, 0, 11); //Send frame from 0 to 11 byte
			}
			*/
			
			
		}
		catch (System::Exception^ ex)
		{
			Console::WriteLine(ex->Message+L"\n"); // if any problems with serial port
			return 0;
		}

		yarp::os::Time::delay(0.01);
	

		if(loopcnt>=1000)
		{
			//emotion++;
			loopcnt=0;
			//emotion_pos(emotion);
		}
	
		//if(emotion>5){emotion=0;}
		
		serialport->Close();     // Close serial port
		
	}
	


	system("Pause");
	return 0;
} // end of Main
//

//
void build_frame(array<unsigned char> ^ frame, unsigned char id, int pos, int vel)
{	
	frame[0] = 255;         // This signal notifies the beginning of the packet.
	frame[1] = 255;         // This signal notifies the beginning of the packet.
	frame[2] = id;          // It is the ID of servo which will receive Instruction Packet. It can use 254 IDs from 0 to 253 .
	frame[3] = 7;           // It is the length of the packet. The length is calculated as “the number of Parameters (N) + 2”.
	frame[4] = 3;           // Read, write, reset, etc. (3 - WRITE DATA)
	frame[5] = 30;          // Start addres in Control Table (30 - Goal Position)
	frame[6] = Convert::ToByte(pos & 0x00FF);        // Lowest byte of Goal Position  (30 in CT)
	frame[7] = Convert::ToByte((pos & 0xFF00) >> 8); // Highest byte of Goal Position (31 in CT)
	frame[8] = Convert::ToByte(vel & 0x00FF);        // Lowest byte of Moving Speed   (32 in CT) 
	frame[9] = Convert::ToByte((vel & 0xFF00) >> 8); // Highest byte of Moving Speed  (33 in CT)
	CheckSum(frame);    // ChceckSum calculation
} // end of build_frame
//
//
//
void CheckSum(array<unsigned char> ^ frame)
{
	//
	// It is used to check if packet is damaged during communication. 
	// Check Sum is calculated according to the following formula.
	// Check Sum = ~ ( ID + Length + Instruction + Parameter1 + … Parameter N )
	//
	int tmp = 0;
	tmp = frame[2] + frame[3] + frame[4] + frame[5] + frame[6] + frame[7] + frame[8] + frame[9];
	frame[10] = Convert::ToByte(tmp % 256);
	frame[10] = Convert::ToByte(255 - frame[10]);
} // end of CheckSum

void emotion_pos(int emotion)
{

	if (emotion==0)
	{
		//int neu[] = { 500, 650, 680, 500, 0, 0, 500, 500, 500, 500, 500, 500 };//neutral
		cout<<"neutral"<<endl;
		//return neu; 
	}
	else if (emotion==1)
	{
		//int anger[] = { 800, 600, 630, 0, 0, 0, 800, 500, 500, 500, 500, 500 };//anger
		cout<<"anger"<<endl;
		//return anger; 
	}
	else if (emotion==2)
	{
		//int fear[] = { 0, 600, 630, 800, 0, 0, 800, 0, 400, 500, 500, 500 };//fear
		cout<<"fear"<<endl;
		//return fear; 
	}
	else if (emotion==3)
	{
		//int sad[] = { 0, 620, 650, 800, 0, 0, 500, 500, 800, 500, 550, 550 };//sad
		cout<<"joy"<<endl;
		//return sad; 
	}
	else if (emotion==4)
	{
		//int joy[] =  { 400, 700, 730, 550, 0, 0, 500, 750, 500, 500, 500, 500 };//joy
		//return joy; 
		cout<<"surprise"<<endl;
	}
	else if (emotion==5)
	{
		//int sup[] =  { 500, 800, 830, 500, 900, 900, 0, 800, 450, 500, 500, 500 };//surprise
		cout<<"sad"<<endl;
		//return sup; 
	}
	else 
	{
		//int defa[] = { 500, 650, 680, 500, 0, 0, 500, 500, 500, 500, 500, 500 };
		cout<<"default neutral"<<endl;
		//return defa; 
	}//neutral

}


double calculate_distance (double x1,double y1,double x2 ,double y2)
{

double distance;

double distance_x = x1-x2;

double distance_y = y1- y2;

distance = sqrt( (distance_x * distance_x) + (distance_y * distance_y));

return distance;

}

double  mouthOpenForPhoneme(std::string p)
{
	if(p=="sil") return 0;
	if(p=="@") return 10;
	if(p=="@@") return 10;
	if(p=="a") return 12;
	if(p=="aa") return 12;
	if(p=="ai") return 10;
	if(p=="au") return 10;
	if(p=="b") return 3;
	if(p=="ch") return 5;
	if(p=="d") return 2;
	if(p=="dh") return 3;
	if(p=="e") return 3;
	if(p=="ei") return 10;
	if(p=="f") return 2;
	if(p=="g") return 2;
	if(p=="h") return 0;
	if(p=="i") return 10;
	if(p=="ii") return 12;
	if(p=="jh") return 3;
	if(p=="k") return 4;
	if(p=="l") return 2;
	if(p=="m") return 0;
	if(p=="n") return 1;
	if(p=="o") return 12;
	if(p=="oi") return 10;
	if(p=="oo") return 10;
	if(p=="ou") return 10;
	if(p=="p") return 0;
	if(p=="r") return 4;
	if(p=="s") return 3;
	if(p=="sh") return 5;
	if(p=="t") return 2;
	if(p=="th") return 4;
	if(p=="u") return 8;
	if(p=="uh") return 12;
	if(p=="uu") return 12;
	if(p=="v") return 1;
	if(p=="w") return 1;
	if(p=="x") return 6;	
	if(p=="y") return 3;
	if(p=="z") return 2;
	if(p=="zh") return 3;
	if(p=="Z") return 1;
	return 0;
}


double SetTargetPos(string target)
{
		if(target=="screen")return 60;
		else if(target=="deskbob")return 45;
		else if(target=="deskpaul")return -25;
		else return 1;
}