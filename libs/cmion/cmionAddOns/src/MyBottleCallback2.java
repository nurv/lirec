import yarp.Bottle;
import yarp.BottleCallback;


public class MyBottleCallback2 extends BottleCallback
{
	MyBottleCallback2()
	{
		super();
	}
	
	@Override
	public void onRead(Bottle datum) 
	{
	    System.out.println("call back 2");
	    System.out.println(datum);
	 }		
}	
