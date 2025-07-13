package com.example.doan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    EditText email, password;
    Button dangnhap;
    ProgressBar progressBar;
    TextView forgotPassword, registerNow;
    LinearLayout DNbangGoogle;

    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null && currentUser.isEmailVerified()) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.txt_email);
        password = findViewById(R.id.txt_password);
        dangnhap = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        registerNow = findViewById(R.id.registerNow);
        forgotPassword = findViewById(R.id.txt_forgotpassword);
        DNbangGoogle = findViewById(R.id.lnl_loginwithgoogle);
        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // đảm bảo đã thêm vào strings.xml
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut();

        DNbangGoogle.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        registerNow.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
            finish();
        });

        forgotPassword.setOnClickListener(v -> {
            String emailInput = email.getText().toString().trim();
            if (TextUtils.isEmpty(emailInput)) {
                Toast.makeText(Login.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(emailInput)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Password reset link sent to your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Login.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dangnhap.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String Email = email.getText().toString().trim();
            String Password = password.getText().toString().trim();

            if (TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password)) {
                Toast.makeText(Login.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(user.getUid())
                                        .update("verified", true)
                                        .addOnSuccessListener(unused -> {
                                            progressBar.setVisibility(View.GONE);
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(Login.this, "Login successful, but Firestore update failed.", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                if (user != null) {
                                    user.sendEmailVerification();
                                }
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(Login.this, "Please verify your email.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("name", name);
                            userInfo.put("email", email);
                            userInfo.put("verified", true);

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .set(userInfo, SetOptions.merge())
                                    .addOnSuccessListener(unused -> {
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Login.this, "Firestore save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Google Sign-In failed: user is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Google Sign-In failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
