package cmion.inTheWild.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class ProcessTextInput extends Competency
{
	public ProcessTextInput(IArchitecture architecture) 
	{
		super(architecture);
		this.competencyName = "ProcessTextInput"; 
		this.competencyType = "ProcessTextInput";
	}

	@Override
	public boolean runsInBackground() 
	{
		return true;
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
		return true;
	}
	
	
}
