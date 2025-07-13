package com.example.doan;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

public class BlockUtil {

    public static void blockUser(Context context, String currentUserId, String targetUserId) {
        if (currentUserId == null || targetUserId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("blocked", true);
        data.put("blockerId", currentUserId);

        db.collection("blocked_users")
                .document(currentUserId + "_" + targetUserId)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Đã chặn người dùng", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->{
                        Log.e("BlockUtil", "Lỗi khi chặn người dùng", e);
                        Toast.makeText(context, "Lỗi khi chặn người dùng", Toast.LENGTH_SHORT).show();
                });
    }


    // Unblock user nếu muốn dùng sau này
    public static void unblockUser(Context context, String currentUserId, String targetUserId) {
        if (currentUserId == null || targetUserId == null) return;

        FirebaseFirestore.getInstance()
                .collection("blocked_users")
                .document(currentUserId + "_" + targetUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã bỏ chặn người dùng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi bỏ chặn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
