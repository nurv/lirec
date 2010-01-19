import yarp.BufferedPortBottle;
import yarp.Network;
import yarp.Bottle;
import yarp.BottleCallback;

class Example1 extends BufferedPortBottle {

	
    public static void main(String[] args) {
	System.loadLibrary("jyarp");
	Network.init();
	Example1 port = new Example1();
	
	
	yarp.Time.delay(2);
	BufferedPortBottle p1 = new BufferedPortBottle();
	p1.useCallback(new MyBottleCallback2());
	p1.open("/Port_CMion_port1");
	
	yarp.Time.delay(2);
	BufferedPortBottle p2 = new BufferedPortBottle();
	p2.useCallback(new MyBottleCallback3());
	p2.open("/Port_CMion_port2");
	
	yarp.Time.delay(2);
		
	Bottle b = port.prepare();
	b.addInt(10);
	b.addString("CMion");
	b.addString("Control");
	b.addString("Control");
	System.out.println(b);
	port.write();
	
	/*	
	while(true)
	{
		while (getPendingReads()<1)
		{
		
		}
		System.out.println(getPendingReads());
		Bottle b2 = read();
		System.out.println(b2);
		
	} 
	*/
	
	
	/*
	Port p = new Port();
	p.open("/foo");
	// connect to a port called "/bar" if present
	Network.connect("/foo","/bar");
	while(true) {
	    Bottle bot = new Bottle();
	    bot.addDouble(10.4);
	    bot.addString("bozo");
	    System.out.println("sending bottle: " + bot);
	    p.write(bot);
	    Time.delay(1);
	}
	
	//Network.fini(); // never reached*/
    while (true) {}
    } 
	
	
	Example1()
	{
		useCallback(new MyBottleCallback());      // set the port to use onRead
		while (Network.getNameServerName().toString().equals("/global"))
		{}   // a loop to wait until the network is on the local and not global server
		open("/Main_CMion"); // open the port, this will be the main module port you'll get updates on
		while(getInputCount()<1){} // you might want a line like this to know that the samgar gui 
								   // has actually connected to the maim module port
		System.out.println(getInputCount());
		System.out.println(this.getPendingReads());
		
	}
	
	
	private class MyBottleCallback extends BottleCallback
	{
		MyBottleCallback()
		{
			super();
		}
		
		@Override
		public void onRead(Bottle datum) 
		{
		    System.out.println("call back 1");
		    System.out.println(datum);
		 }		
	}
		
}
	


