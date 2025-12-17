package com.debug.view;

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
    private static final int PORT = 8080;
    private static DebugHttpServer server;

    /**
     * Initialize the Debug Server.
     *
     * @param context Application Context
     */
    public static void init(Context context) {
        try {
            if (server == null || !server.isAlive()) {
                server = new DebugHttpServer(context, PORT);
                server.start();
            }
            String ip = getDeviceIpAddress();
            Log.d(TAG, "Debug Web Server started on port " + PORT);
            Log.d(TAG, "Open http://" + ip + ":" + PORT + "/index.html");
            Log.d(TAG, "If not enabling http:// prefix, it may fail. Ensure your browser uses http.");
            Log.d(TAG, "ADB fallback: adb forward tcp:" + PORT + " tcp:" + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start DebugView server", e);
            server = null; // Reset to allow retry
        }
    }

    /**
     * Resolve the local IPv4 address of the device.
     */
    private static String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (!address.isLoopbackAddress()
                            && address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to resolve device IP address", e);
        }

        // Safe fallback
        return "127.0.0.1";
    }
}
