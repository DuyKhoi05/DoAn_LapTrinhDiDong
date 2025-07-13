package com.example.doan;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private PaymentAdapter adapter;
    private List<Payment> paymentList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_payments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentAdapter(getContext(), paymentList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadPayments();

        return view;
    }

    private void loadPayments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        FirebaseFirestore.getInstance().collection("payments")
                .get()
                .addOnSuccessListener(query -> {
                    List<Payment> filteredPayments = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        Payment payment = doc.toObject(Payment.class);

                        // Hiển thị nếu là người mua hoặc người bán
                        if (payment.getUserId() != null && payment.getSellerId() != null) {
                            if (payment.getUserId().equals(currentUserId) ||
                                    payment.getSellerId().equals(currentUserId)) {
                                filteredPayments.add(payment);
                            }
                        }
                    }

                    paymentList.clear();
                    paymentList.addAll(filteredPayments);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải lịch sử giao dịch", Toast.LENGTH_SHORT).show();
                });
    }
}
