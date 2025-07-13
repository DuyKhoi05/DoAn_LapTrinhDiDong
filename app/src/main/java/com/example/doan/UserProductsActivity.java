package com.example.doan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class UserProductsActivity extends AppCompatActivity implements UserProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private UserProductAdapter adapter;
    private List<Product> userProducts = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_products);

        recyclerView = findViewById(R.id.recycler_user_products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserProductAdapter(this, userProducts, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserProducts();
    }

    private void loadUserProducts() {
        db.collection("products")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userProducts.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId()); // Gán ID để sửa/xoá
                        userProducts.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("title", product.getTitle());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        intent.putExtra("status", product.getStatus());

        startActivity(intent);
    }


    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("products").document(product.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                loadUserProducts();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(UserProductsActivity.this, EditProductActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("title", product.getTitle());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        startActivity(intent);
    }
}
