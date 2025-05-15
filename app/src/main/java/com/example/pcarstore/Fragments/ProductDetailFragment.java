package com.example.pcarstore.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.Activities.ProductShowARActivity;
import com.example.pcarstore.Adapters.ProductImagesAdapter;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class ProductDetailFragment extends Fragment {

    private String productId;
    private DatabaseReference productRef;
    private ValueEventListener productListener;

    // UI Components
    private TextView productName, productPrice, productSpecs, productDescription;
    private RatingBar productRating;
    private RecyclerView imagesRecycler;
    private Button btnAddToCart, btnViewAR;
    private ProductImagesAdapter imagesAdapter;

    public static ProductDetailFragment newInstance(String productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString("productId", productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all views
        initializeViews(view);

        // Set up Firebase reference
        productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);

        // Set up realtime listener
        setupRealtimeListener();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        productName = view.findViewById(R.id.productName);
        productPrice = view.findViewById(R.id.productPrice);
        productRating = view.findViewById(R.id.productRatingDetail);
        productSpecs = view.findViewById(R.id.productSpecs);
        productDescription = view.findViewById(R.id.productDescription);
        imagesRecycler = view.findViewById(R.id.recyclerProductImages);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnViewAR = view.findViewById(R.id.btnViewAR);

        // Configure RecyclerView
        imagesRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void openARModel() {
        // Verificar si tenemos un ID de producto
        if (productId != null && !productId.isEmpty()) {
            // Crear un Intent para abrir la actividad ProductShowARActivity
            Intent arIntent = new Intent(getActivity(), ProductShowARActivity.class);

            // Pasar el ID del producto como extra en el Intent
            arIntent.putExtra("product_id", productId);

            // Mostrar mensaje informativo
            Toast.makeText(getContext(), "Iniciando visualización AR", Toast.LENGTH_SHORT).show();

            // Iniciar la actividad
            startActivity(arIntent);
        } else {
            Toast.makeText(getContext(), "Error: ID de producto no disponible para AR", Toast.LENGTH_SHORT).show();
        }
    }
    private void setupRealtimeListener() {
        productListener = productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    updateUI(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading product details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Product product) {
        // Basic product info
        productName.setText(product.getName());
        productPrice.setText(String.format("$%.2f", product.getPrice()));
        productDescription.setText(product.getDescription());

        // Rating
        if (product.getRating() != null) {
            productRating.setRating(product.getRating().floatValue());
        }

        // Specifications (assuming specs is a Map<String, String> in Product model)
        if (product.getSpecifications() != null) {
            StringBuilder specsBuilder = new StringBuilder();
            for (String key : product.getSpecifications().keySet()) {
                specsBuilder.append(key).append(": ").append(product.getSpecifications().get(key)).append("\n");
            }
            productSpecs.setText(specsBuilder.toString().trim());
        }

        // Images gallery
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imagesAdapter = new ProductImagesAdapter(product.getImageUrls());
            imagesRecycler.setAdapter(imagesAdapter);

        }

        // AR button visibility
        if (product.getModel3dUrl() == null || product.getModel3dUrl().isEmpty()) {
            btnViewAR.setVisibility(View.GONE);
        }
    }

    private void setupButtonListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnViewAR.setOnClickListener(v -> openARModel());
    }

    private void addToCart() {
        // Implement add to cart functionality
        Toast.makeText(getContext(), "Producto añadido al carrito", Toast.LENGTH_SHORT).show();
        // You would typically add the product to a cart database or local storage here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (productListener != null) {
            productRef.removeEventListener(productListener);
        }
    }
}