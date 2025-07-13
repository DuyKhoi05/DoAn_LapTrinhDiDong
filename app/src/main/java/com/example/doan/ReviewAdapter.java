package com.example.doan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.ratingBar.setRating(review.getRating());
        holder.txtComment.setText(review.getComment());

        // Lấy tên hoặc email của người đánh giá từ Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(review.getUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        holder.txtReviewer.setText(name != null ? name : "Người dùng ẩn danh");
                    } else {
                        holder.txtReviewer.setText("Không rõ người dùng");
                    }
                })
                .addOnFailureListener(e -> holder.txtReviewer.setText("Lỗi tải tên"));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtReviewer, txtComment;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReviewer = itemView.findViewById(R.id.txt_reviewer);
            txtComment = itemView.findViewById(R.id.txt_comment);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
