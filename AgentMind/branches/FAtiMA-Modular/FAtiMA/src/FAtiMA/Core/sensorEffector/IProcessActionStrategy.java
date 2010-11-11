package FAtiMA.Core.sensorEffector;

public interface IProcessActionStrategy {
	
	public RemoteAction ProcessActionToWorld(RemoteAction action);
	
	public RemoteAction ProcessActionFromWorld(RemoteAction action);

	
}
