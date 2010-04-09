package eu.lirec.myfriend;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import eu.lirec.myfriend.Emotivector.MoodModel;
import eu.lirec.myfriend.competences.ExecuteMonitor;
import eu.lirec.myfriend.competences.FaceAnimation;
import eu.lirec.myfriend.competences.Manager;
import eu.lirec.myfriend.competences.Migration;
import eu.lirec.myfriend.competences.PlayMove;
import eu.lirec.myfriend.competences.SpeechPlayback;
import eu.lirec.myfriend.competences.TextDisplay;
import eu.lirec.myfriend.events.GameOver;
import eu.lirec.myfriend.events.GameStarted;
import eu.lirec.myfriend.events.UndoAvailable;
import eu.lirec.myfriend.events.UndoUnavailable;
import eu.lirec.myfriend.requests.Animate;
import eu.lirec.myfriend.requests.MakeMove;
import eu.lirec.myfriend.requests.Migrate;
import eu.lirec.myfriend.requests.Say;
import eu.lirec.myfriend.requests.UserUndo;

public class MyFriendChessGame extends Activity implements DialogInterface.OnClickListener, ViewFactory{
	
	static final int MIGRATE_DIALOG = 1;
	static final int AGENT_DIALOG = 2;
	static final int PIECE_DIALOG = 3;
	
	public static final CharSequence[] pieceList = {"Pawn", "Rook", "Knight", "Bishop", "Queen" };
	
	public final Handler handler = new Handler();
	
	ChessView mView;
	Simulation simulation = Simulation.instance;
	Timer timer;
	UpdateSimulation updateSimulation;
	
	WindowManager wmanage;
	View goView;
	private Migration migration;
	private CharSequence[] migrationItems;
	private ChessAgent agent;
	private ChessBoard board;
	private FaceAnimation animator;
	private SpeechPlayback speech;
	private ExecuteMonitor execMon;
	private AgentLoader agentLoader;
	private ChessUser user;
	private TimerTask task;
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    menu.add(Menu.NONE, 1, Menu.NONE, "Migrate");
//	    menu.add(Menu.NONE, 2, Menu.NONE, "Agents");
//	    menu.add(Menu.NONE, 3, Menu.NONE, "Kill");
//	    menu.add(Menu.NONE, 4, Menu.NONE, "Wipe Memory");
	    return true;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        XmlPullParser parser = null;
        
        //Setup content view and other UI items
        ViewGroup mainView = (ViewGroup) getLayoutInflater().inflate(R.layout.main, null); 
        setContentView(mainView);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);        
        TextSwitcher messageBox = (TextSwitcher) findViewById(R.id.MessageBox);
        messageBox.setFactory(this);
        messageBox.setInAnimation(in);
        messageBox.setOutAnimation(out);
        
        agentLoader = new AgentLoader();
        try {
			parser = new KXmlParser();
			parser.setInput(getResources().openRawResource(R.raw.agent_configurations),null);
			agentLoader.importConfig(parser);
		} catch (XmlPullParserException e1) {
			System.err.println("Couldn't load agent configurations.");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Couldn't load agent configurations.");
			e1.printStackTrace();
		}

        //setting up ION simulation
        execMon = new ExecuteMonitor();
        Manager manager = new Manager(execMon);
        board = new ChessBoard();
        user = new ChessUser(board, this);
        ChessEngine engine = new ChessEngine();
        agent = new ChessAgent(board, engine, manager);
        animator = new FaceAnimation(this, findViewById(R.id.iCatFace), findViewById(R.id.iCatLips), handler);
        agent.setOpponent(user);
        try {
			agent.loadMemory(openFileInput("memory.xml"));
		} catch (FileNotFoundException e1) {
			System.out.println("No existing memory file to load.");
		}
        user.setOpponent(agent);
        
        PlayMove playMove = new PlayMove(board);
        TextDisplay dialog = new TextDisplay(messageBox, handler);
        
        try {
			parser = new KXmlParser();
			parser.setInput(getResources().openRawResource(R.raw.sentences_motivate), "UTF-8");
			execMon.importSentences(parser);
		} catch (XmlPullParserException e) {
			System.err.println("Could not import sentences.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not import sentences.");
			e.printStackTrace();
		}
		
		try {
			parser = new KXmlParser();
			parser.setInput(getResources().openRawResource(R.raw.animations), "UTF-8");
			animator.importAnimations(parser);
		} catch (XmlPullParserException e) {
			System.err.println("Could not import animations.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not import animations.");
			e.printStackTrace();
		}
        
        try {
        	parser = new KXmlParser();
        	parser.setInput(getResources().openRawResource(R.raw.soundmappings_male), "UTF-8");
			speech = new SpeechPlayback(this, parser);
			speech.setAnimateCompetence(animator);
		} catch (IOException e) {
			System.err.println("Could not open configuration file.");
		} catch (XmlPullParserException e) {
			System.err.println("Could not parse configuration file.");
			e.printStackTrace();
		}
		
		try{
			parser = new KXmlParser();
			parser.setInput(getResources().openRawResource(R.raw.migrationconfig), "UTF-8");
			migration = new Migration(parser);
			migration.enableAnimations(findViewById(R.id.iCat), this, handler);
		} catch (IOException e) {
			System.err.println("Could not open migration configuration file.");
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			System.err.println("Could not parse migration configuration file.");
			e.printStackTrace();
		}
		
		board.setMigrationCompetence(migration);
		agent.setMigrationCompetence(migration);
        
        execMon.registerCompetence(dialog, Say.class);
        if(speech != null){
        	execMon.registerCompetence(speech, Say.class);
        }
        if(migration != null){
        	execMon.registerCompetence(migration, Migrate.class);
        }
        execMon.registerCompetence(playMove, MakeMove.class);
        execMon.registerCompetence(animator, Animate.class);

        simulation.getElements().add(user);
        simulation.getElements().add(board);
        simulation.getElements().add(agent);
        simulation.getElements().add(engine);
        simulation.getElements().add(dialog);
        simulation.getElements().add(playMove);
        simulation.getElements().add(execMon);
        simulation.getElements().add(manager);
        simulation.getElements().add(animator);
        if(speech != null)
        	simulation.getElements().add(speech);
        if(migration != null)
        	simulation.getElements().add(migration);
        simulation.update();
        
        //Only setting the ChessView and buttons now 
        //because they need the user element
        mView = new ChessView(this,user, board);
        mainView.addView(mView);
        ((Button) findViewById(R.id.NewGameButton)).setOnClickListener(new NewGameListener(user));
        ((Button) findViewById(R.id.ForfeitButton)).setOnClickListener(new ForfeitGameListener(user));
        new UndoListener((Button) findViewById(R.id.UndoButton), board);
        
        //TODO Remodel this whole block
        wmanage = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        goView = getLayoutInflater().inflate(R.layout.gameover_overlay, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        wmanage.addView(goView, lp);
        
        board.getEventHandlers().add(new Started());
        board.getEventHandlers().add(new Ended());
        agent.greet();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	timer.cancel();
    	timer = null;
    	updateSimulation.cancel();
    	updateSimulation = null;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	timer = new Timer("ION Update", true);
    	updateSimulation = new UpdateSimulation(simulation);
    	timer.scheduleAtFixedRate(updateSimulation, 0, 200);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	System.out.println("DESTROYING");
    	wmanage.removeView(goView);
    	migration.onDestroy();
    	
    	try {
			agent.saveMemory(openFileOutput("memory.xml", MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
		case 1:
			showDialog(MIGRATE_DIALOG);
			return true;

		case 2:
			showDialog(AGENT_DIALOG);
			return true;
			
		case 3:
			agent.kill();
			Animation anim = AnimationUtils.makeOutAnimation(this, true);
			anim.setFillAfter(true);
			findViewById(R.id.iCat).startAnimation(anim);
			return true;
			
		case 4:
			agent.clearMemory();
			return true;
			
		default:
			break;
		}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    public View makeView() {
        TextView t = new TextView(this);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(18);
        return t;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Builder builder;
    	
    	switch (id) {
		case MIGRATE_DIALOG:
			builder = new Builder(this);
			builder.setTitle("Migrate To...");
			
			//TODO change list creation to onPrepareDialog()
			Set<String> deviceNames = migration.getDeviceList().keySet();
			migrationItems = new CharSequence[deviceNames.size()];
			migrationItems = deviceNames.toArray(migrationItems);
			builder.setItems(migrationItems, this);
			return builder.create();
			
		case AGENT_DIALOG:
			return agentLoader.createDialog();
			
		case PIECE_DIALOG:
			builder = new Builder(this);
			builder.setTitle("Select Piece");
			builder.setItems(pieceList, user);
			return builder.create();
			
		default:
			break;
		}
    	return super.onCreateDialog(id);
    }
    
    public void onClick(DialogInterface dialog, int which){
    	agent.schedule(new Migrate(migrationItems[which].toString()));
    }
    
    private class Ended extends EventHandler {
    	public Ended() {
    		super(GameOver.class);
		}
    	
    	@Override
    	public void invoke(IEvent evt) {
    		handler.post(new Runnable(){
    			@Override
    			public void run() {
    				goView.setVisibility(View.VISIBLE);
    			}
    		});
    	}
    }
    
    private class Started extends EventHandler {
    	public Started() {
    		super(GameStarted.class);
		}
    	
    	@Override
    	public void invoke(IEvent evt) {
    		handler.post(new Runnable(){
    			@Override
    			public void run() {
    				goView.setVisibility(View.INVISIBLE);
    			}
    		});
    	}
    }
    
    private class NewGameListener implements OnClickListener{

    	private ChessUser user;
    	
    	public NewGameListener(ChessUser user) {
    		this.user = user;
		}
    	
		@Override
		public void onClick(View v) {
			user.proposeNewGame();
		}
    }
    
    private class ForfeitGameListener implements OnClickListener{

    	private ChessUser user;
    	
    	public ForfeitGameListener(ChessUser user) {
    		this.user = user;
		}
    	
		@Override
		public void onClick(View v) {
			user.forfeitGame();
		}
    }
    
    private class UndoListener implements OnClickListener{

    	private ChessBoard board;
    	private Button button;
    	
    	public UndoListener(Button button, ChessBoard board) {
    		this.board = board;
    		board.getEventHandlers().add(new UndoAvailableListener());
    		board.getEventHandlers().add(new UndoUnavailableListener());
    		
    		this.button = button;
    		button.setOnClickListener(this);
		}
    	
		@Override
		public void onClick(View v) {
			board.schedule(new UserUndo());
		}
		
		private class UndoAvailableListener extends EventHandler{
			public UndoAvailableListener() {
				super(UndoAvailable.class);
			}
			@Override
			public void invoke(IEvent evt) {
				handler.post(new Runnable(){
								public void run(){
									button.setEnabled(true);
								}
							});
			}
		}
		
		private class UndoUnavailableListener extends EventHandler{
			public UndoUnavailableListener() {
				super(UndoUnavailable.class);
			}
			@Override
			public void invoke(IEvent evt) {
				handler.post(new Runnable(){
					public void run(){
						button.setEnabled(false);
					}
				});
			}
		}
    }
    

    private class AgentLoader implements DialogInterface.OnClickListener, AnimationListener {
    	
    	private class Configuration {
    		public int voice;
    		public int sentences;
    		public int animations;
    		public MoodModel mood;
    		public boolean memory;
    	}
    	
    	private HashMap<String, Configuration> agents;
    	private String names[];
    	private Animation out;
    	private Animation in;
    	private View agentView;
    	
    	public AgentLoader(){
    		agents = new HashMap<String, Configuration>();
    		out = AnimationUtils.makeOutAnimation(MyFriendChessGame.this, true);
    		out.setFillAfter(true);
    		in = AnimationUtils.makeInAnimation(MyFriendChessGame.this, false);
    		in.setFillAfter(true);
    		agentView = findViewById(R.id.iCat);
    	}

    	public void importConfig(XmlPullParser parser) throws IOException, XmlPullParserException{
    		Document doc = new Document();
    		doc.parse(parser);
    		
    		Element root = doc.getRootElement();
    		
    		for (int i = 0; i < root.getChildCount() ; i++) {
				Element agent = root.getElement(i);
				
				if(agent != null){
					String value;
					String name = agent.getAttributeValue("", "name");
					Configuration config = new Configuration();
					
					value = agent.getAttributeValue("", "animations");
					config.animations = getResources().getIdentifier(value, "raw", getPackageName());
					
					value = agent.getAttributeValue("", "sentences");
					config.sentences = getResources().getIdentifier(value, "raw", getPackageName());
					
					value = agent.getAttributeValue("", "voice");
					config.voice = getResources().getIdentifier(value, "raw", getPackageName());
					
					value = agent.getAttributeValue("", "memory");
					config.memory = value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true");
					
					value = agent.getAttributeValue("", "mood");
					if(value.equalsIgnoreCase("happy")){
						config.mood = MoodModel.Happy;
					} else if(value.equalsIgnoreCase("neutral")){
						config.mood = MoodModel.Neutral;
					} else if(value.equalsIgnoreCase("sad")){
						config.mood = MoodModel.Sad;
					} else {
						config.mood = MoodModel.Dynamic;
					}
					
					agents.put(name, config);
				}
			}
    	}
    	
    	public AlertDialog createDialog(){
    		Builder builder = new Builder(MyFriendChessGame.this);
    		builder.setTitle("Agents");

    		names = new String[agents.keySet().size()];
    		names = agents.keySet().toArray(names);
    		builder.setItems(names, this);
    		
    		return builder.create();
    	}
    	
    	public void loadConfig(String name){
    		Configuration config = agents.get(name);
    		KXmlParser parser = new KXmlParser();

    		agentView.startAnimation(out);
    		
    		execMon.clearSentences();
    		animator.clearAnimations();
    		speech.clearSoundMappings();
    		
    		agent.setMemoryState(config.memory);
    		agent.changeMoodModel(config.mood);
    		
    		try {
				parser.setInput(getResources().openRawResource(config.sentences), "UTF-8");
				execMon.importSentences(parser);
				
				parser.setInput(getResources().openRawResource(config.animations), "UTF-8");
				animator.importAnimations(parser);
				
				parser.setInput(getResources().openRawResource(config.voice), "UTF-8");
				speech.importSoundMappings(parser);
				
				animator.resetFace();
				
				if(out.hasEnded()){
					agentView.startAnimation(in);
				} else {
					out.setAnimationListener(this);
				}
				
			} catch (NotFoundException e) {
				System.err.println("Agent configuration change incomplete");
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				System.err.println("Agent configuration change incomplete");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Agent configuration change incomplete");
				e.printStackTrace();
			}
			
    	}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			loadConfig(names[which]);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			animation.setAnimationListener(null);
			agentView.startAnimation(in);
			System.out.println("Animation on Agent Change has finished.");
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
    }
}