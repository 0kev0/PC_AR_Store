package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {
    /*************************************************************VARIABLES******************************************************************************************/
    private List<Product> productList = new ArrayList<>();
    private final OnProductActionsListener listener;
    public interface OnProductActionsListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    public AdminProductAdapter(OnProductActionsListener listener) {
        this.listener = listener;
    }


    public void setProducts(List<Product> products) {
        this.productList = new ArrayList<>(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productStock;
        private final TextView productPrice;
        private final TextView productCode;
        private final TextView productCategory;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productStock = itemView.findViewById(R.id.productStock);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCode = itemView.findViewById(R.id.productCode);
            productCategory = itemView.findViewById(R.id.productCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Product product, OnProductActionsListener listener) {
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));
            productStock.setText(product.getStock() + " unidades");
            productCode.setText("Código: " + product.getProductId());

            if (product.getCategory() != null) {
                productCategory.setText(product.getCategory());
            }

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_products)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_products);
            }

            // Set up listeners
            btnEdit.setOnClickListener(v -> listener.onEditProduct(product));
            btnDelete.setOnClickListener(v -> listener.onDeleteProduct(product));
        }
    }

}