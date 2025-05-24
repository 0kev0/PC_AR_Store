package com.example.pcarstore.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Activities.InicioActivity;
import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.Activities.ProductShowARActivity;
import com.example.pcarstore.Adapters.ProductImagesAdapter;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.DownloadService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;


public class ProductDetailFragment extends Fragment {

    private static final String TAG = "ProductDetailFragment";
    private String productId;
    private DatabaseReference productRef;
    private ValueEventListener productListener;
    private DatabaseReference wishlistRef;

    private Product currentProduct;
    private FirebaseStorage storage;
    private TextView productName, productPrice, productSpecs, productDescription;

    private RatingBar productRating;
    private RecyclerView imagesRecycler;
    private Button btnAddToCart, btnViewAR;
    private ProductImagesAdapter imagesAdapter;
    private InicioActivity inicioActivity;
    private Context context;
    private FirebaseAuth mAuth;
    private MaterialButton btnWishlist;

    // Modelo 3D y textura
    private File modelFile;
    private File textureFile;
    private boolean modelLoaded = false;
    private boolean textureLoaded = false;
    private ProgressDialog progressDialog;
    private static final long CACHE_EXPIRATION_TIME = 120000; // 2 minutos en milisegundos

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

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(currentUser.getUid());
        }
        if (currentUser != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference()
                    .child("wishlist")
                    .child(currentUser.getUid()); // Referencia específica del usuario
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof InicioActivity) {
            inicioActivity = (InicioActivity) context;
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

        initializeViews(view);
        setupWishlistButton();

        productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        setupRealtimeListener();
        setupButtonListeners();

        // Initialize progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Preparando visualización AR");
        progressDialog.setMessage("Descargando archivos necesarios...");
        progressDialog.setCancelable(false);
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
        btnWishlist = view.findViewById(R.id.btnWishlist);

        productRating.setNumStars(5);
        productRating.setStepSize(0.5f);

        imagesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupWishlistButton() {
        btnWishlist.setOnClickListener(v -> handleWishlistClick());
        checkWishlistStatus();
    }
    private void handleWishlistClick() {
        if (currentProduct == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showLoginRequiredDialog("Para guardar productos en tu lista de deseos, necesitas iniciar sesión.");
            return;
        }

        if (wishlistRef == null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference()
                    .child("wishlist")
                    .child(currentUser.getUid());
        }

        if (currentProduct.isInWishlist()) {
            removeFromWishlist();
        } else {
            addToWishlist();
        }
    }
    private void addToWishlist() {
        wishlistRef.child(productId)
                .setValue(true) // Guarda como true para indicar que existe
                .addOnSuccessListener(aVoid -> {
                    currentProduct.setInWishlist(true);
                    updateWishlistButton(true);
                    showToast("Añadido a tu lista de deseos");
                })
                .addOnFailureListener(e -> showToast("Error al añadir a la lista de deseos"));
    }
    private void removeFromWishlist() {
        wishlistRef.child(productId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    currentProduct.setInWishlist(false);
                    updateWishlistButton(false);
                    showToast("Eliminado de tu lista de deseos");
                })
                .addOnFailureListener(e -> showToast("Error al eliminar de la lista de deseos"));
    }
    private void updateWishlistButton(boolean isInWishlist) {
        if (isInWishlist) {
            btnWishlist.setText("Quitar de favoritos");
            btnWishlist.setIconResource(R.drawable.ic_heart);
            btnWishlist.setIconTint(ColorStateList.valueOf(Color.RED));
            btnWishlist.setStrokeColor(ColorStateList.valueOf(Color.RED));
            btnWishlist.setTextColor(Color.RED);
        } else {
            btnWishlist.setText("Añadir a favoritos");
            btnWishlist.setIconResource(R.drawable.ic_unfill_heart);
            btnWishlist.setIconTint(ColorStateList.valueOf(Color.parseColor("#FF4081")));
            btnWishlist.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF4081")));
            btnWishlist.setTextColor(Color.parseColor("#FF4081"));
        }
    }
    private void checkWishlistStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || wishlistRef == null) {
            updateWishlistButton(false);
            return;
        }

        wishlistRef.child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isInWishlist = snapshot.exists();
                        if (currentProduct != null) {
                            currentProduct.setInWishlist(isInWishlist);
                        }
                        updateWishlistButton(isInWishlist);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking wishlist status", error.toException());
                    }
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
    private void openARModel() {
        if (currentProduct != null && currentProduct.getModel3dUrl() != null && currentProduct.getTextureUrl() != null) {
            File cacheDir = getContext().getCacheDir();
            String modelFilename = currentProduct.getModel3dUrl().substring(currentProduct.getModel3dUrl().lastIndexOf('/') + 1);
            String textureFilename = currentProduct.getTextureUrl().substring(currentProduct.getTextureUrl().lastIndexOf('/') + 1);

            File cachedModel = new File(cacheDir, modelFilename);
            File cachedTexture = new File(cacheDir, textureFilename);

            if (cachedModel.exists() && cachedTexture.exists() &&
                    !hasCacheExpired(cachedModel) && !hasCacheExpired(cachedTexture)) {
                Toast.makeText(getContext(), "Modelo 3D y textura disponibles en caché", Toast.LENGTH_SHORT).show();
                launchARActivity(cachedModel, cachedTexture);
                return;
            }

            DownloadService downloadService = new DownloadService(getContext());

            downloadService.downloadModelAndTexture(
                    currentProduct.getModel3dUrl(),
                    currentProduct.getTextureUrl(),
                    modelFilename,
                    textureFilename,
                    new DownloadService.DownloadCallback() {
                        @Override
                        public void onDownloadComplete(File modelFile, File textureFile) {
                            launchARActivity(modelFile, textureFile);
                        }

                        @Override
                        public void onDownloadFailed(String errorMessage) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Error: Modelo 3D o texturas no disponibles", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean hasCacheExpired(File cachedFile) {
        long lastModified = cachedFile.lastModified();
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastModified) > CACHE_EXPIRATION_TIME;
    }

    private void launchARActivity(File modelFile, File textureFile) {
        Intent arIntent = new Intent(getActivity(), ProductShowARActivity.class);
        arIntent.putExtra("product_id", productId);
        arIntent.putExtra("model_path", modelFile.getAbsolutePath());
        arIntent.putExtra("texture_path", textureFile.getAbsolutePath());
        startActivity(arIntent);
    }
    private void setupRealtimeListener() {
        productListener = productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    currentProduct = product;
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
        productName.setText(product.getName());
        productPrice.setText(String.format("$%.2f", product.getPrice()));
        productDescription.setText(product.getDescription());

        if (product.getRating() != null) {
            // Configurar el RatingBar basado en la calificación
            float rating = product.getRating().floatValue();

            if (rating > 5.0f) {
                productRating.setNumStars((int) Math.ceil(rating));
            } else {
                productRating.setNumStars(5);
            }

            // Establecer la calificación
            productRating.setRating(rating);

            // Configurar el paso (para permitir medias estrellas)
            productRating.setStepSize(0.5f);
        } else {
            //sin rating
            productRating.setNumStars(5);
            productRating.setRating(0);
        }

        if (product.getSpecifications() != null) {
            StringBuilder specsBuilder = new StringBuilder();
            for (String key : product.getSpecifications().keySet()) {
                specsBuilder.append(key).append(": ").append(product.getSpecifications().get(key)).append("\n");
            }
            productSpecs.setText(specsBuilder.toString().trim());
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imagesAdapter = new ProductImagesAdapter(product.getImageUrls());
            imagesRecycler.setAdapter(imagesAdapter);
        }

        if (product.getModel3dUrl() == null || product.getModel3dUrl().isEmpty() ||
                product.getTextureUrl() == null || product.getTextureUrl().isEmpty()) {
            btnViewAR.setVisibility(View.GONE);
        } else {
            btnViewAR.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnViewAR.setOnClickListener(v -> openARModel());
    }

    private void addToCart() {
        if (currentProduct == null) {
            showToast("Error: No se puede agregar al carrito, producto no disponible");
            return;
        }

        // Default quantity is 1, you can modify this if needed
        addToCart(currentProduct, 1);
    }

    private void addToCart(Product product, int quantity) {
        if (getContext() == null || (this instanceof Fragment && !((Fragment) this).isAdded())) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            showLoginRequiredDialog(product, quantity);
            return;
        }

        // Usuario autenticado - proceder con la lógica del carrito
        proceedWithAddToCart(currentUser.getUid(), product, quantity);
    }

    private void showLoginRequiredDialog(Product product, int quantity) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Inicio de sesión requerido")
                .setMessage("Para agregar productos al carrito necesitas iniciar sesión. ¿Deseas iniciar sesión ahora?")
                .setPositiveButton("Iniciar sesión", (dialog, which) -> {
                    // Redirigir a LoginActivity
                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                    loginIntent.putExtra("redirect_to", "cart");
                    loginIntent.putExtra("product_id", product.getProductId()); // Opcional: para agregar después del login
                    loginIntent.putExtra("quantity", quantity); // Opcional: cantidad deseada
                    startActivity(loginIntent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    showToast("Puedes iniciar sesión más tarde desde el menú");
                    dialog.dismiss();
                })
                .setIcon(R.drawable.ic_home)
                .setCancelable(true)
                .show();
    }

    private void proceedWithAddToCart(String userId, Product product, int quantity) {
        // Actualizar contador del carrito
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
                showToast("Producto agregado al carrito");
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
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (productListener != null) {
            productRef.removeEventListener(productListener);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}