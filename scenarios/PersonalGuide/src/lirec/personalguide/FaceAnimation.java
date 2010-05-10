package lirec.personalguide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lirec.personalguide.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class FaceAnimation {

	public static enum AnimationIntent { Neutral, Happy, Sad, Fear, Disgust, Sleep }
	
	
	private Context context;
	private HashMap<AnimationIntent, List<PersonalGuideAnimation>> animations;
	private View faceView;
	private View lipsView;
	private AnimationDrawable currentFaceAnimation;
	private AnimationDrawable currentLipAnimation;
	private AnimationDrawable talkAnimation;
	private Handler handlerUI;
	private Timer timer;	
	private boolean playing;
	private boolean talking;
	private int updateMood;
	private int moodLevel;
	private int moodStates;
	private Map<Integer, PersonalGuideAnimation> moodIn;
	private Map<Integer, PersonalGuideAnimation> moodOut;
	
	public FaceAnimation(Context context, View faceView, View lipsView, Handler handlerUI){
		this.context = context;
		this.faceView = faceView;
		this.lipsView = lipsView;
		this.handlerUI = handlerUI;
		this.timer = new Timer("AnimationTimer");
		this.playing = false;
		this.talking = false;
		this.moodLevel = 0;
		this.animations = new HashMap<AnimationIntent, List<PersonalGuideAnimation>>();
		this.moodIn = new HashMap<Integer, PersonalGuideAnimation>();
		this.moodOut = new HashMap<Integer, PersonalGuideAnimation>();
		
		for (AnimationIntent intent : AnimationIntent.values()) {
			animations.put(intent, new ArrayList<PersonalGuideAnimation>());
		}
	}
	
	public void clearAnimations(){
		for (AnimationIntent intent : AnimationIntent.values()) {
			animations.get(intent).clear();
		}
	}
	
	public void importAnimations(XmlPullParser parser) throws IOException, XmlPullParserException{
		Document document = new Document();
		document.parse(parser);
		
		org.kxml2.kdom.Element root = document.getRootElement();
		
		for (int i=0; i < root.getChildCount() ; i++) {
			org.kxml2.kdom.Element group = root.getElement(i);
			
			if(group != null){
				
				List<PersonalGuideAnimation> animationList;
				if (group.getName().equals("neutral")){
					animationList = animations.get(AnimationIntent.Neutral);
				} else if (group.getName().equals("happy")){
					animationList = animations.get(AnimationIntent.Happy);
				} else if (group.getName().equals("sadness")){
					animationList = animations.get(AnimationIntent.Sad);
				} else if (group.getName().equals("fear")){
					animationList = animations.get(AnimationIntent.Fear);
				} else if (group.getName().equals("disgust")){
					animationList = animations.get(AnimationIntent.Disgust);
				} else if (group.getName().equals("sleep")){
					animationList = animations.get(AnimationIntent.Sleep);	
				} else {
					System.err.println("Unused animation group: "+group.getName());
					continue;
				}
				
				for (int j=0; j < group.getChildCount(); j++) {
					org.kxml2.kdom.Element animationElement = group.getElement(j);
					
					if(animationElement != null){
						String face = animationElement.getAttributeValue("", "face");
						PersonalGuideAnimation anim = new PersonalGuideAnimation(getAnimationId(face));
						animationList.add(anim);
					}
				}
			}
		}
	}
	
	private void updateMood(){
		currentFaceAnimation = null;
		handlerUI.post(new MoodOut());
	}
	
	private void play(){
		handlerUI.post(new MoodOut());
	}
	
	private void importMoodStates(Element moodStatesElement){
		moodStates = Integer.parseInt(moodStatesElement.getAttributeValue("", "number"));
		
		for (int j=0; j < moodStatesElement.getChildCount(); j++) {
			org.kxml2.kdom.Element moodState = moodStatesElement.getElement(j);
			
			if(moodState != null){
				Integer moodLevel = new Integer(moodState.getAttributeValue("", "level"));
				String face = moodState.getAttributeValue("", "facein");
				
				PersonalGuideAnimation anim = new PersonalGuideAnimation(getAnimationId(face));
				moodIn.put(moodLevel, anim);
				
				face = moodState.getAttributeValue("", "faceout");
				anim = new PersonalGuideAnimation(getAnimationId(face));
				moodOut.put(moodLevel, anim);
			}
		}
	}
	private int getAnimationId(String filename){
		int resourceId = context.getResources().getIdentifier(filename, "anim", context.getPackageName());
		
		return resourceId;
	}
	
	private AnimationDrawable getAnim(int resId){
		return (AnimationDrawable) context.getResources().getDrawable(resId);
	}
	
	private class MoodIn implements Runnable {
		
		@Override
		public void run() {
			if (currentFaceAnimation != null) {
				Drawable current = currentFaceAnimation.getCurrent();
				int totalFrames = currentFaceAnimation.getNumberOfFrames();
				int currentFrame = 0;
				for (int i = 0; i < totalFrames; i++) {
					Drawable frame = currentFaceAnimation.getFrame(i);
					if (current.equals(frame)) {
						currentFrame = i + 1;
						break;
					}
				}
				System.out.println("Animation stopped at " + currentFrame
						+ " of " + totalFrames + " frames.");
			}
			
			if(moodLevel != updateMood){
				moodLevel = updateMood;
			}
			
			if(currentFaceAnimation != null){
				currentFaceAnimation.stop();		
			}
			
			if(moodLevel == 0){
				return;
			}
			
			int faceDuration = 0;

			PersonalGuideAnimation animation = moodIn.get(moodLevel);
			AnimationDrawable faceAnimation;
			
			try {
				faceAnimation = getAnim(animation.face);
			} catch (RuntimeException e) {
				e.printStackTrace();
				new AnimationStopper().run();
				return;
			}
			
			faceView.setBackgroundDrawable(faceAnimation);
			
			for (int i=1 ; i < faceAnimation.getNumberOfFrames() ; i++) {
				faceDuration += faceAnimation.getDuration(i);
			}
			
			faceAnimation.start();
			timer.schedule(new AnimationStopper(), faceDuration);
		}
	}
	
	private class AnimationPlayer implements Runnable {
		
		@Override
		public void run() {
			int faceDuration = 0;
			
			if(currentFaceAnimation == null){
				new MoodIn().run();
				return;
			}
			
			faceView.setBackgroundDrawable(currentFaceAnimation);
			
			for (int i=1 ; i < currentFaceAnimation.getNumberOfFrames() ; i++) {
				faceDuration += currentFaceAnimation.getDuration(i);
			}
			
			currentFaceAnimation.start();
			handlerUI.postDelayed(new MoodIn(), faceDuration+300);
		}
	}
	
	private class AnimationStopper extends TimerTask implements Runnable {
		
		@Override
		public void run() {
			getAnim(moodIn.get(moodLevel).face).stop();
			
			if(moodLevel != updateMood){
				updateMood();
			}
		}
	}

	private class MoodOut implements Runnable {
		
		@Override
		public void run() {
			int faceDuration = 0;
			int duration;
			
			if(moodLevel == 0){
				new AnimationPlayer().run();
				return;
			}
			
			PersonalGuideAnimation animation = moodOut.get(moodLevel);
			AnimationDrawable faceAnimation; 
			
			try {
				faceAnimation = getAnim(animation.face);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				handlerUI.post(new AnimationPlayer());
				return;
			}
			
			faceView.setBackgroundDrawable(faceAnimation);
			
			for (int i=1 ; i < faceAnimation.getNumberOfFrames() ; i++) {
				faceDuration += faceAnimation.getDuration(i);
			}
			
			faceAnimation.start();
			handlerUI.postDelayed(new AnimationPlayer(), faceDuration);
		}
	}

	public void resetFace()
	{
		try {
			currentFaceAnimation = getAnim(R.anim.happy_face_sarah);
			faceView.setBackgroundDrawable(currentFaceAnimation);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void stopAnimate()
	{
		try {
			currentFaceAnimation.stop();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return;
		}
	}
	
	public boolean animateFace(AnimationIntent aIntent)
	{
		
		int faceDuration = 0;
			
		lipsView.setVisibility(View.INVISIBLE);
		List<PersonalGuideAnimation> intentAnimations =  animations.get(aIntent);
		
		if(intentAnimations.isEmpty()){
			return false;
		}
		
		int index = (int) (Math.random() * animations.get(aIntent).size());
		PersonalGuideAnimation animation = animations.get(aIntent).get(index);
		
		currentFaceAnimation = getAnim(animation.face);
		
		if(currentFaceAnimation == null){
			return false;
		}
		
		faceView.setBackgroundDrawable(currentFaceAnimation);
		
		for (int i=1 ; i < currentFaceAnimation.getNumberOfFrames() ; i++) {
			faceDuration += currentFaceAnimation.getDuration(i);
		}
		
		currentFaceAnimation.start();
		
		return true;	
	}
	
	public void Talk()
	{
		lipsView.setVisibility(View.VISIBLE);
		AnimationDrawable talkAnimation = getAnim(R.anim.talk_sarah);
		lipsView.setBackgroundDrawable(talkAnimation);
		talkAnimation.start();
	}	
	
	public void Stop()
	{
		talkAnimation.stop();
	}	
}
