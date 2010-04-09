package eu.lirec.myfriend.competences;

import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import eu.lirec.myfriend.ICatAnimation;
import eu.lirec.myfriend.competences.Manager.AnimationIntent;
import eu.lirec.myfriend.events.Failed;
import eu.lirec.myfriend.events.MoodChanged;
import eu.lirec.myfriend.events.Successful;
import eu.lirec.myfriend.requests.Animate;
import eu.lirec.myfriend.requests.ChangeMood;
import eu.lirec.myfriend.requests.StartTalking;
import eu.lirec.myfriend.requests.StopTalking;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

public class FaceAnimation extends Competence {
	
	private Context context;
	private HashMap<AnimationIntent, List<ICatAnimation>> animations;
	private View faceView;
	private View lipsView;
	private AnimationDrawable currentFaceAnimation;
	private AnimationDrawable currentLipAnimation;
	private AnimationDrawable talkAnimation;
	private Animate currentRequest;
	private Handler handlerUI;
	private Timer timer;
	private int moodStates;
	private Map<Integer, ICatAnimation> moodIn;
	private Map<Integer, ICatAnimation> moodOut;
	private boolean playing;
	private boolean talking;
	private int updateMood;
	private int moodLevel;
	
	public FaceAnimation(Context context, View faceView, View lipsView, Handler handlerUI){
		this.context = context;
		this.faceView = faceView;
		this.lipsView = lipsView;
		this.handlerUI = handlerUI;
		this.timer = new Timer("AnimationTimer");
		this.animations = new HashMap<AnimationIntent, List<ICatAnimation>>();
		this.moodIn = new HashMap<Integer, ICatAnimation>();
		this.moodOut = new HashMap<Integer, ICatAnimation>();
		this.playing = false;
		this.moodLevel = 0;
		this.updateMood = 0;
		this.talking = false;
		
		for (AnimationIntent intent : AnimationIntent.values()) {
			animations.put(intent, new ArrayList<ICatAnimation>());
		}
		
		this.getRequestHandlers().add(new AnimationHandler());
//		this.getRequestHandlers().add(new MoodHandler());
		this.getRequestHandlers().add(new TalkHandler());
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}
	
	public boolean isPlaying(){
		return this.playing;
	}
	
	public void resetFace(){
		handlerUI.post(new FaceReset());
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
				if(group.getName().equals("moodstates")){
					importMoodStates(group);
					continue;
				}
				
				if(group.getName().equals("talk")){
					int id = getAnimationId(group.getAttributeValue("", "lips"));
					talkAnimation = getAnim(id);
					continue;
				}
				
				List<ICatAnimation> animationList;
				
				if(group.getName().equals("illegalmove")){
					animationList = animations.get(AnimationIntent.IllegalMove);
				} else if (group.getName().equals("wrongturn")){
					animationList = animations.get(AnimationIntent.WrongTurn);
				} else if (group.getName().equals("winning")){
					animationList = animations.get(AnimationIntent.Winning);
				} else if (group.getName().equals("losing")){
					animationList = animations.get(AnimationIntent.Losing);
				} else if (group.getName().equals("draw")){
					animationList = animations.get(AnimationIntent.Draw);
				} else if (group.getName().equals("more-excited")){
					animationList = animations.get(AnimationIntent.MoreExcited);
				} else if (group.getName().equals("excited")){
					animationList = animations.get(AnimationIntent.Excited);
				} else if (group.getName().equals("less-excited")){
					animationList = animations.get(AnimationIntent.LessExcited);
				} else if (group.getName().equals("good-surprise")){
					animationList = animations.get(AnimationIntent.GoodSurprise);
				} else if (group.getName().equals("think")){
					animationList = animations.get(AnimationIntent.Think);
				} else if (group.getName().equals("bad-surprise")){
					animationList = animations.get(AnimationIntent.BadSurprise);
				} else if (group.getName().equals("less-unhappy")){
					animationList = animations.get(AnimationIntent.LessUnhappy);
				} else if (group.getName().equals("unhappy")){
					animationList = animations.get(AnimationIntent.Unhappy);
				} else if (group.getName().equals("more-unhappy")){
					animationList = animations.get(AnimationIntent.MoreUnhappy);
				} else {
					System.err.println("Unused animation group: "+group.getName());
					continue;
				}
				
				for (int j=0; j < group.getChildCount(); j++) {
					org.kxml2.kdom.Element animationElement = group.getElement(j);
					
					if(animationElement != null){
						String face = animationElement.getAttributeValue("", "face");
						String lips = animationElement.getAttributeValue("", "lips");
						ICatAnimation anim = new ICatAnimation(getAnimationId(face), getAnimationId(lips));
						animationList.add(anim);
					}
				}
			}
		}
	}
	
	private void importMoodStates(Element moodStatesElement){
		moodStates = Integer.parseInt(moodStatesElement.getAttributeValue("", "number"));
		
		for (int j=0; j < moodStatesElement.getChildCount(); j++) {
			org.kxml2.kdom.Element moodState = moodStatesElement.getElement(j);
			
			if(moodState != null){
				Integer moodLevel = new Integer(moodState.getAttributeValue("", "level"));
				String face = moodState.getAttributeValue("", "facein");
				String lips = moodState.getAttributeValue("", "lipsin");
				ICatAnimation anim = new ICatAnimation(getAnimationId(face), getAnimationId(lips));
				moodIn.put(moodLevel, anim);
				
				face = moodState.getAttributeValue("", "faceout");
				lips = moodState.getAttributeValue("", "lipsout");
				anim = new ICatAnimation(getAnimationId(face), getAnimationId(lips));
				moodOut.put(moodLevel, anim);
			}
		}
	}
	
	private void play(){
		playing = true;
		handlerUI.post(new MoodOut());
	}
	
	private void updateMood(){
		currentFaceAnimation = null;
		handlerUI.post(new MoodOut());
	}
	
	private int getAnimationId(String filename){
		int resourceId = context.getResources().getIdentifier(filename, "anim", context.getPackageName());
		
		return resourceId;
//		return (AnimationDrawable) context.getResources().getDrawable(resourceId); 
	}
	
	private AnimationDrawable getAnim(int resId){
		return (AnimationDrawable) context.getResources().getDrawable(resId);
	}
	
	private class TalkHandler extends RequestHandler {

		public TalkHandler() {
			super(new TypeSet(StartTalking.class, StopTalking.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			if(talkAnimation == null){
				return;
			}
			
			if(!requests.get(StopTalking.class).isEmpty()){
				talking = false;
				handlerUI.post(new TalkStopper());
				return;
			}
			
			if(!requests.get(StartTalking.class).isEmpty()){
				talking = true;
				handlerUI.post(new TalkStarter());
				return;
			}
		}
	}
	
	private class AnimationHandler extends RequestHandler {
		
		public AnimationHandler() {
			super(new TypeSet(Animate.class, ChangeMood.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {

			for (Animate request : requests.get(Animate.class)) {
				System.out.println("Received request for "+request.intent);
				if(playing || !performAnimation(request)){
					raise(new Failed(request));
					continue;
				}
				break;
			}
			
			
			if (!requests.get(ChangeMood.class).isEmpty()) {
				System.out.println("Received mood change request");
				ChangeMood request = requests.get(ChangeMood.class).getFirst();
				performMoodChange(request);
			}
		}
		
		private void performMoodChange(ChangeMood request){
			
			updateMood = new Double(Math.floor(request.mood
					/ (100 / moodStates))).intValue();
			
			if (updateMood != moodLevel) {
				raise(new MoodChanged(moodLevel, updateMood));

				if (!playing) {
					updateMood();
				}
			}
		}
		
		private boolean performAnimation(Animate request){
		
			List<ICatAnimation> intentAnimations =  animations.get(request.intent);
			
			if(intentAnimations.isEmpty()){
				return false;
			}
			
			int index = (int) (Math.random() * animations.get(request.intent).size());
			ICatAnimation animation = animations.get(request.intent).get(index);
			
			try {
				currentFaceAnimation = getAnim(animation.face);
				currentLipAnimation = getAnim(animation.lips);
			} catch (OutOfMemoryError e) {
				System.err.println(request.intent);
				e.printStackTrace();
				return false;
			}
			
			System.out.println("Animation chosen: "+animation.face);
			
			currentRequest = request;
			play();
			return true;
		}
	}
	
	//Currently handled in AnimationHandler
	/*
	private class MoodHandler extends RequestHandler {

		public MoodHandler() {
			super(new TypeSet(ChangeMood.class));
		}

		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			System.out.println("Received mood change request");
			
			ChangeMood request = requests.get(ChangeMood.class).getFirst();
			
			updateMood = new Double(Math.floor(request.mood / (100/moodStates))).intValue();
			if(updateMood != moodLevel){
				raise(new MoodChanged(moodLevel, updateMood));
				
				if(!playing){
					updateMood();
				}
			}
		}
	}
	*/
	
	private class MoodOut implements Runnable {
		
		@Override
		public void run() {
			int faceDuration = 0;
			int lipsDuration = 0;
			int duration;
			
			if(moodLevel == 0){
				new AnimationPlayer().run();
				playing = false;
				return;
			}
			
			ICatAnimation animation = moodOut.get(moodLevel);
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
			
			if(!talking){
				AnimationDrawable lipAnimation;
				
				try{
					lipAnimation = getAnim(animation.lips);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					handlerUI.post(new AnimationPlayer());
					return;
				}
				
				lipsView.setBackgroundDrawable(lipAnimation);
				
				for (int i=1 ; i < lipAnimation.getNumberOfFrames() ; i++) {
					lipsDuration += lipAnimation.getDuration(i);
				}
				
				lipAnimation.start();
			}
			
			faceAnimation.start();
			handlerUI.postDelayed(new AnimationPlayer(), Math.max(lipsDuration, faceDuration));
		}
	}
	
	private class AnimationPlayer implements Runnable {
		
		@Override
		public void run() {
			int faceDuration = 0;
			int lipsDuration = 0;
			
			if(currentFaceAnimation == null){
				new MoodIn().run();
				return;
			}
			
			faceView.setBackgroundDrawable(currentFaceAnimation);
			
			for (int i=1 ; i < currentFaceAnimation.getNumberOfFrames() ; i++) {
				faceDuration += currentFaceAnimation.getDuration(i);
			}
			
			if(!talking){
				lipsView.setBackgroundDrawable(currentLipAnimation);
				
				for (int i=1 ; i < currentLipAnimation.getNumberOfFrames() ; i++) {
					lipsDuration += currentLipAnimation.getDuration(i);
				}
				
				currentLipAnimation.start();
			}
			
			currentFaceAnimation.start();
			handlerUI.postDelayed(new MoodIn(), Math.max(faceDuration, lipsDuration)+450);
		}
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
				if(!talking){
					currentLipAnimation.stop();
				}
			}
			
			if(moodLevel == 0){
				playing = false;
				raise(new Successful(currentRequest));
				System.out.println("Animate success event raised!!!");
				return;
			}
			
			int faceDuration = 0;
			int lipsDuration = 0;
			
			ICatAnimation animation = moodIn.get(moodLevel);
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
			
			if(!talking){
				AnimationDrawable lipAnimation;
				try {
					lipAnimation = getAnim(animation.lips);
				} catch (RuntimeException e) {
					e.printStackTrace();
					new AnimationStopper().run();
					return;
				}
				
				lipsView.setBackgroundDrawable(lipAnimation);
				
				for (int i=1 ; i < lipAnimation.getNumberOfFrames() ; i++) {
					lipsDuration += lipAnimation.getDuration(i);
				}
				
				lipAnimation.start();
			}
			
			faceAnimation.start();
			timer.schedule(new AnimationStopper(), Math.max(faceDuration, lipsDuration));
		}
	}
	
	private class AnimationStopper extends TimerTask implements Runnable {
		
		@Override
		public void run() {
			playing = false;
			getAnim(moodIn.get(moodLevel).face).stop();
			getAnim(moodIn.get(moodLevel).lips).stop();
			raise(new Successful(currentRequest));
			System.out.println("Animate success event raised!!!");
			
			if(moodLevel != updateMood){
				updateMood();
			}
		}
	}

	private class TalkStarter implements Runnable {
		@Override
		public void run() {
			if(talkAnimation != null){
				lipsView.setBackgroundDrawable(talkAnimation);
				talkAnimation.start();
			}
		}
	}
	
	private class TalkStopper implements Runnable {
		@Override
		public void run() {
			if(talkAnimation != null){
				talkAnimation.stop();
				
				if(moodLevel == 0){
					lipsView.setBackgroundResource(moodIn.get(1).lips);
				} else {
					lipsView.setBackgroundResource(moodOut.get(moodLevel).lips);
				}
			}
		}
	}

	private class FaceReset implements Runnable{
		
		@Override
		public void run() {
			if(moodLevel == 0){
				lipsView.setBackgroundResource(moodIn.get(1).lips);
				faceView.setBackgroundResource(moodIn.get(1).face);
			} else {
				lipsView.setBackgroundResource(moodOut.get(moodLevel).lips);
				faceView.setBackgroundResource(moodOut.get(moodLevel).face);
			}
		}
	}
}
