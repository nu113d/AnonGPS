package com.application.anongps;

import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
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

    public String makeGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle successful response
                if (response.isSuccessful()) {
                    jsonResponse = response.body().string();
                    // Process the response data
                }
                response.close();
            }
        });
        return jsonResponse;
    }

    public void makePatchRequest(String url, String jsonBody) {
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .patch(requestBody)
                .build();

//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                // Handle failure
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // Handle successful response
//                if (response.isSuccessful()) {
//                    jsonResponse = response.body().string();
//                    // Process the response data
//                }
//                else{
//                    Log.d("okhtttp", jsonResponse);
//                }
//                response.close();
//            }
//        });
//        Log.d("okhtttp", jsonResponse);
        Response response = null;
        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
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
        } catch (IOException e) {
            return;
        }
    }
}
