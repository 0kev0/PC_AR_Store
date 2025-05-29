package com.example.pcarstore.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Activities.InicioActivity;
import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.Adapters.CategoryAdapter;
import com.example.pcarstore.Adapters.ProductAdapter;
import com.example.pcarstore.Dialogs.PrimeReminder;
import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class CatalogoFragment extends Fragment{
    /*************************************************************VARIABLES******************************************************************************************/
    private static final String TAG = "CatalogoFragment";
    private RecyclerView productsRecycler, categoriesRecycler;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private DatabaseReference wishlistRef;
    private Context context;
    private InicioActivity inicioActivity;
    private FirebaseAuth mAuth;
    private EditText searchInput;;
    private DatabaseReference productsRef;
    private boolean isSearchVisible = true;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);
        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");

        LinearLayout searchLayoutProducts = view.findViewById(R.id.searchLayoutProducts);
        searchInput = view.findViewById(R.id.searchEditText);

        // Configurar el buscador
        setupSearchFunctionality();

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


        // Inicializar referencia a la wishlist
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(currentUser.getUid());
        }
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
        // Adapter de productos con la funcionalidad de wishlist corregida
        productAdapter = new ProductAdapter(productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                openProductDetail(product.getProductId());
            }

            @Override
            public void onAddToCart(Product product) {
                addToCart(product, 1);
            }

            @Override
            public void onWishlistClick(Product product, boolean isInWishlist) {
                handleWishlistClick(product, isInWishlist);
            }
        }, context);

        // Adapter de categorías
        categoryAdapter = new CategoryAdapter(context, categoryList, category ->
                filterProductsByCategoryName(category.getName())
        );

        productsRecycler.setAdapter(productAdapter);
        categoriesRecycler.setAdapter(categoryAdapter);
    }

    private void setupSearchFunctionality() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchProducts(String searchText) {
        if (searchText.isEmpty()) {
            loadAllProducts();
            return;
        }
        final String normalizedSearch = normalizeSearchTerm(searchText);
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        String productNameNormalized = normalizeSearchTerm(product.getName());
                        if (productNameNormalized.contains(normalizedSearch)) {
                            productList.add(product);
                        }
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });
    }

    public static String normalizeSearchTerm(String input) {
        if (input == null) return "";

        // Convertir a minúsculas y quitar acentos
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase();

        return normalized;
    }

    private void loadAllProducts() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleWishlistClick(Product product, boolean isInWishlist) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredDialog("Para guardar productos en tu lista de deseos, necesitas iniciar sesión.");
            return;
        }

        if (wishlistRef == null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(currentUser.getUid());
        }

        if (isInWishlist) {
            removeFromWishlist(product);
        } else {
            addToWishlist(product);
        }
    }

    private void addToWishlist(Product product) {
        product.setInWishlist(true);
        productAdapter.notifyDataSetChanged();

        wishlistRef.child(product.getProductId()).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    showToast("Añadido a tu lista de deseos");
                })
                .addOnFailureListener(e -> {
                    // En caso de error, revertimos el cambio
                    product.setInWishlist(false);
                    productAdapter.notifyDataSetChanged();
                    showToast("Error al añadir a la lista de deseos");
                });
    }

    private void removeFromWishlist(Product product) {
        product.setInWishlist(false);
        productAdapter.notifyDataSetChanged();

        wishlistRef.child(product.getProductId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("Eliminado de tu lista de deseos");
                })
                .addOnFailureListener(e -> {
                    product.setInWishlist(true);
                    productAdapter.notifyDataSetChanged();
                    showToast("Error al eliminar de la lista de deseos");
                });
    }

    private void showLoginRequiredDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Inicio de sesión requerido")
                .setMessage(message)
                .setPositiveButton("Iniciar sesión", (dialog, which) -> {
                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                    startActivity(loginIntent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_heart)
                .setCancelable(false)
                .show();
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredDialog("Debes iniciar sesión para agregar productos al carrito.");
            return;
        }

        String userId = currentUser.getUid();

        if (inicioActivity != null) {
            inicioActivity.incrementCartCount();
        }

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

                    // Verificar wishlist para cada producto
                    checkWishlistStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error al cargar productos: " + databaseError.getMessage());
                Log.e(TAG, "Error loading products", databaseError.toException());
            }
        });
    }

    private void checkWishlistStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || wishlistRef == null) {
            // Si no hay usuario logueado, no hay productos en wishlist
            for (Product product : productList) {
                product.setInWishlist(false);
            }
            productAdapter.notifyDataSetChanged();
            return;
        }

        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Product product : productList) {
                    boolean isInWishlist = snapshot.hasChild(product.getProductId());
                    product.setInWishlist(isInWishlist);
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking wishlist status: " + error.getMessage());
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

                            checkWishlistStatus();
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

    @Override
    public void onResume() {
        super.onResume();
        checkPrimeReminder();
    }

    private void checkPrimeReminder() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (activity.isFinishing() || activity.isDestroyed()) {
                            return;
                        }

                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                user.setUserId(snapshot.getKey());
                                PrimeReminder.showIfNeeded(activity, user);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PrimeCheck", "Error checking prime status: " + error.getMessage());
                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            Toast.makeText(activity, "Error checking membership", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}