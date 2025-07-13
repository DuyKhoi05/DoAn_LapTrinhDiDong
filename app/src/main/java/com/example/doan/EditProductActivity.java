package com.example.doan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private EditText edtTitle, edtDescription, edtPrice;
    private ImageView imgProduct;
    private Button btnSave;
    private String productId, imageUrl;
    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri = null;
    Spinner spinnerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        edtTitle = findViewById(R.id.edt_title);
        edtDescription = findViewById(R.id.edt_description);
        edtPrice = findViewById(R.id.edt_price);
        imgProduct = findViewById(R.id.img_product);
        btnSave = findViewById(R.id.btn_save);
        spinnerStatus = findViewById(R.id.spinner_status);

        imgProduct.setOnClickListener(v -> openImagePicker());

        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tạo adapter cho Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Available", "Sold", "Paused"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

// Set giá trị ban đầu từ Firestore
        String currentStatus = intent.getStringExtra("status");
        if (currentStatus != null) {
            int position = statusAdapter.getPosition(currentStatus);
            spinnerStatus.setSelection(position);
        }

        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        double price = intent.getDoubleExtra("price", 0);
        imageUrl = intent.getStringExtra("imageUrl");

        edtTitle.setText(title);
        edtDescription.setText(description);
        edtPrice.setText(String.valueOf(price));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(imageUrl))
                    .placeholder(R.drawable.ic_sample_product)
                    .error(R.drawable.ic_sample_product)
                    .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.ic_sample_product);
        }

        btnSave.setOnClickListener(v -> {
            String newTitle = edtTitle.getText().toString().trim();
            String newDescription = edtDescription.getText().toString().trim();
            double newPrice = Double.parseDouble(edtPrice.getText().toString().trim());
            String selectedStatus = spinnerStatus.getSelectedItem().toString();

            Map<String, Object> updates = new HashMap<>();
            updates.put("title", newTitle);
            updates.put("description", newDescription);
            updates.put("price", newPrice);
            updates.put("status", selectedStatus);

            if (selectedImageUri != null) {
                updates.put("imageUrl", selectedImageUri.toString());
            } else if (imageUrl == null || imageUrl.isEmpty()) {
                updates.put("imageUrl", "");
            } else {
                updates.put("imageUrl", imageUrl);
            }

            FirebaseFirestore.getInstance()
                    .collection("products")
                    .document(productId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri rawUri = data.getData();
            selectedImageUri = copyImageToCache(rawUri);
            if (selectedImageUri != null) {
                imgProduct.setImageURI(selectedImageUri);
            } else {
                Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private Uri copyImageToCache(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File file = new File(getCacheDir(), "product_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
