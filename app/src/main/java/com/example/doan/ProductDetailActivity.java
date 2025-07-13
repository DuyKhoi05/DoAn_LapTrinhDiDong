package com.example.doan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView txtTitle, txtDescription, txtPrice, txtEmail, txtCategory, txtViews;
    private Button btnBuy, btnRate;
    private RecyclerView recyclerReviews;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private FirebaseFirestore db;

    private PaymentSheet paymentSheet;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51RgsTWJdGZiQ4fdVd3NfWoPHBDvHkklkkfDI2lpzRuFxkMDzAGTugRti3BK5NIJG4cEg2rkJPzOF2sRyvybwIL0m00ZbOuL5qw" // ← Thay bằng publishable key của bạn
        );
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        imgProduct = findViewById(R.id.img_product);
        txtTitle = findViewById(R.id.txt_title);
        txtDescription = findViewById(R.id.txt_description);
        txtPrice = findViewById(R.id.txt_price);
        txtEmail = findViewById(R.id.txt_owner_email);
        btnBuy = findViewById(R.id.btn_buy);
        recyclerReviews = findViewById(R.id.recycler_reviews);
        btnRate = findViewById(R.id.btn_rate);
        txtCategory = findViewById(R.id.txt_category);
        txtViews = findViewById(R.id.txt_views);

        db = FirebaseFirestore.getInstance();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);

        btnRate.setOnClickListener(v -> showRatingDialog());

        String productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không có ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails(productId);
        loadReviews(productId);

        txtEmail.setOnClickListener(v -> {
            if (product != null && product.getOwnerId() != null) {
                Intent intent = new Intent(ProductDetailActivity.this, UserDetailActivity.class);
                intent.putExtra("userId", product.getOwnerId()); // ✅ Lấy từ product
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không thể lấy thông tin người bán", Toast.LENGTH_SHORT).show();
            }
        });

        btnBuy.setOnClickListener(v -> {
            if (product != null) {
                startCheckout(product);
            } else {
                Toast.makeText(this, "Chưa tải xong sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateViews(String productId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || product == null) return;

        if (product.getOwnerId().equals(currentUser.getUid())) return;

        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentReference productRef = db.collection("products").document(productId);
            DocumentSnapshot snapshot = transaction.get(productRef);

            Long currentViews = snapshot.getLong("views");
            if (currentViews == null) currentViews = 0L;

            transaction.update(productRef, "views", currentViews + 1);
            return null;
        }).addOnFailureListener(e -> Log.e("VIEWS", "Không thể cập nhật lượt xem", e));
    }

    private void startCheckout(Product product) {
        // Sử dụng client_secret test của bạn từ Stripe CLI
        String clientSecret = "pi_3RjzuiJdGZiQ4fdV1TdrDQJs_secret_oHymG2NY0Kn2MSoeX30FqGtLv";

        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("Your Test Store")
                .allowsDelayedPaymentMethods(true) // có thể bỏ nếu không dùng
                .build();

        try {
            paymentSheet.presentWithPaymentIntent(clientSecret, config);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("STRIPE", "Stripe PaymentSheet lỗi: " + e.getMessage(), e);
            Toast.makeText(this, "Thiết bị không hỗ trợ thanh toán", Toast.LENGTH_LONG).show();
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
            savePaymentHistory();
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Đã huỷ thanh toán", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failed = (PaymentSheetResult.Failed) result;
            Toast.makeText(this, "Lỗi thanh toán: " + failed.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePaymentHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || product == null) {
            Log.e("PAYMENT", "User hoặc sản phẩm bị null");
            return;
        }

        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()),
                product.getTitle(),
                product.getPrice(),
                "Completed",
                user.getUid(),          // userId = người mua
                product.getOwnerId()    // sellerId = người bán ✅
        );

        FirebaseFirestore.getInstance()
                .collection("payments")
                .add(payment)
                .addOnSuccessListener(doc -> Log.d("PAYMENT", "Saved"))
                .addOnFailureListener(e -> Log.e("PAYMENT", "Failed to save", e));
    }


    // --- Load sản phẩm ---
    private void loadProductDetails(String productId) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    product = doc.toObject(Product.class);
                    Long views = doc.getLong("views");
                    txtViews.setText((views != null ? views : 0) + " lượt xem");
                    txtTitle.setText(product.getTitle());
                    txtDescription.setText(product.getDescription());
                    txtPrice.setText(NumberFormat.getCurrencyInstance(Locale.US).format(product.getPrice()));
                    txtCategory.setText("Loại: " + (product.getCategory() != null ? product.getCategory() : "Không xác định"));


                    Glide.with(this)
                            .load(Uri.parse(product.getImageUrl()))
                            .placeholder(R.drawable.ic_sample_product)
                            .error(R.drawable.ic_sample_product)
                            .into(imgProduct);

                    if ("Sold".equals(product.getStatus())) {
                        btnBuy.setText("Sold");
                        btnBuy.setEnabled(false);
                    } else if ("Paused".equals(product.getStatus())) {
                        btnBuy.setText("Tạm ẩn");
                        btnBuy.setEnabled(false);
                    }

                    db.collection("users").document(product.getOwnerId())
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                txtEmail.setText(userDoc.exists()
                                        ? userDoc.getString("email")
                                        : "Không tìm thấy người đăng");
                            })
                            .addOnFailureListener(e -> txtEmail.setText("Lỗi khi tải email"));
                    updateViews(productId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show());
    }

    // --- Đánh giá ---
    private void showRatingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_input);
        EditText edtComment = dialogView.findViewById(R.id.edt_comment_input);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Đánh giá sản phẩm")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = edtComment.getText().toString().trim();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String productId = getIntent().getStringExtra("productId");

                    Review review = new Review();
                    review.setRating(rating);
                    review.setComment(comment);
                    review.setUserId(userId);
                    review.setTimestamp(new Date());

                    db.collection("products")
                            .document(productId)
                            .collection("reviews")
                            .document(userId)
                            .set(review)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                                loadReviews(productId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadReviews(String productId) {
        db.collection("products")
                .document(productId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(query -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        reviewList.add(doc.toObject(Review.class));
                    }
                    reviewAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể tải đánh giá", Toast.LENGTH_SHORT).show());
    }
}

