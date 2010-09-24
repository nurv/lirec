#include <Samgar.h>
using namespace std;

int main()
{
    Network yarp;
    int myint;
    myint = 0;
    Samgar::SamgarModule IntReciver("Reciver","EmergencyStop","Wheels",Samgar::ModeInterupt);
    IntReciver.AddPortS("In");
    //  IntReciver.AddPortS("InExample");
  
    while( 1 )
    {
        if (IntReciver.getCurrentState() == Samgar::StateRunning)
        {
            // in this module we accept int data from the specified port. When we call a port for data a true or false returns specifying whether it is new or old data,
            // and the data reference we pass gets changed if true
            if(IntReciver.GetIntData("In",&myint)==true)
            {
                printf("got data %d \n",myint);
                // in this module we also update the GUI log with this command.
                if(myint % 5 == 0)
                {
                    IntReciver.SendToLog("this has come from a port",3);
                }
            }
            else
            {
                // this will never be printed proving the module sleeps after each new input
                //printf("got no data \n");
            }
        }
        // in an interrupt module this function call has added
        // significance, the primary being the true or false whether it
        // has accomplished its task,  and secondly a variable which could
        // be how well it has acheived the task. This data is sent on to
        // the GUI so the success rate of the modules can be deduced
        IntReciver.SucceedFail(true,myint);
    }
    return 0;
}
