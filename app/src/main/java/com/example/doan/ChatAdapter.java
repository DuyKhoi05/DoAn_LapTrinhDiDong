package com.example.doan;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_IMAGE = 1;

    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public ChatAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }



    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        boolean isCurrentUser = msg.getSenderId().equals(currentUserId);
        Log.d("VIEW_TYPE_DEBUG", "currentUserId = " + currentUserId);
        Log.d("VIEW_TYPE_DEBUG", "msg.senderId = " + msg.getSenderId());
        Log.d("VIEW_TYPE_DEBUG", "isCurrentUser = " + isCurrentUser);
        if (msg.getImageUri() != null && !msg.getImageUri().isEmpty()) {
            return isCurrentUser ? 3 : 2; // 3: image right, 2: image left
        } else {
            return isCurrentUser ? 1 : 0; // 1: text right, 0: text left
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_text_right, parent, false);
                return new TextMessageHolder(view);
            case 0:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_text_left, parent, false);
                return new TextMessageHolder(view);
            case 3:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_image_right, parent, false);
                return new ImageMessageHolder(view);
            case 2:
            default:
                view = LayoutInflater.from(context).inflate(R.layout.item_message_image_left, parent, false);
                return new ImageMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.getTimestamp());

        if (holder instanceof TextMessageHolder) {
            ((TextMessageHolder) holder).txtMessage.setText(msg.getText());
            ((TextMessageHolder) holder).txtTime.setText(time);

        } else if (holder instanceof ImageMessageHolder) {
            String imageUri = msg.getImageUri();

            if (imageUri != null && !imageUri.isEmpty()) {
                // 🔍 Log để kiểm tra URI
                android.util.Log.d("CHAT_IMAGE", "Loading URI: " + imageUri);

                Glide.with(context)
                        .load(Uri.parse(imageUri))
                        .placeholder(R.drawable.ic_sample_product)
                        .error(R.drawable.ic_sample_product)
                        .into(((ImageMessageHolder) holder).imgMessage);
            } else {
                ((ImageMessageHolder) holder).imgMessage.setImageResource(R.drawable.ic_sample_product);
            }

            ((ImageMessageHolder) holder).txtTime.setText(time);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class TextMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        TextMessageHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTime = itemView.findViewById(R.id.txt_time);
        }
    }

    static class ImageMessageHolder extends RecyclerView.ViewHolder {
        ImageView imgMessage;
        TextView txtTime;

        ImageMessageHolder(@NonNull View itemView) {
            super(itemView);
            imgMessage = itemView.findViewById(R.id.img_message);
            txtTime = itemView.findViewById(R.id.txt_time);
        }
    }
}

