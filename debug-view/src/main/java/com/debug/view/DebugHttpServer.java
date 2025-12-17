package com.debug.view;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

/**
 * Embedded HTTP Server for Debug View.
 */
public class DebugHttpServer extends NanoHTTPD {

    private static final String TAG = "DebugHttpServer";
    private final Context context;

    public DebugHttpServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
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
