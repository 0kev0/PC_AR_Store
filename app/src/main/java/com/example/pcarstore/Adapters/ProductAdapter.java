package com.example.pcarstore.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener  listener;
    private Context context;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCart(Product product);  // Changed from OnAddToCartListener to onAddToCart
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener, Context context) {
        this.productList = productList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final Button viewDetailsButton, addToCartButton;
        private final LottieAnimationView lottieLoading;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            lottieLoading = itemView.findViewById(R.id.lottieLoading);

            lottieLoading.setAnimation(R.raw.loading_animation);
            lottieLoading.loop(true);
        }

        public void bind(Product product, OnProductClickListener  listener) {
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();

            Glide.with(itemView.getContext())
                    .load(product.getMainImageUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            lottieLoading.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            lottieLoading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(productImage);

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            addToCartButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCart(product);
                    showToast(itemView.getContext(), "Producto agregado al carrito");
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }

        private void showToast(Context context, String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProductList(List<Product> newProductList) {
        if (newProductList != null) {
            productList = newProductList;
            notifyDataSetChanged();
        }
    }

    private void showToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}