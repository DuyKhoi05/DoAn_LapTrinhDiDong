package com.example.doan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private Context context;
    private List<Payment> paymentList;

    private String currentUserId;

    public PaymentAdapter(Context context, List<Payment> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    public void setCurrentUserId(String uid) {
        this.currentUserId = uid;
    }

    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        holder.txtTransactionId.setText("Transaction ID: #" + payment.getTransactionId());
        holder.txtDate.setText("Date: " + payment.getDate());
        holder.txtItem.setText("Item: " + payment.getItemName());
        holder.txtAmount.setText("Amount: " + NumberFormat.getCurrencyInstance(Locale.US).format(payment.getAmount()));
        holder.txtStatus.setText("Status: " + payment.getStatus());

        if (payment.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // màu đỏ nhạt cho mình mua
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")); // màu xanh nhạt cho người khác mua của mình
        }
    }

    @Override
    public int getItemCount() {
        return paymentList != null ? paymentList.size() : 0;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView txtTransactionId, txtDate, txtItem, txtAmount, txtStatus;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTransactionId = itemView.findViewById(R.id.txt_transaction_id);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtItem = itemView.findViewById(R.id.txt_item);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtStatus = itemView.findViewById(R.id.txt_status);
        }
    }
}
