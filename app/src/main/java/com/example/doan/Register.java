package com.example.doan;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText email, password, name, confirmPW;
    Button dangky;
    FirebaseAuth mAuth;

    FirebaseUser user;
    ProgressBar progressBar;
    TextView loginNow;

    @Override
    public void onStart() {
        super.onStart();
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null && currentUser.isEmailVerified()){
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = findViewById(R.id.txt_register_email);
        password = findViewById(R.id.txt_register_password);
        name = findViewById(R.id.txt_name);
        confirmPW = findViewById(R.id.txt_confirm_password);
        dangky = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        loginNow = findViewById(R.id.loginNow);

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        dangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String Email, Password, Name, ConfirmPW;
                Email = String.valueOf(email.getText());
                Password = String.valueOf(password.getText());
                Name = String.valueOf(name.getText());
                ConfirmPW = String.valueOf(confirmPW.getText());

                if(TextUtils.isEmpty(Email)){
                    Toast.makeText(Register.this, "Enter your email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(TextUtils.isEmpty(Password)){
                    Toast.makeText(Register.this, "Enter your password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(TextUtils.isEmpty(Name)){
                    Toast.makeText(Register.this, "Enter your name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(TextUtils.isEmpty(ConfirmPW)){
                    Toast.makeText(Register.this, "Enter your confirm password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!Password.equals(ConfirmPW)){
                    Toast.makeText(Register.this, "Check your confirm password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                mAuth.createUserWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // Lưu thông tin người dùng vào Firestore
                                        String uid = user.getUid();
                                        String user_name = name.getText().toString();
                                        String user_email = email.getText().toString();

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        Map<String, Object> userInfo = new HashMap<>();
                                        userInfo.put("name", user_name);
                                        userInfo.put("email", user_email);
                                        userInfo.put("verified", false);

                                        db.collection("users").document(uid)
                                                .set(userInfo, SetOptions.merge())
                                                .addOnSuccessListener(unused -> {
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(task1 -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                if (task1.isSuccessful()) {
                                                                    Toast.makeText(Register.this, "Check your email to verify your account.", Toast.LENGTH_LONG).show();
                                                                    FirebaseAuth.getInstance().signOut();
                                                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(Register.this, "Lưu thông tin thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Exception exception = task.getException();
                                    if (exception != null && exception.getMessage() != null &&
                                            exception.getMessage().contains("The email address is already in use")) {
                                        // 🔁 Thử đăng nhập lại để gửi lại email xác minh
                                        mAuth.signInWithEmailAndPassword(Email, Password)
                                                .addOnCompleteListener(loginTask -> {
                                                    if (loginTask.isSuccessful()) {
                                                        FirebaseUser existingUser = mAuth.getCurrentUser();
                                                        if (existingUser != null && !existingUser.isEmailVerified()) {
                                                            existingUser.sendEmailVerification()
                                                                    .addOnSuccessListener(unused -> {
                                                                        Toast.makeText(Register.this, "Email đã tồn tại nhưng chưa xác minh. Đã gửi lại email xác nhận.", Toast.LENGTH_LONG).show();
                                                                        FirebaseAuth.getInstance().signOut();
                                                                    });
                                                        }
                                                    } else {
                                                        Toast.makeText(Register.this, "Email đã tồn tại. Vui lòng đăng nhập hoặc kiểm tra lại mật khẩu.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(Register.this, "Đăng ký thất bại: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

            }
        });
    }
}