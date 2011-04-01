package FAtiMA.culture;

import FAtiMA.Core.sensorEffector.IProcessActionStrategy;
import FAtiMA.Core.sensorEffector.RemoteAction;

public class CultureProcessActionStrategy implements IProcessActionStrategy {
	

	@Override
	public RemoteAction ProcessActionToWorld(RemoteAction action) {
		
		action.setActionType(SymbolTranslator.GetInstance().translateMeaningToSymbol(action.getActionType()));
		return action;
	}

	@Override
	public RemoteAction ProcessActionFromWorld(RemoteAction action) {
		action.setActionType(SymbolTranslator.GetInstance().translateSymbolToMeaning(action.getActionType()));
		return action;
	}

}
