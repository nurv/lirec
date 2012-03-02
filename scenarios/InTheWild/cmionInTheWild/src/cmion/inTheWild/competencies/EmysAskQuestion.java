package cmion.inTheWild.competencies;

import java.util.ArrayList;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;

/** a competency that when invoked will cause Emys to ask the user a mutiple choice question (up to 4)
 *  and wait for the answer to the question and raise a remote action event representing
 *  the user's answer
 *  this competency only writes to the blackboard and requires
 * 	an emys connector to run and transmit the request and receive the answer */
public class EmysAskQuestion extends Competency {

	public static final String CHOICE1 = "choice1";
	public static final String CHOICE2 = "choice2";
	public static final String CHOICE3 = "choice3";
	public static final String CHOICE4 = "choice4";	
	public static final String CHOICES = "choices";
	public static final String QUESTION = "question";	
	public static final String ANSWER = "answer";	
	public static final String CANCELLED = "cancelled";	
	public static final String PERSON = "person";	
	public static final String CONTAINER_NAME = "EmysMultipleChoiceQuestion";

	public EmysAskQuestion(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "EmysAskQuestion";
		this.competencyType = "EmysAskQuestion";
	}

	@Override
	public boolean runsInBackground() 
	{
		return false;
	}
	
	@Override
	public void initialize() 
	{
		this.available = true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException 
	{
		// check if all required parameters are there
		if (!parameters.containsKey(CHOICE1)) return false;
		if (!parameters.containsKey(PERSON)) return false;		
		if (!parameters.containsKey(QUESTION)) return false;	

		int noChoices; 
		
		// see how many choices there are
		if (!parameters.containsKey(CHOICE2)) noChoices=1;
		else if (!parameters.containsKey(CHOICE3)) noChoices=2;
		else if (!parameters.containsKey(CHOICE4)) noChoices=3;
		else noChoices=4;
		
		String choices[] = new String[noChoices];
		choices[0] = parameters.get(CHOICE1);
		if (noChoices>1) choices[1] = parameters.get(CHOICE2);
		if (noChoices>2) choices[2] = parameters.get(CHOICE3);
		if (noChoices>3) choices[3] = parameters.get(CHOICE4);
		
		
		// post question to the blackboard
		// properties of the question are the question itself and the array containing the choices
		HashMap<String, Object> properties = new HashMap<String,Object>();
		properties.put(QUESTION, parameters.get(QUESTION));
		properties.put(CHOICES, choices);		
		architecture.getBlackBoard().requestAddSubContainer(CONTAINER_NAME, CONTAINER_NAME, properties);
		
		// wait until the user has answered
		String answer = null;
		Boolean cancelled = false;
		while ((answer == null) && !cancelled)
		{
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
			// check if there is an answer posted (to go super safe, we check if the container is even there,
			// it should long be by now
			if (architecture.getBlackBoard().hasSubContainer(CONTAINER_NAME))
			{
				CmionStorageContainer questionContainer = architecture.getBlackBoard().getSubContainer(CONTAINER_NAME);
				if (questionContainer.hasProperty(ANSWER))
				{
					answer = questionContainer.getPropertyValue(ANSWER).toString();
				} 
				else if (questionContainer.hasProperty(CANCELLED))
				{
					cancelled = (Boolean) questionContainer.getPropertyValue(CANCELLED);
				}					
			}

		}
		
		// now we have an answer, so first delete the container from the blackboard
		architecture.getBlackBoard().requestRemoveSubContainer(CONTAINER_NAME);
		
		// return failure if cancelled
		if (cancelled) return false;
		
		// and finally raise the remote action
		ArrayList<String> actionParameters = new ArrayList<String>();
		actionParameters.add(parameters.get(QUESTION));
		actionParameters.add(answer);
		// create an action called answer caused by the person specified with the additional
		// parameters of the question and the answer 
		MindAction ma = new MindAction(parameters.get(PERSON),"answer",actionParameters);
		this.raise(new EventRemoteAction(ma));	

		// wait a little bit as we want to make sure we have a chance to process the answer before the question end signal
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		
		
		return true;
	}	
	
}
