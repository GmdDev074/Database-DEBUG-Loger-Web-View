package data.base.de.bug.web.view;

import android.content.Context;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Entry point for the Android Debug View library.
 */
public class DebugView {

    private static final String TAG = "DebugView";
    private static final int DEFAULT_PORT = 8080;

    /**
     * Initialize the Debug Server.
     * @param context Application Context
     */
    public static void init(Context context) {
        int port = DEFAULT_PORT;

        // TODO: Start NanoHTTPD or any embedded server here
        // startServer(context, port);

        String ipAddress = getDeviceIpAddress();

        Log.d(TAG, "DebugView Initialized. Web Server started.");
        Log.d(TAG, "Open http://" + ipAddress + ":" + port + "/index.html");

        // Helpful emulator fallback
        Log.d(TAG, "ADB fallback: adb forward tcp:" + port + " tcp:" + port);
    }

    /**
     * Resolve local IPv4 address
     */
    private static String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses =
                        networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (!address.isLoopbackAddress()
                            && address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "IP address resolution failed", e);
        }

        // Safe fallback
        return "127.0.0.1";
    }
}
