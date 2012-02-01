package uk.ac.hw.lirec.dialogsystem;

import uk.ac.hw.lirec.emys3d.EmysModel;
import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;
import uk.ac.hw.lirec.threedtest.ThreeDTestActivity;
import android.app.Activity;

/**
 * @author iw24
 * For now this is just a test class, hence the hacky approach of passing it the activity, so
 * it can call methods on it.
 */
public class AndroidDialogInterface extends DialogInterface {

	private static final long MOMENTARY_EXPRESSION_TIME = 600; //ms to show an expression 
	
	private Activity mActivity;
	private AndroidDialogProvider mProvider;
	private boolean mInterrupted = false;
	private boolean mWaiting = false;
	
	public AndroidDialogInterface(Activity mainActivity, AndroidDialogProvider provider ) {
		mActivity = mainActivity;
		mProvider = provider;
	}
	@Override
	public void speakText(final String text) {
		if (mInterrupted)
			return;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.speakText(text);	
			}
		});
		waitForCallback();
	}
	
	@Override
	public void getResponse(final String infoText ) {
		if (mInterrupted)
			return;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.confirmDialog(infoText);	
			}
		});

		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
			mProvider.dismissConfirmDialog();
		}
	}
	
	@Override
	public synchronized void interruptDialog() {
		// TODO Auto-generated method stub
		mInterrupted = true;
		this.notify();
	}
	
	public void stopWaiting() { mWaiting = false;}
	
	public synchronized void stopWaitingAndNotify() {
		stopWaiting();
		this.notify();
	}
	
	@Override
	public void resetDi() {

		mWaiting = false;
		mInterrupted = false;
	}
	
	public boolean isWaiting() { return mWaiting;}
	
	
	private String multiChoiceAnswer = new String();
	
	public void setMultiChoiceAnswer(String ans) { multiChoiceAnswer = ans;}
	
	@Override
	public String multipleChoiceQuestion(final Integer numChoices, final String[] options) {
		if (mInterrupted)
			return "";
		
		multiChoiceAnswer = "";
		
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.askMultiChoice(numChoices,options);	
			}
		});

		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
			mProvider.dismissMultiDialog();
		}
		return multiChoiceAnswer;
	}
	
	private EmysModel.Emotion mood2emotion( Moods mood) {		
		switch (mood)  {
			case ANGER : return Emotion.ANGER;
			case NEUTRAL : return Emotion.NEUTRAL;
			case JOY : return Emotion.JOY;
			case SADNESS : return Emotion.SADNESS;
			case SLEEP : return Emotion.SLEEP;
			default : 	System.out.println("WARNING unknown mapping from mood: "+mood+" to emotion!");
						return Emotion.NEUTRAL;
		}	
	}
	
	private EmysModel.Emotion expression2emotion(Expression exp) {
		switch (exp)  {
		case ANGER : return Emotion.ANGER;
		case SURPRISE : return Emotion.SURPRISE;
		case JOY : return Emotion.JOY;
		default : 	System.out.println("WARNING unknown mapping from expression: "+exp+" to emotion!");
					return Emotion.NEUTRAL;
		}
	}
	
	@Override
	public void setMood(Moods mood) {
		if (mInterrupted)
			return;
		
		mProvider.setExpression(mood2emotion(mood));
	}
	@Override
	public void showExpression(Expression expression) {
		if (mInterrupted)
			return;
		
		EmysModel.Emotion current = mProvider.getEmysEmotion();
		mProvider.setExpression(expression2emotion(expression));
		try {
			Thread.sleep(MOMENTARY_EXPRESSION_TIME); //try 600 milli
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (mInterrupted)
			return; //check here too incase this was interrupted. 
		mProvider.setExpression(current);
	}

	
	private synchronized void waitForCallback() {
		mWaiting = true;
		while (mWaiting && !mInterrupted) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
}
