package com.example.pcarstore.Adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

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
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
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
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final RatingBar productRating;
        private final Button viewDetailsButton;
        private final LottieAnimationView lottieLoading;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productRating = itemView.findViewById(R.id.productRating);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            lottieLoading = itemView.findViewById(R.id.lottieLoading);

            // Configuración inicial de Lottie
            lottieLoading.setAnimation(R.raw.loading_animation); // Asegúrate de tener este archivo en res/raw/
            lottieLoading.loop(true);
        }

        public void bind(Product product, OnProductClickListener listener) {
            // Configurar datos básicos
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            // Mostrar animación antes de cargar
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();

            // Cargar imagen con Glide
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

            viewDetailsButton.setOnClickListener(v -> listener.onProductClick(product));
            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }

    public void updateProductList(List<Product> newProductList) {
        productList = newProductList;
        notifyDataSetChanged();
    }
}