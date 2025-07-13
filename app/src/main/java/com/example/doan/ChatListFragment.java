package com.example.doan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatPreviewAdapter adapter;
    private List<ChatPreview> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatPreviewAdapter(getContext(), chatList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChatList();

        return view;
    }

    private void loadChatList() {
        db.collection("chatPreviews").document(currentUserId).collection("users")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    chatList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        ChatPreview preview = doc.toObject(ChatPreview.class);
                        chatList.add(preview);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải danh sách chat", Toast.LENGTH_SHORT).show();
                    e.printStackTrace(); // In ra chi tiết lỗi trong Logcat
                    android.util.Log.e("CHAT_LIST_ERROR", "Lỗi khi tải danh sách chat", e);
                });
    }
}
