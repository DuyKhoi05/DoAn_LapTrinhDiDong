package com.example.doan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtTitle, edtDescription, edtPrice;
    private ImageView imgPreview;
    private Button btnSelectImage, btnUpload;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Spinner spinnerCategory;
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        edtTitle = findViewById(R.id.edt_product_title);
        edtDescription = findViewById(R.id.edt_product_description);
        edtPrice = findViewById(R.id.edt_product_price);
        imgPreview = findViewById(R.id.img_product_preview);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUpload = findViewById(R.id.btn_upload_product);
        progressDialog = new ProgressDialog(this);
        spinnerCategory = findViewById(R.id.spinner_category);

        String[] categories = {"Game", "GameConsole"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "Chọn loại sản phẩm";
            }
        });

        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnUpload.setOnClickListener(v -> uploadProduct());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgPreview.setImageURI(imageUri); // Hiển thị ảnh
        }
    }

    private void uploadProduct() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr) || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        String localUri = imageUri.toString();

        Map<String, Object> product = new HashMap<>();
        product.put("title", title);
        product.put("description", description);
        product.put("price", price);
        product.put("imageUrl", localUri);
        product.put("ownerId", currentUser.getUid());
        product.put("category", selectedCategory);
        product.put("views", 0);

        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    String titleMsg = "Sản phẩm mới!";
                    String bodyMsg = title + " đã được đăng.";

                    db.collection("users")
                            .document(currentUser.getUid())
                            .get()
                            .addOnSuccessListener(doc -> {
                                String token = doc.getString("fcmToken");
                                if (token != null) {
                                    FCMSender.sendNotification(getApplicationContext(), token, titleMsg, bodyMsg);
                                } else {
                                    Log.e("FCM", "Không tìm thấy fcmToken cho người dùng");
                                }
                                progressDialog.dismiss();
                                Toast.makeText(this, "Đăng sản phẩm và gửi thông báo thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Không thể gửi thông báo", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
                });
    }

}
