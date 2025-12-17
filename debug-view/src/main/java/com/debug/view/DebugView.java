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
    private static final int DEFAULT_PORT = 8080;

    private static DebugHttpServer server;

    /**
     * Initialize the Debug Server.
     *
     * @param context Application Context
     */
    public static void init(Context context) {
        int port = DEFAULT_PORT;

        try {
            if (server != null) {
                server.stop();
            }
            server = new DebugHttpServer(context, port);
            server.start();
            Log.d(TAG, "DebugView Initialized. Web Server started.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start DebugHttpServer", e);
        }

        String ipAddress = getDeviceIpAddress();

        Log.d(TAG, "Open http://" + ipAddress + ":" + port + "/index.html");

        // Emulator / USB fallback
        Log.d(TAG, "ADB fallback: adb forward tcp:" + port + " tcp:" + port);
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
