package com.example.doan;

import android.content.Context;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

public class FCMSender {
    private static final String TAG = "FCMSender";

    public static void sendNotification(Context context, String targetToken, String title, String body) {
        new Thread(() -> {
            try {
                // Load service_account.json từ assets
                InputStream inputStream = context.getAssets().open("service_account.json");
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(inputStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));

                googleCredentials.refreshIfExpired();
                String accessToken = googleCredentials.getAccessToken().getTokenValue();

                // Đọc project ID từ file
                InputStream raw = context.getAssets().open("service_account.json");
                Scanner scanner = new Scanner(raw).useDelimiter("\\A");
                String json = scanner.hasNext() ? scanner.next() : "";
                JSONObject jsonObject = new JSONObject(json);
                String projectId = jsonObject.getString("project_id");

                // URL gửi FCM v1
                String fcmUrl = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

                // Tạo JSON body
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);

                JSONObject messageObj = new JSONObject();
                messageObj.put("token", targetToken);
                messageObj.put("notification", notification);

                JSONObject requestBody = new JSONObject();
                requestBody.put("message", messageObj);

                // Gửi bằng Volley (phải chạy trên UI thread)
                RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, fcmUrl, requestBody,
                        response -> Log.d(TAG, "FCM sent successfully: " + response),
                        error -> Log.e(TAG, "FCM send failed: " + error.toString())) {

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + accessToken);
                        headers.put("Content-Type", "application/json; charset=UTF-8");
                        return headers;
                    }
                };

                // Phải đưa add() về main thread
                android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                handler.post(() -> queue.add(request));

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage(), e);
            }
        }).start();
    }
}
