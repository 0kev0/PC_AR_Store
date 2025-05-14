package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.CategoryAdapter;
import com.example.pcarstore.Adapters.ProductAdapter;
import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
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
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);

        //Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configurar RecyclerView de categorías
        categoriesRecycler = view.findViewById(R.id.categoriesRecycler);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        // Configurar RecyclerView de productos
        productsRecycler = view.findViewById(R.id.recyclerViewCatalogo);
        productsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar adapters
        productAdapter = new ProductAdapter(productList, product -> {
            // Manejar clic en producto
            Toast.makeText(getContext(), "Seleccionado: " + product.getName(), Toast.LENGTH_SHORT).show();
            // Aquí puedes abrir un detalle del producto
        });

        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            // Pasar el categoryId (ej: "cat_001") en lugar del nombre
            filterProductsByCategory(category.getCategoryId());
            Toast.makeText(getContext(), "Categoría: " + category.getName(), Toast.LENGTH_SHORT).show();
        });

        productsRecycler.setAdapter(productAdapter);
        categoriesRecycler.setAdapter(categoryAdapter);

        // Cargar datos
        loadCategories();
        loadProducts();

        return view;
    }

    private void loadCategories() {
        mDatabase.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = snapshot.getValue(Category.class);
                        if (category != null) {
                            category.setCategoryId(snapshot.getKey());
                            categoryList.add(category);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category: " + e.getMessage());
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error al cargar categorías: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading categories", databaseError.toException());
            }
        });
    }

    private void loadProducts() {
        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setProductId(snapshot.getKey());
                            productList.add(product);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product: " + e.getMessage());
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error al cargar productos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading products", databaseError.toException());
            }
        });
    }

    private void filterProductsByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            loadProducts(); // Mostrar todos si no hay categoría seleccionada
            return;
        }

        // Primero obtenemos el nombre de la categoría correspondiente al ID
        mDatabase.child("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                if (categorySnapshot.exists()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        String categoryName = category.getName();

                        // Ahora filtramos los productos por el nombre de la categoría
                        mDatabase.child("products").orderByChild("category").equalTo(categoryName)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        productList.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            try {
                                                Product product = snapshot.getValue(Product.class);
                                                if (product != null) {
                                                    product.setProductId(snapshot.getKey());
                                                    productList.add(product);
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error parsing product: " + e.getMessage());
                                            }
                                        }
                                        productAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getContext(), "Error al filtrar productos: " + databaseError.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getContext(), "Categoría no encontrada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error al obtener categoría: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }}