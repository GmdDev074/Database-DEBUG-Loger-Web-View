package com.debug.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
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
        } else if (uri.startsWith("/addData")) {
            Map<String, String> params = session.getParms();
            return handleAddData(params.get("dbName"), params.get("key"), params.get("value"));
        } else if (uri.startsWith("/updateData")) {
            Map<String, String> params = session.getParms();
            return handleUpdateData(params.get("dbName"), params.get("key"), params.get("value"));
        } else if (uri.startsWith("/deleteData")) {
            Map<String, String> params = session.getParms();
            return handleDeleteData(params.get("dbName"), params.get("key"));
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

            // Get SharedPreferences directory
            File prefsDir = new File(context.getApplicationInfo().dataDir, "shared_prefs");

            if (prefsDir.exists() && prefsDir.isDirectory()) {
                File[] prefFiles = prefsDir.listFiles();
                if (prefFiles != null) {
                    for (File file : prefFiles) {
                        if (file.getName().endsWith(".xml")) {
                            // Remove .xml extension to get the preference name
                            String prefName = file.getName().replace(".xml", "");

                            JSONObject dbEntry = new JSONObject();
                            dbEntry.put("name", prefName);
                            dbEntry.put("type", "SHARED_PREFS");
                            rows.put(dbEntry);

                            Log.d(TAG, "Found SharedPreferences: " + prefName);
                        }
                    }
                }
            }

            // If no prefs found, add default
            if (rows.length() == 0) {
                JSONObject defaultPrefs = new JSONObject();
                defaultPrefs.put("name", context.getPackageName() + "_preferences");
                defaultPrefs.put("type", "SHARED_PREFS");
                rows.put(defaultPrefs);
            }

            response.put("rows", rows);
            Log.d(TAG, "getDbList response: " + response.toString());
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
            Log.d(TAG, "getTableList response for " + dbName + ": " + response.toString());
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in getTableList", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGetAllData(String dbName, String tableName) {
        try {
            Log.d(TAG, "getAllData called with dbName=" + dbName + ", tableName=" + tableName);

            // Get SharedPreferences using the exact name passed
            SharedPreferences prefs = context.getSharedPreferences(
                    dbName != null ? dbName : context.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);

            Map<String, ?> allPrefs = prefs.getAll();
            JSONArray dataArray = new JSONArray();

            Log.d(TAG, "Found " + allPrefs.size() + " entries in SharedPreferences: " + dbName);

            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                JSONObject item = new JSONObject();
                item.put("key", entry.getKey());
                item.put("value", entry.getValue() != null ? entry.getValue().toString() : "null");
                dataArray.put(item);

                Log.d(TAG, "  - " + entry.getKey() + " = " + entry.getValue());
            }

            Log.d(TAG, "getAllData response: " + dataArray.toString());
            return newFixedLengthResponse(Response.Status.OK, "application/json", dataArray.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in getAllData", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleAddData(String dbName, String key, String value) {
        try {
            Log.d(TAG, "addData called with dbName=" + dbName + ", key=" + key + ", value=" + value);

            SharedPreferences prefs = context.getSharedPreferences(
                    dbName != null ? dbName : context.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);

            prefs.edit().putString(key, value).apply();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Added successfully");

            Log.d(TAG, "addData response: " + response.toString());
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in addData", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleUpdateData(String dbName, String key, String value) {
        try {
            Log.d(TAG, "updateData called with dbName=" + dbName + ", key=" + key + ", value=" + value);

            SharedPreferences prefs = context.getSharedPreferences(
                    dbName != null ? dbName : context.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);

            prefs.edit().putString(key, value).apply();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Updated successfully");

            Log.d(TAG, "updateData response: " + response.toString());
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in updateData", e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleDeleteData(String dbName, String key) {
        try {
            Log.d(TAG, "deleteData called with dbName=" + dbName + ", key=" + key);

            SharedPreferences prefs = context.getSharedPreferences(
                    dbName != null ? dbName : context.getPackageName() + "_preferences",
                    Context.MODE_PRIVATE);

            prefs.edit().remove(key).apply();

            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("message", "Deleted successfully");

            Log.d(TAG, "deleteData response: " + response.toString());
            return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteData", e);
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
