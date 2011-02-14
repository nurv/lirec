package FAtiMA.emotivector;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;

public class EmotivectorComponent implements IComponent, IAppraisalDerivationComponent, IAffectDerivationComponent {

	protected static enum  AppraisalVariables{SENSED_VALUE,EXPECTED_VALUE};
  
	private static final float ERROR_TRESHOLD = 3;
	private static final float NEGLIGIBLE_TRESHOLD = 1;
	private static final float ALPHA = 0.3f;//moving averages factor
	private static final float MEDIUM_MOVE_VALUE = 0;
	private static final float GOOD_MOVE_VALUE = -10;
	private static final float BAD_MOVE_VALUE = 10;
	private static final short EMOTIVECTOR_WEIGHT = 10;
	
	
	private float expectedValue;
	
	private EmotivectorSensationType determineSensationType(float ev, float mismatch){
		//expected punishement
		if(ev <= -NEGLIGIBLE_TRESHOLD){
			if(mismatch >= ERROR_TRESHOLD){
				//weaker punishment
				return EmotivectorSensationType.WEAKER_PUNISHMENT;
			}
			if(-ERROR_TRESHOLD < mismatch && mismatch < ERROR_TRESHOLD){
				//expected punishement
				return EmotivectorSensationType.EXPECTED_PUNISHMENT; 	
			}				
			if(mismatch <= -ERROR_TRESHOLD){
				//stronger punishement
				return EmotivectorSensationType.STRONGER_PUNISHMENT;		
			}
		}
		
		//expected negligible	
		if (-NEGLIGIBLE_TRESHOLD < ev && ev < NEGLIGIBLE_TRESHOLD){
			if(mismatch >= ERROR_TRESHOLD){
				//unexpected reward
				return EmotivectorSensationType.UNEXPECTED_REWARD;
			}
			if(-ERROR_TRESHOLD < mismatch && mismatch < ERROR_TRESHOLD){
				return EmotivectorSensationType.NEUTRAL;
			}				
			if(mismatch <= -ERROR_TRESHOLD){
				//unexpected punishement
				return EmotivectorSensationType.UNEXPECTED_PUNISHMENT;
			}
		}
		
		//expected reward
		if (ev >= NEGLIGIBLE_TRESHOLD){
			
			if(mismatch >= ERROR_TRESHOLD){
				//stronger reward
				return EmotivectorSensationType.STRONGER_REWARD;
			}
			if(-ERROR_TRESHOLD < mismatch && mismatch < ERROR_TRESHOLD){
				//expected reward
				return EmotivectorSensationType.EXPECTED_REWARD;
			}				
			if(mismatch <= -ERROR_TRESHOLD){
				return EmotivectorSensationType.WEAKER_REWARD;
				//weaker reward
			}			
		}
		return EmotivectorSensationType.NEUTRAL;
	}

	private boolean isMove(Event e) {
		String actionName = e.GetAction();
		
		if(actionName.equalsIgnoreCase("medium-move") || 
			actionName.equalsIgnoreCase("good-move") ||
			  actionName.equalsIgnoreCase("bad-move")){
			return true;
		}
		return false;
	}


	@Override
	public ArrayList<BaseEmotion> affectDerivation(AgentModel am,
			AppraisalFrame af) {
	
		ArrayList<BaseEmotion> result = new ArrayList<BaseEmotion>();
		
		float sv, ev, mismatch;
		BaseEmotion bE = null;
		
			
		if(af.containsAppraisalVariable(AppraisalVariables.SENSED_VALUE.name()) && af.containsAppraisalVariable(AppraisalVariables.EXPECTED_VALUE.name())){
		
			ArrayList<String> appraisalVariables = new ArrayList<String>();
			appraisalVariables.add(AppraisalVariables.SENSED_VALUE.name());
			appraisalVariables.add(AppraisalVariables.EXPECTED_VALUE.name());
			
			sv = af.getAppraisalVariable(AppraisalVariables.SENSED_VALUE.name());
			ev = af.getAppraisalVariable(AppraisalVariables.EXPECTED_VALUE.name());
		
			mismatch = sv - ev;
		
			EmotivectorSensationType emotionType = determineSensationType(ev, mismatch);
			
			//add the emotion to the emotional state of the agent
			if(emotionType!=EmotivectorSensationType.NEUTRAL){
				result.add(new EmotivectorSensation(emotionType, af.getEvent()));
			}
		}
	
		return result;
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame af) {
		float sensedValue = 0;
		
		//determines if the event perceived is a move
		if(isMove(e)){
			if(e.GetAction().equalsIgnoreCase("medium-move")){
				sensedValue = MEDIUM_MOVE_VALUE;
			}
			if(e.GetAction().equalsIgnoreCase("bad-move")){
				sensedValue = BAD_MOVE_VALUE;
			}
			if(e.GetAction().equalsIgnoreCase("good-move")){
				sensedValue = GOOD_MOVE_VALUE;
			}
			
			af.SetAppraisalVariable(this.name(),EMOTIVECTOR_WEIGHT, AppraisalVariables.SENSED_VALUE.name(), sensedValue);
			af.SetAppraisalVariable(this.name(),EMOTIVECTOR_WEIGHT, AppraisalVariables.EXPECTED_VALUE.name(), this.expectedValue);

			//update the expectedValue using a moving average:
			this.expectedValue = ALPHA * sensedValue + (1 - ALPHA) * this.expectedValue;
		}	
	}
	
	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {};
		return dependencies;
	}

	
	@Override
	public void initialize(AgentModel am) {
		expectedValue = 0;	
	}

	@Override
	public void inverseAffectDerivation(AgentModel am, BaseEmotion em,
			AppraisalFrame af) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String name() {
		return "EmotivectorComponent";
	}
	
	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
		// TODO Auto-generated method stub
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub		
	}


	@Override
	public void update(AgentModel am, Event e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(AgentModel am, long time) {
		// TODO Auto-generated method stub
	}

}
