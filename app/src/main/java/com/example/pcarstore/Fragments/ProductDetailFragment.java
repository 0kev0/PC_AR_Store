package com.example.pcarstore.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ProductDetailFragment extends Fragment {

    private static final String TAG = "ProductDetailFragment";
    private String productId;
    private DatabaseReference productRef;
    private ValueEventListener productListener;
    private Product currentProduct;
    private FirebaseStorage storage;
    private TextView productName, productPrice, productSpecs, productDescription;
    private RatingBar productRating;
    private RecyclerView imagesRecycler;
    private Button btnAddToCart, btnViewAR;
    private ProductImagesAdapter imagesAdapter;
    private InicioActivity inicioActivity;
    private Context context;



    // Modelo 3D y textura
    private File modelFile;
    private File textureFile;
    private boolean modelLoaded = false;
    private boolean textureLoaded = false;
    private ProgressDialog progressDialog;


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

        // Configure RecyclerView
        imagesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void openARModel() {
        // Verificar si tenemos un producto cargado
        if (currentProduct != null && currentProduct.getModel3dUrl() != null && currentProduct.getTextureUrl() != null) {
            progressDialog.show();

            //Iniciar descarga de archivos
            downloadFiles(currentProduct.getModel3dUrl(), currentProduct.getTextureUrl());
        } else {
            Toast.makeText(getContext(), "Error: Modelo 3D o texturas no disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetWorldReadable")
    private void downloadFiles(String modelUrl, String textureUrl) {
        modelLoaded = false;
        textureLoaded = false;

        StorageReference modelRef = storage.getReferenceFromUrl(modelUrl);
        StorageReference textureRef = storage.getReferenceFromUrl(textureUrl);

        try {
            // archivos para AR
            modelFile = File.createTempFile("model_", ".obj", getContext().getCacheDir());
            textureFile = File.createTempFile("texture_", ".png", getContext().getCacheDir());

            modelRef.getFile(modelFile).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Model downloaded to: " + modelFile.getAbsolutePath());
                if (modelFile.setReadable(true, false)) {
                    Log.d(TAG, "Model file permissions set successfully");
                } else {
                    Log.w(TAG, "Failed to set model file permissions");
                }
                modelLoaded = true;
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error downloading model: " + exception.getMessage());
                Toast.makeText(getContext(), "Error al descargar el modelo 3D", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });

            textureRef.getFile(textureFile).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Texture downloaded to: " + textureFile.getAbsolutePath());
                if (textureFile.setReadable(true, false)) {
                    Log.d(TAG, "Texture file permissions set successfully");
                } else {
                    Log.w(TAG, "Failed to set texture file permissions");
                }
                textureLoaded = true;
                checkIfFilesAreReady();
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error downloading texture: " + exception.getMessage());
                Toast.makeText(getContext(), "Error al descargar la textura", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });

        } catch (IOException e) {
            Log.e(TAG, "Error creating temp files: " + e.getMessage());
            Toast.makeText(getContext(), "Error al crear archivos temporales", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private void checkIfFilesAreReady() {
        if (modelLoaded && textureLoaded && modelFile.exists() && textureFile.exists()) {
            progressDialog.dismiss();

            // Verificación adicional con Toast
            String verificationMsg = "Archivos listos:\n" +
                    "Modelo: " + modelFile.getAbsolutePath() + "\n" +
                    "Textura: " + textureFile.getAbsolutePath() + "\n" +
                    "Permisos Modelo: " + (modelFile.canRead() ? "LEÍBLE" : "NO LEÍBLE") + "\n" +
                    "Permisos Textura: " + (textureFile.canRead() ? "LEÍBLE" : "NO LEÍBLE");
            Log.d(TAG, verificationMsg);

            Intent arIntent = new Intent(getActivity(), ProductShowARActivity.class);
            arIntent.putExtra("product_id", productId);
            arIntent.putExtra("model_path", modelFile.getAbsolutePath());
            arIntent.putExtra("texture_path", textureFile.getAbsolutePath());
            startActivity(arIntent);
        } else {
            String errorMsg = "Error: Archivos no disponibles\n" +
                    "Modelo: " + (modelFile != null ? modelFile.exists() : "null") + "\n" +
                    "Textura: " + (textureFile != null ? textureFile.exists() : "null");

            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
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
            productRating.setRating(product.getRating().floatValue());
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

    // Fixed method - No arguments version that uses currentProduct
    private void addToCart() {
        if (currentProduct == null) {
            showToast("Error: No se puede agregar al carrito, producto no disponible");
            return;
        }

        // Default quantity is 1, you can modify this if needed
        addToCart(currentProduct, 1);
    }

    private void addToCart(Product product, int quantity) {
        // Verificar primero si el fragment/activity está disponible
        if (getContext() == null || (this instanceof Fragment && !((Fragment) this).isAdded())) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Si el usuario no está autenticado, mostrar diálogo de confirmación
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

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
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