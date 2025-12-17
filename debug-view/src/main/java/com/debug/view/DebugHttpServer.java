package com.debug.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

/**
 * Embedded HTTP Server for Debug View with API endpoints.
 */
public class DebugHttpServer extends NanoHTTPD {

    private static final String TAG = "DebugHttpServer";
    private final Context context;

    public DebugHttpServer(Context context, int port) {
        super(port);
        this.context = context.getApplicationContext();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        // API Endpoints
        if (uri.startsWith("/getDbList")) {
            return handleGetDbList();
        } else if (uri.startsWith("/getTableList")) {
            Map<String, String> params = session.getParms();
            return handleGetTableList(params.get("dbName"));
        } else if (uri.startsWith("/getAllData")) {
            Map<String, String> params = session.getParms();
            return handleGetAllData(params.get("dbName"), params.get("tableName"));
        }

        // Static file serving
        if (uri.equals("/") || uri.isEmpty()) {
            uri = "/index.html";
        }

        // Remove leading slash for asset opening
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        try {
            InputStream mimeTypeStream = context.getAssets().open(uri);
            String mimeType = determineMimeType(uri);
            return newChunkedResponse(Response.Status.OK, mimeType, mimeTypeStream);
        } catch (IOException e) {
            Log.e(TAG, "File not found: " + uri, e);
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "File not found");
        }
    }

    private Response handleGetDbList() {
        try {
            JSONObject response = new JSONObject();
            JSONArray rows = new JSONArray();

            // Get all SharedPreferences files
            String[] prefFiles = context.getSharedPreferences("temp", Context.MODE_PRIVATE)
                    .getAll().keySet().toArray(new String[0]);

            // Add default SharedPreferences
            JSONObject defaultPrefs = new JSONObject();
            defaultPrefs.put("name", context.getPackageName() + "_preferences");
            defaultPrefs.put("type", "SHARED_PREFS");
            rows.put(defaultPrefs);

            response.put("rows", rows);
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in getDbList", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGetTableList(String dbName) {
        try {
            JSONObject response = new JSONObject();
            JSONArray rows = new JSONArray();

            // For SharedPreferences, we only have one "table" (the prefs file itself)
            rows.put(dbName);

            response.put("rows", rows);
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in getTableList", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGetAllData(String dbName, String tableName) {
        try {
            // Get SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(
                    dbName != null ? dbName : context.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);

            Map<String, ?> allPrefs = prefs.getAll();
            JSONArray dataArray = new JSONArray();

            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                JSONObject item = new JSONObject();
                item.put("key", entry.getKey());
                item.put("value", entry.getValue() != null ? entry.getValue().toString() : "null");
                dataArray.put(item);
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", dataArray.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in getAllData", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String determineMimeType(String uri) {
        if (uri.endsWith(".html"))
            return "text/html";
        if (uri.endsWith(".css"))
            return "text/css";
        if (uri.endsWith(".js"))
            return "application/javascript";
        if (uri.endsWith(".png"))
            return "image/png";
        if (uri.endsWith(".jpg") || uri.endsWith(".jpeg"))
            return "image/jpeg";
        return NanoHTTPD.MIME_PLAINTEXT;
    }
}
