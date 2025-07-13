package com.example.doan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doan.ChatAdapter;
import com.example.doan.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private String receiverId;
    private boolean isBlocked = false;

    private EditText edtMessage;
    private ImageButton btnSend, btnImage;
    private FirebaseUser currentUser;

    private View inputLayout;
    private TextView txtBlockedNotice;

    private static final int IMAGE_PICK_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_chat);
        edtMessage = findViewById(R.id.edt_message);
        btnSend = findViewById(R.id.btn_send);
        btnImage = findViewById(R.id.btn_image);

        inputLayout = findViewById(R.id.message_input_layout);
        txtBlockedNotice = findViewById(R.id.txt_blocked_notice);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        receiverId = getIntent().getStringExtra("receiverId");
        if (receiverId == null|| receiverId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, messageList, currentUser.getUid());
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage(edtMessage.getText().toString()));
        btnImage.setOnClickListener(v -> selectImage());

        loadMessages();
        checkIfBlocked(() -> sendMessage(edtMessage.getText().toString()));
    }

    private void showBlockedUI(String message) {
        txtBlockedNotice.setVisibility(View.VISIBLE);
        txtBlockedNotice.setText(message);
        inputLayout.setVisibility(View.GONE);
    }

    private void showUnblockedUI() {
        txtBlockedNotice.setVisibility(View.GONE);
        inputLayout.setVisibility(View.VISIBLE);
    }

    private void checkIfBlocked(Runnable onAllowed) {
        String currentToReceiver = currentUser.getUid() + "_" + receiverId;
        String receiverToCurrent = receiverId + "_" + currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Kiểm tra cả hai chiều block
        db.collection("blocked_users").document(currentToReceiver).get()
                .addOnSuccessListener(doc1 -> {
                    if (doc1.exists()) {
                        // Bạn đã chặn người kia
                        isBlocked = true;
                        showBlockedUI("Bạn đã chặn người dùng này");
                    } else {
                        db.collection("blocked_users").document(receiverToCurrent).get()
                                .addOnSuccessListener(doc2 -> {
                                    if (doc2.exists()) {
                                        // Bạn bị người kia chặn
                                        isBlocked = true;
                                        showBlockedUI("Bạn đã bị chặn bởi người dùng này");
                                    } else {
                                        isBlocked = false;
                                        showUnblockedUI();
                                        onAllowed.run(); // Cho phép gửi nếu không ai bị chặn
                                    }
                                });
                    }
                });
    }

    private void loadMessages() {
        FirebaseFirestore.getInstance()
                .collection("messages")
                .whereEqualTo("chatId", getChatId(currentUser.getUid(), receiverId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage(String text) {
        if (text.isEmpty() || isBlocked) return;

        String chatId = getChatId(currentUser.getUid(), receiverId);

        Message msg = new Message(
                UUID.randomUUID().toString(),
                currentUser.getUid(),
                receiverId,
                text,
                null,
                chatId,
                new Date(),
                false
        );


        FirebaseFirestore.getInstance()
                .collection("messages")
                .document(msg.getId())
                .set(msg);

        updateChatPreview(currentUser.getUid(), receiverId, text, new Date());
        updateChatPreview(receiverId, currentUser.getUid(), text, new Date());


        edtMessage.setText("");
    }

    private void updateChatPreview(String userId, String otherUserId, String lastMessage, Date timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Log.e("ChatPreview", "Không tìm thấy userId: " + otherUserId);
                        return;
                    }

                    String name = snapshot.getString("name");
                    String avatarUrl = snapshot.getString("avatarUrl");

                    if (name == null) name = "(Không tên)"; // hoặc currentUser.getDisplayName()

                    ChatPreview preview = new ChatPreview(
                            otherUserId,
                            name,
                            avatarUrl != null ? avatarUrl : "",
                            lastMessage,
                            timestamp
                    );

                    db.collection("chatPreviews")
                            .document(userId)
                            .collection("users")
                            .document(otherUserId)
                            .set(preview)
                            .addOnSuccessListener(unused ->
                                    Log.d("ChatPreview", "Đã lưu chat preview cho " + userId))
                            .addOnFailureListener(e ->
                                    Log.e("ChatPreview", "Lỗi khi lưu: " + e.getMessage()));
                });
    }




    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            sendImageMessage(imageUri.toString());
        }
    }

    private void sendImageMessage(String imageUri) {
        if (isBlocked) return;

        String chatId = getChatId(currentUser.getUid(), receiverId);

        Message msg = new Message(
                UUID.randomUUID().toString(),
                currentUser.getUid(),
                receiverId,
                "",
                imageUri,
                chatId,
                new Date(),
                false
        );

        FirebaseFirestore.getInstance()
                .collection("messages")
                .document(msg.getId())
                .set(msg);
    }

    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}