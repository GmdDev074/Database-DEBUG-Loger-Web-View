package data.base.de.bug.web.view;

import android.content.Context;
import android.util.Log;

/**
 * Entry point for the Android Debug View library.
 */
public class DebugView {
    private static final String TAG = "DebugView";

    /**
     * Initialize the Debug Server.
     * @param context Application Context
     */
    public static void init(Context context) {
        // TODO: Implement the Web Server starting logic here using NanoHTTPD or similar.
        // It should serve the files from "assets/" folder.
        
        Log.d(TAG, "DebugView Initialized. Web Server should start serving assets...");
        Log.d(TAG, "Open http://[DEVICE_IP]:8080/index.html");
    }
}
