package FAtiMA.Core.sensorEffector;

public class DefaultProcessActionStrategy  implements IProcessActionStrategy {

	@Override
	public RemoteAction ProcessActionToWorld(RemoteAction action) {
		return action;
	}

	@Override
	public RemoteAction ProcessActionFromWorld(RemoteAction action) {
		return action;
	}

}
