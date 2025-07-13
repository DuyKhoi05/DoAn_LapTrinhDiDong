package com.example.doan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText edtSearch, edtPriceMin, edtPriceMax;
    private Spinner spinnerSort;
    private ProductAdapter productAdapter;
    private List<Product> fullProductList = new ArrayList<>();
    private FirebaseFirestore db;
    Spinner spinnerCategory;
    String selectedCategory = "Tất cả";
    private Button btnFilter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ
        recyclerView = view.findViewById(R.id.recycler_products);
        edtSearch = view.findViewById(R.id.edt_search);
        edtPriceMin = view.findViewById(R.id.edt_price_min);
        edtPriceMax = view.findViewById(R.id.edt_price_max);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnFilter = view.findViewById(R.id.btn_filter);

        btnFilter.setOnClickListener(v -> applyFilters());

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // Giao diện lưới 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            startActivity(intent);
        });

        recyclerView.setAdapter(productAdapter);

        // Spinner sắp xếp
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        db = FirebaseFirestore.getInstance();

        fetchProducts();
        setupListeners();

        productAdapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            startActivity(intent);
        });


        return view;
    }


    private void fetchProducts() {
            db.collection("products")
                    .orderBy("title", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        fullProductList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Product product = doc.toObject(Product.class);
                            product.setId(doc.getId());
                            if (product.getStatus() == null || !product.getStatus().equals("Paused")) {
                                fullProductList.add(product);
                            }
                        }
                        applyFilters();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load products.", Toast.LENGTH_SHORT).show();
                    });
    }

    private void setupListeners() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

    }

    private void applyFilters() {
        String keyword = edtSearch.getText().toString().toLowerCase();
        String minStr = edtPriceMin.getText().toString();
        String maxStr = edtPriceMax.getText().toString();

        double minPrice = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
        double maxPrice = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);

        List<Product> filtered = new ArrayList<>();
        for (Product product : fullProductList) {
            boolean matchCategory = selectedCategory.equals("Tất cả") || selectedCategory.equals(product.getCategory());
            if (product.getTitle().toLowerCase().contains(keyword)
                    && product.getPrice() >= minPrice
                    && product.getPrice() <= maxPrice
                    && matchCategory) {
                filtered.add(product);
            }
        }

        String sort = spinnerSort.getSelectedItem().toString();
        switch (sort) {
            case "Newest":
                Collections.reverse(filtered);
                break;
            case "Price ↑":
                Collections.sort(filtered, Comparator.comparingDouble(Product::getPrice));
                break;
            case "Price ↓":
                Collections.sort(filtered, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
        }

        productAdapter.setProductList(filtered);
    }
}
