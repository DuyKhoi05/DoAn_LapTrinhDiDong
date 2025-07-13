package com.example.doan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView txtName, txtEmail, txtPhone, txtAddress;
    private RecyclerView recyclerProducts;
    private Button btnEditProfile, btnChat, btnLogout, btnDeleteAccount, btnAddProduct, btnPaymentHistory;

    private FirebaseUser user;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.img_avatar);
        txtName = view.findViewById(R.id.txt_name);
        txtEmail = view.findViewById(R.id.txt_email);
        recyclerProducts = view.findViewById(R.id.recycler_user_products);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnChat = view.findViewById(R.id.btn_chat);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnAddProduct = view.findViewById(R.id.btn_add_product);
        btnPaymentHistory = view.findViewById(R.id.btn_payment_history);
        txtPhone = view.findViewById(R.id.txt_phone);
        txtAddress = view.findViewById(R.id.txt_address);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        adapter = new ProductAdapter(getContext(), productList, product -> {
            Intent intent = new Intent(getContext(), EditProductActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            startActivity(intent);
        });

        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProducts.setAdapter(adapter);

        loadUserInfo();
        loadUserProducts();
        setupListeners();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Glide.with(this).load(imageUri).into(imgAvatar);

                        String userId = FirebaseAuth.getInstance().getUid();
                        if (userId == null) return;

                        // 👉 Lưu trực tiếp URI nội bộ (local) thay vì upload lên Storage
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("avatarUrl", imageUri.toString());

                        db.collection("users")
                                .document(userId)
                                .set(updateData, SetOptions.merge())
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(getContext(), "Avatar saved locally", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show());
                    }
                }
        );


        return view;
    }

    private void loadUserInfo() {
        if (user != null) {
            txtEmail.setText(user.getEmail());
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phone = documentSnapshot.getString("phone");
                            String address = documentSnapshot.getString("address");

                            txtName.setText(documentSnapshot.getString("name"));
                            txtPhone.setText("Số điện thoại: " + (phone != null ? phone : "Chưa có"));
                            txtAddress.setText("Địa chỉ: " + (address != null ? address : "Chưa có"));

                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                if (isAdded()) {
                                    Glide.with(requireContext())
                                            .load(Uri.parse(avatarUrl))
                                            .placeholder(R.drawable.ic_profile)
                                            .error(R.drawable.ic_profile)
                                            .into(imgAvatar);
                                }
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_profile); // ảnh mặc định
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load user info", Toast.LENGTH_SHORT).show());
        }
    }


    private void loadUserProducts() {
        db.collection("products")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        productList.add(product);
                    }
                    adapter.setProductList(productList);
                });
    }

    private void setupListeners() {
        imgAvatar.setOnClickListener(v -> showImageOptions());

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        btnChat.setOnClickListener(v -> {
            // Chuyển sang ChatListFragment
            Fragment chatListFragment = new ChatListFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatListFragment) // fragment_container là ID của FrameLayout trong activity
                    .addToBackStack(null) // để quay lại được
                    .commit();
        });

        btnAddProduct.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddProductActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), Login.class));
            getActivity().finish();
        });

        btnPaymentHistory.setOnClickListener(v -> {
            Fragment fragment = new PaymentHistoryFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void showImageOptions() {
        String[] options = {"Xem ảnh", "Chỉnh sửa ảnh"};
        new AlertDialog.Builder(getContext())
                .setTitle("Chọn hành động")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // TODO: Mở ảnh lớn
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        imagePickerLauncher.launch(intent);
                    }
                })
                .show();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản? Tất cả dữ liệu sẽ bị mất.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAccount() {
        db.collection("products")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    db.collection("users").document(user.getUid()).delete()
                            .addOnSuccessListener(unused -> {
                                user.delete();
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(getContext(), Login.class));
                                getActivity().finish();
                            });
                });
    }
}
