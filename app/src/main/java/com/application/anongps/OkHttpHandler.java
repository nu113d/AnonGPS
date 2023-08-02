package com.application.anongps;

import android.os.StrictMode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHandler {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .build();
    private String jsonResponse;
    public OkHttpHandler() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public String getRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
        } catch (IOException e) {
            //note that Firebase returns "null" when data not found
            return "null";
        }
        response.close();
        return  jsonResponse;
    }

    public void patchRequest(String url, String jsonBody) {
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .patch(requestBody)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
            response.close();
        } catch (IOException e) {
            return;
        }
    }

    public void deleteRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
            response.close();
        } catch (IOException e) {
            return;
        }
    }
}
