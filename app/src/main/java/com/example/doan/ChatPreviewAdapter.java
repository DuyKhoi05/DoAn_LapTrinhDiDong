package com.example.doan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatPreview> chatList;

    public ChatPreviewAdapter(Context context, List<ChatPreview> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);
        holder.txtName.setText(chat.getName());
        holder.txtLastMessage.setText(chat.getLastMessage());

        if (chat.getAvatarUrl() != null && !chat.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(chat.getAvatarUrl())) // ✅ load avatar URI đúng
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", chat.getUserId());
            intent.putExtra("receiverName", chat.getName());
            context.startActivity(intent);
        });

        holder.btnBlock.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getUid();
            String targetUserId = chat.getUserId();

            String blockDocId = currentUserId + "_" + targetUserId;

            FirebaseFirestore.getInstance()
                    .collection("blocked_users")
                    .document(blockDocId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            // Đã block → chuyển thành unblock
                            BlockUtil.unblockUser(context, currentUserId, targetUserId);
                            Toast.makeText(context, "Đã bỏ chặn", Toast.LENGTH_SHORT).show();
                            holder.btnBlock.setText("Block");
                        } else {
                            // Chưa block → block
                            BlockUtil.blockUser(context, currentUserId, targetUserId);
                            Toast.makeText(context, "Đã chặn người dùng", Toast.LENGTH_SHORT).show();
                            holder.btnBlock.setText("Unblock");
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName, txtLastMessage;
        Button btnBlock;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            txtName = itemView.findViewById(R.id.txt_name);
            txtLastMessage = itemView.findViewById(R.id.txt_last_message);
            btnBlock = itemView.findViewById(R.id.btn_block);
        }
    }
}
