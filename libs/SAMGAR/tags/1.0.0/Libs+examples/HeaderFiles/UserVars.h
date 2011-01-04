

#include <yarp/os/all.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <list>
#include <yarp/sig/all.h> //image stuff
#include <math.h>
#include <cxtypes.h>
#include <fstream>



using namespace std;
using namespace yarp::os; // network stuff
using namespace yarp::sig;// image stuff
//using namespace yarp::os;

#define TypeInt 0
#define TypeFloat 1
#define TypeString 2
#define TypeDouble 3
#define TypeBottle 4

// types of module
#define interupt 0
#define run      1
// current mode
#define running 0
#define paused 1
#define stoped 2
#define fullstop 3
