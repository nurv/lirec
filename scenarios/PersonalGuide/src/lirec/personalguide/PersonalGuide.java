package lirec.personalguide;

import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.io.IOException;
import java.util.Timer;

import lirec.personalguide.FaceAnimation.AnimationIntent;
import lirec.personalguide.events.EventChangeEmotion;
import lirec.personalguide.events.EventTalk;
import lirec.personalguide.events.EventUserSubmit;
import lirec.personalguide.R;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import cmion.architecture.AndroidArchitecture;
import cmion.architecture.CmionEvent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;;


public class PersonalGuide extends Activity implements OnClickListener{
	
	/** the personal guide application */
	private PersonalGuideApplication app;
	
	/** the cmion architecture */
	private AndroidArchitecture architecture;
	
	/** an ion element representing the personal guide gui */
	private PersonalGuideIONElement element;
	
	/** user options to be displayed in the spinner */
	private ArrayAdapter<String> userOptionsAdapter;
	
	public final Handler handler = new Handler();
	FaceAnimation animator;
	Timer timer;
	
	private static final int MENU_JOY = 0;
	private static final int MENU_NEUTRAL =1;
	private static final int MENU_SAD = 2;
	private static final int MENU_TALK = 3;
	
	/** the text view that displays sarah's current utterance */
	private TextView text;
	/** the submit button */
	private Button btnSubmit;
	/** the spinner for selecting user options */
	private Spinner spinner;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
           
		// acquire a reference to the android application object
		app = (PersonalGuideApplication) getApplication();
        
        XmlPullParser parser = null;
        ViewGroup mainView = (ViewGroup) getLayoutInflater().inflate(R.layout.main, null); 
        //mainView.addView();      
        setContentView(mainView);
        animator = new FaceAnimation(this, findViewById(R.id.PersonalGuideFace), findViewById(R.id.PersonalGuideLips), handler);
        
        //Drawable face = getResources().getDrawable(R.drawable.arrogant_face_human_000);
        //mainView.setBackgroundDrawable(face);
        text = (TextView) findViewById(R.id.TextViewTalk);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);
        btnSubmit.setEnabled(false);
        spinner = (Spinner) findViewById(R.id.Spinner01);
        
        userOptionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        userOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(userOptionsAdapter);
        
        try {
			parser = new KXmlParser();
			parser.setInput(getResources().openRawResource(R.raw.animations_sarah), "UTF-8");
			animator.importAnimations(parser);
		} catch (XmlPullParserException e) {
			System.err.println("Could not import animations.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not import animations.");
			e.printStackTrace();
		}

        // create an Ion element for this gui, so we can communicate between gui and cmion
        // via ion events
        element = new PersonalGuideIONElement();
        Simulation.instance.getElements().add(element);
        element.registerListeners();

		// start cmion
        architecture = AndroidArchitecture.startup("architectureconfiguration", this,getApplication());        
    }

    
	@Override
	public void onClick(View v) 
	{
		if (v == btnSubmit)
		{
			if (spinner.getSelectedItem() != null)
			{
				element.raise(new EventUserSubmit(spinner.getSelectedItem().toString()));
				btnSubmit.setEnabled(false);
			}
		}
	}
	
	@Override
    protected void onPause() {
    	super.onPause();
    	
    	timer.cancel();
    	timer = null;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	timer = new Timer("Update ", true);
    }    	
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	System.out.println("DESTROYING");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(0,MENU_NEUTRAL,0,"neutral");
    	menu.add(0,MENU_JOY,0,"joy");    	
    	menu.add(0,MENU_SAD,0,"sad");
    	menu.add(0,MENU_TALK,0,"talk");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
		if(item.getItemId() == MENU_NEUTRAL)
		{
			animator.animateFace(AnimationIntent.Neutral);
			return true;
		}
		else if (item.getItemId() == MENU_JOY)
		{
			animator.animateFace(AnimationIntent.Happy);
			return true;
		}
		else if (item.getItemId() == MENU_SAD)
		{
			animator.animateFace(AnimationIntent.Sad);
			return true;
		}
		else if (item.getItemId() == MENU_TALK)
		{
			text.setText("Hello my name is Sarah.");
			animator.Talk();
			return true;
		}
		return false;
    }
    
    private Handler emotionHandler = new Handler()
    {
    	
    	@Override
    	public void handleMessage(Message msg)
    	{
    		String emotion = msg.obj.toString();
    		if(emotion.equals("neutral"))
    			animator.animateFace(AnimationIntent.Neutral);
			else if (emotion.equals("joy"))
    			animator.animateFace(AnimationIntent.Happy);
			else if (emotion.equals("sadness"))
    			animator.animateFace(AnimationIntent.Sad);
			else if (emotion.equals("sleep"))
    			animator.animateFace(AnimationIntent.Sleep);
    	}
    	
    };
    
    private Handler talkHandler = new Handler()
    {
    	
    	@Override
    	public void handleMessage(Message msg)
    	{
    		EventTalk evtTalk = (EventTalk) msg.obj;
    		
    		// talk
    		text.setText(evtTalk.getUtterance());
			animator.Talk();
    		
			// set user options
			userOptionsAdapter.clear();
			if (evtTalk.getUserOptions().size()>0)
			{	
				for (String option : evtTalk.getUserOptions())
					userOptionsAdapter.add(option);
				btnSubmit.setEnabled(true);
			}
			else
				btnSubmit.setEnabled(false);
	
    	}
    	
    };
    
    private class PersonalGuideIONElement extends Element
    {
    	public void registerListeners()
    	{
    		Simulation.instance.getEventHandlers().add(new HandleEventTalk());
    		Simulation.instance.getEventHandlers().add(new HandleEventChangeEmotion());
    	}
    	
		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub			
		}
		
		/** internal event handler class for listening to talk events */
		private class HandleEventTalk extends EventHandler {

		    public HandleEventTalk() {
		        super(EventTalk.class);
		    }

		    @Override
		    public void invoke(IEvent evt) 
		    {
		    	EventTalk evtTalk = (EventTalk) evt;
		    	Message msg = talkHandler.obtainMessage();
		    	msg.obj = evtTalk;
		    	talkHandler.sendMessage(msg);
		    }
		}
		
		/** internal event handler class for listening to change of emotion events */
		private class HandleEventChangeEmotion extends EventHandler {

		    public HandleEventChangeEmotion() {
		        super(EventChangeEmotion.class);
		    }

		    @Override
		    public void invoke(IEvent evt) 
		    {
		    	EventChangeEmotion evtCE = (EventChangeEmotion) evt;
		    	String emotion = evtCE.getEmotion();
		    	Message msg = emotionHandler.obtainMessage();
		    	msg.obj = emotion;
		    	emotionHandler.sendMessage(msg);
		    }
		}
    	
    }

}