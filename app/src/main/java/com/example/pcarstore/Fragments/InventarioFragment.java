package com.example.pcarstore.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.AdminProductAdapter;
import com.example.pcarstore.Adapters.ImagesAdapter;
import com.example.pcarstore.Dialogs.AddProductDialog;
import com.example.pcarstore.Dialogs.EditProductDialog;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventarioFragment extends Fragment {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int RESULT_OK = Activity.RESULT_OK;
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private DatabaseReference productsRef;
    private DatabaseReference transactionsRef;
    private MaterialButton btnAddProduct;
    private List<Product> productList = new ArrayList<>();
    private List<String> selectedImageUrls = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);

        // Inicializar Firebase
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.productsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configurar adaptador con listeners internos
        adapter = new AdminProductAdapter(new AdminProductAdapter.OnProductActionsListener() {
            @Override
            public void onEditProduct(Product product) {
                showEditDialog(product);
            }

            @Override
            public void onDeleteProduct(Product product) {
                showDeleteConfirmation(product);
            }
        });
        recyclerView.setAdapter(adapter);

        // Botón para agregar producto
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());

        loadProducts();
        return view;
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        // Configurar vistas del diálogo
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etCost = dialogView.findViewById(R.id.etProductCost);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinerCategoria);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etRating = dialogView.findViewById(R.id.etProductRating);
        Button btnSelectImages = dialogView.findViewById(R.id.btnSelectImages);
        RecyclerView rvSelectedImages = dialogView.findViewById(R.id.rvSelectedImages);

        // Configurar adaptador de imágenes
        ImagesAdapter imagesAdapter = new ImagesAdapter(selectedImageUrls);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);

        // Cargar categorías
        loadCategories(spinnerCategory);

        // Seleccionar imágenes
        btnSelectImages.setOnClickListener(v -> selectImages());

        builder.setView(dialogView)
                .setTitle("Agregar Producto")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String category = spinnerCategory.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String ratingStr = etRating.getText().toString().trim();

                    if (validateProductInput(name, priceStr, costStr, stockStr, category, ratingStr)) {
                        addProduct(name, priceStr, costStr, stockStr, category, description, ratingStr);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean validateProductInput(String name, String price, String cost,
                                         String stock, String category, String rating) {
        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double priceValue = Double.parseDouble(price);
            double costValue = Double.parseDouble(cost);
            if (priceValue <= 0 || costValue <= 0 || priceValue < costValue) {
                Toast.makeText(getContext(), "Precio y costo deben ser válidos", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valores numéricos inválidos", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int stockValue = Integer.parseInt(stock);
            if (stockValue < 0) {
                Toast.makeText(getContext(), "Stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Stock debe ser entero", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addProduct(String name, String priceStr, String costStr,
                            String stockStr, String category,
                            String description, String ratingStr) {
        try {
            double price = Double.parseDouble(priceStr);
            double cost = Double.parseDouble(costStr);
            int stock = Integer.parseInt(stockStr);
            double rating = Double.parseDouble(ratingStr);

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCost(cost);
            product.setStock(stock);
            product.setCategory(category);
            product.setDescription(description);
            product.setRating(rating);
            product.setImageUrls(new ArrayList<>(selectedImageUrls));

            // Guardar producto
            String productId = productsRef.push().getKey();
            product.setProductId(productId);

            productsRef.child(productId).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        // Registrar transacción como egreso
                        registerInventoryPurchase(product, productId);
                        selectedImageUrls.clear();
                        Toast.makeText(getContext(), "Producto agregado", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error en valores numéricos", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerInventoryPurchase(Product product, String productId) {
        String transactionId = transactionsRef.push().getKey();
        double totalCost = product.getCost() * product.getStock();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", totalCost);
        transaction.put("date", System.currentTimeMillis());
        transaction.put("description", "Compra de inventario: " + product.getName());
        transaction.put("status", "completed");
        transaction.put("type", "inventory_purchase"); // Tipo para identificar como egreso
        transaction.put("productId", productId);
        transaction.put("quantity", product.getStock());
        transaction.put("totalCost", totalCost);

        if (transactionId != null) {
            transactionsRef.child(transactionId).setValue(transaction)
                    .addOnFailureListener(e -> {
                        Log.e("Transaction", "Error al registrar transacción", e);
                    });
        }
    }

    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uploadImage(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                uploadImage(data.getData());
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        String filename = "product_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("product_images/" + filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        selectedImageUrls.add(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCategories(AutoCompleteTextView spinner) {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.child("name").getValue(String.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categories
                );
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar categorías", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Product product) {
        // Implementación de diálogo de edición similar al de agregar
        // ...
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Producto")
                .setMessage("¿Eliminar " + product.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteProduct(product.getProductId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteProduct(String productId) {
        productsRef.child(productId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Producto eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(productSnapshot.getKey());
                        productList.add(product);
                    }
                }
                adapter.setProducts(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}