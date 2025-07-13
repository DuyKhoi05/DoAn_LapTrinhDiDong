package com.example.doan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPhone, edtAddress;
    Button btnSave, btnEditProducts;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        btnSave = findViewById(R.id.btn_save);
        btnEditProducts = findViewById(R.id.btn_edit_products);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user != null) {
            edtEmail.setText(user.getEmail());

            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            edtName.setText(document.getString("name"));
                            edtPhone.setText(document.getString("phone"));
                            edtAddress.setText(document.getString("address"));
                        }
                    });
        }

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
            String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
            String address = edtAddress.getText() != null ? edtAddress.getText().toString().trim() : "";

            if (name == null || name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);
            updates.put("address", address);

            db.collection("users")
                    .document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnEditProducts.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProductsActivity.class); // Bạn cần tạo thêm activity này
            startActivity(intent);
        });
    }
}
