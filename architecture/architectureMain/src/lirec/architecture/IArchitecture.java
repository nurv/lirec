package lirec.architecture;

import lirec.level2.CompetencyExecution;
import lirec.level2.CompetencyLibrary;
import lirec.level3.CompetencyManager;
import lirec.storage.LirecStorageContainer;
import lirec.storage.WorldModel;

public interface IArchitecture {

	public WorldModel getWorldModel();
	public LirecStorageContainer getBlackBoard();
	public CompetencyExecution getCompetencyExecution();
	public CompetencyManager getCompetencyManager();
	public CompetencyLibrary getCompetencyLibrary();
	
}
