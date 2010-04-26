package cmion.level2.competencies;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import android.content.Context;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.AndroidSynchronizer;
import cmion.level2.migration.Synchronizer;

public class AndroidMigration extends Migration {

	public AndroidMigration(IArchitecture architecture, String configFile) {
		super(architecture, configFile);
	}
	
	@Override
	protected InputStream openConfigFile(String configFile) {
		Context context = (Context) architecture.getSystemContext();
		int resourceId = context.getResources().getIdentifier(configFile, "raw", context.getPackageName());
		InputStream inStream = context.getResources().openRawResource(resourceId);
		return inStream;
	}
	
	@Override
	protected Synchronizer getNewSynchronizer(int listenPort) throws IOException, ParserConfigurationException {
		return new AndroidSynchronizer(listenPort, (Context)architecture.getSystemContext());
	}
}
