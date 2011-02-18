package FAtiMA.empathy;

import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;


public class EmpathicAppraisal{
	
	private String _empathicTarget;
	private AppraisalFrame _appraisalFrame;
	private BaseEmotion _elicitedEmotion;
	private long _startTime;
	
	public EmpathicAppraisal(String empathicTarget, AppraisalFrame aF, BaseEmotion elicitedEmotion, long startTime){
		this._empathicTarget = empathicTarget;
		this._appraisalFrame = aF;
		this._elicitedEmotion = elicitedEmotion;
		this._startTime = startTime;	}	
	
	public AppraisalFrame getAppraisalFrame(){
		return _appraisalFrame;
	}
	public String getEmpathicTarget(){
		return _empathicTarget;
	}
	public BaseEmotion getElicitedEmotion(){
		return _elicitedEmotion;
	}
	public long getStartTime(){
		return _startTime;
	}
}
