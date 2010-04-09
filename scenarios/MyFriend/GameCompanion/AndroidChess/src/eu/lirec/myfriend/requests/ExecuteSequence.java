package eu.lirec.myfriend.requests;

import java.util.ArrayList;
import java.util.List;

import ion.Meta.Request;

public class ExecuteSequence extends Request {
	
	private final List<List<Request>> sequence;
	
	public ExecuteSequence() {
		sequence = new ArrayList<List<Request>>();
	}
	
	public void appendStep(List<Request> step){
		this.sequence.add(step);
	}
	
	public void appendStep(Request ... requests){
		ArrayList<Request> step = new ArrayList<Request>();
		
		for (Request request : requests) {
			step.add(request);
		}
		
		this.sequence.add(step);
	}
	
	public List<List<Request>> getSequence(){
		List<List<Request>> duplicateSequence = new ArrayList<List<Request>>();
		
		for (List<Request> step : sequence) {
			List<Request> duplicateList = new ArrayList<Request>(step);
			duplicateSequence.add(duplicateList);
		}
		
		return this.sequence;
	}

}
