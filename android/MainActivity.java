
package app.lovable.luckyvpnmaster;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Plugin;
import java.util.ArrayList;
import app.lovable.luckyvpnmaster.utils.LanguageManager;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize language before calling super
        LanguageManager.initializeLanguage(this);
        
        super.onCreate(savedInstanceState);
        
        // Initialize Capacitor plugins
        this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
            // Add any additional plugins here
        }});
    }
}
