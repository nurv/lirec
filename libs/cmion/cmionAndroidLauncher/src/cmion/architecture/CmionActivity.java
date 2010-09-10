package cmion.architecture;

import android.app.Activity;
import android.os.Bundle;

public class CmionActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        AndroidArchitecture architecture;
        architecture = AndroidArchitecture.startup("architectureconfiguration_migrationsend", this, getApplication());
    }
}