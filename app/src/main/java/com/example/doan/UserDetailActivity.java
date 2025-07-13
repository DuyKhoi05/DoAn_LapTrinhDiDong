package com.example.doan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.doan.Product;
import com.google.firebase.firestore.*;
import com.google.firebase.auth.FirebaseAuth;

import java.util.*;

public class UserDetailActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView txtName, txtEmail, txtPhone, txtAddress;
    private Button btnChat;
    private RecyclerView recyclerProducts;

    private String userId;
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter productAdapter; // Sử dụng adapter của bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        imgAvatar = findViewById(R.id.img_avatar);
        txtName = findViewById(R.id.txt_name);
        txtEmail = findViewById(R.id.txt_email);
        txtPhone = findViewById(R.id.txt_phone);
        txtAddress = findViewById(R.id.txt_address);
        btnChat = findViewById(R.id.btn_chat);
        recyclerProducts = findViewById(R.id.recycler_user_products);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        loadUserInfo();
        loadUserProducts();

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", userId);
            startActivity(intent);
        });
    }

        private void setupRecyclerView() {
            recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
            productAdapter = new ProductAdapter(this, productList, product -> {
                Intent intent = new Intent(UserDetailActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            }); // Bạn cần truyền đúng Adapter của mình
            recyclerProducts.setAdapter(productAdapter);
        }

    private void loadUserInfo() {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String avatarUrl = doc.getString("avatarUrl");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");

                        txtName.setText(name != null ? name : "Không rõ tên");
                        txtEmail.setText(email != null ? email : "Không rõ email");
                        txtPhone.setText("Số điện thoại: " + (phone != null ? phone : "Chưa có"));
                        txtAddress.setText("Địa chỉ: " + (address != null ? address : "Chưa có"));

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(Uri.parse(avatarUrl))
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(imgAvatar);
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_profile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProducts() {
        FirebaseFirestore.getInstance().collection("products")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            productList.add(product);
                        }
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }
}
