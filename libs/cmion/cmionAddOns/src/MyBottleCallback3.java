import yarp.Bottle;
import yarp.BottleCallback;


	public class MyBottleCallback3 extends BottleCallback
	{
		MyBottleCallback3()
		{
			super();
		}
		
		@Override
		public void onRead(Bottle datum) 
		{
		    System.out.println("call back 3");
		    System.out.println(datum);
		 }		
	}