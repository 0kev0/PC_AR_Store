
package com.example.pcarstore.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Activities.InicioActivity;
import com.example.pcarstore.Adapters.CategoryAdapter;
import com.example.pcarstore.Adapters.ProductAdapter;
import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CatalogoFragment extends Fragment {

    private static final String TAG = "CatalogoFragment";
    private RecyclerView productsRecycler, categoriesRecycler;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private Context context;
    private InicioActivity inicioActivity; // Reference to the activity

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context.getApplicationContext();
        if (context instanceof Activity) {
            inicioActivity = (InicioActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);
        initViews(view);
        setupRecyclerViews(view);
        initializeAdapters();
        loadData();
        return view;
    }

    private void initViews(View view) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        productsRecycler = view.findViewById(R.id.recyclerViewCatalogo);
        categoriesRecycler = view.findViewById(R.id.categoriesRecycler);
    }

    private void setupRecyclerViews(View view) {
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        productsRecycler.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initializeAdapters() {
        // Adapter de productos
        productAdapter = new ProductAdapter(productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                openProductDetail(product.getProductId());
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product, 1);
            }
        }, context);

        // Adapter de categorías
        categoryAdapter = new CategoryAdapter(context, categoryList, category ->
                filterProductsByCategoryName(category.getName())
        );

        productsRecycler.setAdapter(productAdapter);
        categoriesRecycler.setAdapter(categoryAdapter);
    }

    private void loadData() {
        loadCategories();
        loadProducts();
    }

    private void openProductDetail(String productId) {
        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(productId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void addToCart(Product product, int quantity) {
        // Update the item count
        if (inicioActivity != null) {
            inicioActivity.incrementCartCount();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showToast("Debes iniciar sesión para agregar al carrito");
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("carts")
                .child(userId);

        cartRef.child("items").child(product.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateExistingProduct(snapshot, cartRef, product, quantity);
                } else {
                    addNewProduct(cartRef, product, quantity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error al acceder al carrito: " + error.getMessage());
            }
        });
    }

    private void updateExistingProduct(DataSnapshot snapshot, DatabaseReference cartRef, Product product, int quantity) {
        Integer currentQuantity = snapshot.child("quantity").getValue(Integer.class);
        int newQuantity = (currentQuantity != null ? currentQuantity : 0) + quantity;

        cartRef.child("items").child(product.getProductId()).child("quantity")
                .setValue(newQuantity)
                .addOnSuccessListener(aVoid -> {
                    updateCartTotal(cartRef);
                    showToast("Cantidad actualizada en el carrito");
                })
                .addOnFailureListener(e -> showToast("Error al actualizar cantidad"));
    }

    private void addNewProduct(DatabaseReference cartRef, Product product, int quantity) {
        OrderItem newItem = new OrderItem(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                quantity
        );

        if (product.getMainImageUrl() != null) {
            newItem.setImageUrl(product.getMainImageUrl());
        }

        cartRef.child("items").child(product.getProductId()).setValue(newItem)
                .addOnSuccessListener(aVoid -> {
                    updateCartTotal(cartRef);
                    showToast("Producto agregado al carrito");
                })
                .addOnFailureListener(e -> showToast("Error al agregar producto"));
    }

    private void loadCategories() {
        mDatabase.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> newCategories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = snapshot.getValue(Category.class);
                        if (category != null) {
                            category.setCategoryId(snapshot.getKey());
                            newCategories.add(category);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category: " + e.getMessage());
                    }
                }
                if (categoryAdapter != null) {
                    categoryList.clear();
                    categoryList.addAll(newCategories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error al cargar categorías: " + databaseError.getMessage());
                Log.e(TAG, "Error loading categories", databaseError.toException());
            }
        });
    }

    private void loadProducts() {
        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> newProducts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setProductId(snapshot.getKey());
                            newProducts.add(product);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product: " + e.getMessage());
                    }
                }
                if (productAdapter != null) {
                    productList.clear();
                    productList.addAll(newProducts);
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error al cargar productos: " + databaseError.getMessage());
                Log.e(TAG, "Error loading products", databaseError.toException());
            }
        });
    }

    private void filterProductsByCategoryName(String categoryName) {
        mDatabase.child("products").orderByChild("category").equalTo(categoryName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Product> filteredProducts = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Product product = snapshot.getValue(Product.class);
                                if (product != null) {
                                    product.setProductId(snapshot.getKey());
                                    filteredProducts.add(product);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing product: " + e.getMessage());
                            }
                        }
                        if (productAdapter != null) {
                            productList.clear();
                            productList.addAll(filteredProducts);
                            productAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showError("Error al filtrar productos: " + databaseError.getMessage());
                    }
                });
    }

    private void updateCartTotal(DatabaseReference cartRef) {
        cartRef.child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double total = 0.0;
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    OrderItem item = itemSnapshot.getValue(OrderItem.class);
                    if (item != null) {
                        total += item.getPrice() * item.getQuantity();
                    }
                }
                cartRef.child("total").setValue(total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al calcular total del carrito: " + error.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, message);
    }
}