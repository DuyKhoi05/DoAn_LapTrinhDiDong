    package com.example.doan;

    import android.content.Context;
    import android.content.Intent;
    import android.net.Uri;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import java.text.NumberFormat;
    import java.util.Locale;

    import com.bumptech.glide.Glide;

    import java.util.List;

        public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

            private Context context;
            private List<Product> productList;
            private OnItemClickListener itemClickListener;

            public interface OnItemClickListener {
                void onItemClick(Product product);
            }


            public ProductAdapter(Context context, List<Product> productList, OnItemClickListener itemClickListener) {
                this.context = context;
                this.productList = productList;
                this.itemClickListener = itemClickListener;
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
                return new ProductViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
                Product product = productList.get(position);
                holder.txtTitle.setText(product.getTitle());
                holder.txtPrice.setText(NumberFormat.getCurrencyInstance(Locale.US).format(product.getPrice()));

                String imageUrl = product.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context)
                            .load(Uri.parse(imageUrl))
                            .placeholder(R.drawable.ic_sample_product)
                            .error(R.drawable.ic_sample_product)
                            .into(holder.imgProduct);
                } else {
                    holder.imgProduct.setImageResource(R.drawable.ic_sample_product);
                }


                holder.itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(product);
                    }
                });


            }

            @Override
            public int getItemCount() {
                return productList.size();
            }

            public void setProductList(List<Product> productList) {
                this.productList = productList;
                notifyDataSetChanged();
            }

            public interface OnProductClickListener {
                void onProductClick(Product product);
            }

            private OnProductClickListener listener;

            public void setOnProductClickListener(OnProductClickListener listener) {
                this.listener = listener;
            }


            public static class ProductViewHolder extends RecyclerView.ViewHolder {
                ImageView imgProduct;
                TextView txtTitle, txtPrice;

                public ProductViewHolder(@NonNull View itemView) {
                    super(itemView);
                    imgProduct = itemView.findViewById(R.id.product_image);
                    txtTitle = itemView.findViewById(R.id.product_title);
                    txtPrice = itemView.findViewById(R.id.product_price);

                }
            }

        }
