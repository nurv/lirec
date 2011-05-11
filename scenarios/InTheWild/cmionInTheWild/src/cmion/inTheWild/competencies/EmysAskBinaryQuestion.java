package cmion.inTheWild.competencies;

import java.util.ArrayList;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;

/** a competency that when invoked will cause Emys to ask the user a binary question
 *  and wait for the answer to the question and raise a remote action event representing
 *  the user's answer
 *  this competency only writes to the blackboard and requires
 * 	an emys connector to run and transmit the request and receive the answer */
public class EmysAskBinaryQuestion extends Competency {

	public static final String CHOICE1 = "choice1";
	public static final String CHOICE2 = "choice2";
	public static final String QUESTION = "question";	
	public static final String ANSWER = "answer";	
	public static final String PERSON = "person";	
	public static final String CONTAINER_NAME = "EmysBinaryQuestion";

	public EmysAskBinaryQuestion(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "EmysAskBinaryQuestion";
		this.competencyType = "EmysAskBinaryQuestion";
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
		if (!parameters.containsKey(CHOICE2)) return false;
		if (!parameters.containsKey(PERSON)) return false;		
		if (!parameters.containsKey(QUESTION)) return false;	

		// post question to the blackboard
		// properties of the question are the question itself and choice 1 and choice 2
		HashMap<String, Object> properties = new HashMap<String,Object>();
		properties.put(QUESTION, parameters.get(QUESTION));
		properties.put(CHOICE1, parameters.get(CHOICE1));
		properties.put(CHOICE2, parameters.get(CHOICE2));		
		architecture.getBlackBoard().requestAddSubContainer(CONTAINER_NAME, CONTAINER_NAME, properties);
		
		// wait until the user has answered
		String answer = null;
		while (answer == null)
		{
			try {
				Thread.sleep(500);
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
			}
		}
		
		// now we have an answer, so first delete the container from the blackboard
		architecture.getBlackBoard().requestRemoveSubContainer(CONTAINER_NAME);
		
		// and finally raise the remote action
		ArrayList<String> actionParameters = new ArrayList<String>();
		actionParameters.add(parameters.get(QUESTION));
		actionParameters.add(answer);
		// create an action called answer caused by the person specified with the additional
		// parameters of the question and the answer 
		MindAction ma = new MindAction(parameters.get(PERSON),"answer",actionParameters);
		this.raise(new EventRemoteAction(ma));	
		
		return true;
	}	
	
}
