package eu.lirec.myfriend.competences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.kxml2.kdom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import eu.lirec.myfriend.competences.Manager.SayIntent;
import eu.lirec.myfriend.events.Failed;
import eu.lirec.myfriend.events.Successful;
import eu.lirec.myfriend.requests.Say;
import eu.lirec.myfriend.requests.StartTalking;
import eu.lirec.myfriend.requests.StopTalking;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

public class SpeechPlayback extends Competence implements OnErrorListener {

	private Context context;	
	private Map<String, Integer> soundMapping;
	private Timer timer;
	private Competence animator;

	public SpeechPlayback(Context context, XmlPullParser parser) throws IOException, XmlPullParserException {
		this.context = context;
		this.soundMapping = new HashMap<String, Integer>();
		this.timer = new Timer("SpeechPlayback", true);
		
		importSoundMappings(parser);
		
		//set ION related components
		this.getRequestHandlers().add(new SayHandler());
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}
	
	public void clearSoundMappings(){
		soundMapping.clear();
	}
	
	public void importSoundMappings(XmlPullParser parser) throws IOException, XmlPullParserException{
		Document document = new Document();
		document.parse(parser);
		
		org.kxml2.kdom.Element root = document.getRootElement();
		
		for (int i=0; i < root.getChildCount(); i++) {
			org.kxml2.kdom.Element mapping = root.getElement(i);
			
			if (mapping != null){
				String text;
				String soundFile;
				Integer resourceId;
				
				text = mapping.getAttributeValue("", "text");
				soundFile = mapping.getAttributeValue("", "file");
				resourceId = context.getResources().getIdentifier(soundFile, "raw", context.getPackageName());
				
				if(resourceId == 0)
					System.err.println("Resource "+ soundFile +" has id "+resourceId);
				
				soundMapping.put(text, resourceId);
			}
		}
	}
		
	public void speak(String text){

		if(!this.soundMapping.containsKey(text)){
			raise(new Failed());
			return;
		}
		
		int resourceId = this.soundMapping.get(text);
		
		MediaPlayer mp = MediaPlayer.create(context, resourceId);
		if (mp == null){
			raise(new Failed());
			return;
		}

		KillSound killsound = new KillSound(mp);
		mp.setOnCompletionListener(new OnCompletion(killsound));
		mp.setOnErrorListener(this);
		mp.setVolume(1, 1);
		if(animator != null){
			animator.schedule(new StartTalking());
		}
		mp.start();
		killsound.schedule();
	}
	
	public void setAnimateCompetence(Competence animator){
		this.animator = animator;
	}
	
	private class OnCompletion implements OnCompletionListener {
	
		private TimerTask task;
		
		public OnCompletion(TimerTask task){
			this.task = task;
		}
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			task.cancel();
			mp.release();
			raise(new Successful());
			if(animator != null){
				animator.schedule(new StopTalking());
			}
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		System.err.println("Media Player error occurred "+what+" "+extra+".");
		raise(new Failed());
		return false;
	}
	
	private class KillSound extends TimerTask {
		
		private MediaPlayer mp;
		
		public KillSound(MediaPlayer mp) {
			this.mp = mp;
		}
		
		public void schedule(){
			int duration = mp.getDuration();
			duration += 500;
			timer.schedule(this, duration);
		}
		
		@Override
		public void run() {
			if(mp.isPlaying())
				System.out.println("Media Player still playing");
			
			System.err.println("MediaPlayer killed.");
			mp.release();
			raise(new Failed());
			if(animator != null){
				animator.schedule(new StopTalking());
			}
		}
	}
	
	
	private class SayHandler extends RequestHandler {
		public SayHandler() {
			super(new TypeSet(Say.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for (Say request : requests.get(Say.class)) {
				if(request.intent.equals(SayIntent.Text)){
					speak(request.text);
					//TODO instead of using only the first request, queue them up.
					break;
				}
			}
		}
	}
}
