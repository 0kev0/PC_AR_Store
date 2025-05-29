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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventarioFragment extends Fragment {
    /*************************************************************VARIABLES******************************************************************************************/
    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int RESULT_OK = Activity.RESULT_OK;
    private AdminProductAdapter adapter;
    private DatabaseReference productsRef;
    private DatabaseReference transactionsRef;
    private final List<Product> productList = new ArrayList<>();
    private final List<String> selectedImageUrls = new ArrayList<>();
    private String currentProductIdForImages;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private ImagesAdapter imagesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);

        // Inicializar Firebase
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        // Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.productsRecycler);
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
        MaterialButton btnAddProduct = view.findViewById(R.id.btnAddProduct);
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


        // Reiniciar las listas de imágenes para un nuevo producto
        selectedImageUris = new ArrayList<>();
        selectedImageUrls.clear();

        imagesAdapter = new ImagesAdapter(getContext(), selectedImageUris);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);

        loadCategories(spinnerCategory);

        currentProductIdForImages = productsRef.push().getKey();

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

                    if (selectedImageUris.isEmpty()) {
                        Toast.makeText(getContext(), "Seleccione al menos una imagen", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (validateProductInput(name, priceStr, costStr, stockStr, category, ratingStr)) {
                        // Primero subir las imágenes y luego agregar el producto
                        uploadImagesAndAddProduct(selectedImageUris, name, priceStr, costStr, stockStr, category, description, ratingStr);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void uploadImagesAndAddProduct(List<Uri> imageUris, String name, String priceStr,
                                          String costStr, String stockStr, String category,
                                          String description, String ratingStr) {
        // Limpiar las URLs de imágenes anteriores
        selectedImageUrls.clear();

        // Contador para saber cuándo hemos subido todas las imágenes
        final int[] uploadCount = {0};
        final int totalImages = imageUris.size();

        for (Uri imageUri : imageUris) {
            uploadImage(imageUri, new ImageUploadCallback() {
                @Override
                public void onUploadComplete() {
                    uploadCount[0]++;
                    // Cuando todas las imágenes están subidas, agregamos el producto
                    if (uploadCount[0] == totalImages) {
                        addProduct(name, priceStr, costStr, stockStr, category, description, ratingStr);
                    }
                }

                @Override
                public void onUploadFailed() {
                    // Si falla alguna imagen, no continuamos
                    Toast.makeText(getContext(), "Error al subir imágenes", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private interface ImageUploadCallback {
        void onUploadComplete();
        void onUploadFailed();
    }

    private void uploadImage(Uri imageUri, ImageUploadCallback callback) {
        if (currentProductIdForImages == null || currentProductIdForImages.isEmpty()) {
            Toast.makeText(getContext(), "Error: No se ha generado ID de producto", Toast.LENGTH_SHORT).show();
            callback.onUploadFailed();
            return;
        }

        String filename = "img_" + System.currentTimeMillis() + ".jpg";

        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("PRODUCTS/" + currentProductIdForImages + "/IMG/" + filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        selectedImageUrls.add(uri.toString());
                        callback.onUploadComplete();
                    }).addOnFailureListener(e -> {
                        callback.onUploadFailed();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onUploadFailed();
                });
    }



    private boolean validateProductInput(String name, String price, String cost, String stock, String category, String rating) {
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

    private void addProduct(String name, String priceStr, String costStr, String stockStr,
                            String category, String description, String ratingStr) {
        try {
            // Parsear valores numéricos
            double price = Double.parseDouble(priceStr);
            double cost = Double.parseDouble(costStr);
            int stock = Integer.parseInt(stockStr);
            double rating = Double.parseDouble(ratingStr);

            // Validar que haya al menos una imagen seleccionada
            if (selectedImageUrls.isEmpty()) {
                Toast.makeText(getContext(), "Debe seleccionar al menos una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear el objeto Producto
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCost(cost);
            product.setStock(stock);
            product.setCategory(category);
            product.setDescription(description);
            product.setRating(rating);
            product.setImageUrls(new ArrayList<>(selectedImageUrls));

            // Usar el ID que ya generamos al seleccionar imágenes
            String productId = currentProductIdForImages;
            if (productId == null || productId.isEmpty()) {
                Toast.makeText(getContext(), "Error: No se generó ID de producto", Toast.LENGTH_SHORT).show();
                return;
            }
            product.setProductId(productId);

            // Guardar el producto en Firebase Database
            productsRef.child(productId).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        // Registrar la transacción de inventario
                        registerInventoryPurchase(product, productId);

                        // Limpiar las variables temporales
                        selectedImageUrls.clear();
                        currentProductIdForImages = null;

                        // Notificar al usuario y actualizar la lista
                        Toast.makeText(getContext(), "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    })
                    .addOnFailureListener(e -> {
                        // Manejar errores
                        Toast.makeText(getContext(), "Error al guardar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", "Error al agregar producto", e);

                        // Opcional: Intentar eliminar las imágenes subidas si falla
                        deleteUploadedImages(productId);
                    });

        } catch (NumberFormatException e) {
            // Manejar errores de formato numérico
            Toast.makeText(getContext(), "Error: Verifique los valores numéricos", Toast.LENGTH_SHORT).show();
            Log.e("NumberFormat", "Error al parsear valores numéricos", e);
        }
    }

    private void deleteUploadedImages(String productId) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("PRODUCTS/" + productId);

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete(); // Eliminar cada imagen
                    }
                    // También eliminar la carpeta IMG si está vacía
                    storageRef.child("IMG").delete();
                })
                .addOnFailureListener(e -> {
                    Log.e("StorageError", "Error al limpiar imágenes fallidas", e);
                });
    }

    private void registerInventoryPurchase(Product product, String productId) {
        String transactionId = transactionsRef.push().getKey();
        double totalCost = product.getCost() * product.getStock();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", totalCost);
        transaction.put("date", System.currentTimeMillis());
        transaction.put("description", "Compra de inventario: " + product.getName());
        transaction.put("status", "completed");
        transaction.put("type", "inventory_purchase");
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
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            imagesAdapter.notifyDataSetChanged();
        }
    }

    private void uploadImage(Uri imageUri) {
        if (currentProductIdForImages == null || currentProductIdForImages.isEmpty()) {
            Toast.makeText(getContext(), "Error: No se ha generado ID de producto", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "img_" + System.currentTimeMillis() + ".jpg";

        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("PRODUCTS/" + currentProductIdForImages + "/IMG/" + filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        selectedImageUrls.add(uri.toString());
                        Toast.makeText(getContext(), "Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        // Initialize views
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etCost = dialogView.findViewById(R.id.etProductCost);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinerCategoria);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etRating = dialogView.findViewById(R.id.etProductRating);
        Button btnSelectImages = dialogView.findViewById(R.id.btnSelectImages);
        RecyclerView rvSelectedImages = dialogView.findViewById(R.id.rvSelectedImages);

        // Set current product values
        etName.setText(product.getName());
        etPrice.setText(String.valueOf(product.getPrice()));
        etCost.setText(String.valueOf(product.getCost()));
        etStock.setText(String.valueOf(product.getStock()));
        spinnerCategory.setText(product.getCategory());
        etDescription.setText(product.getDescription());
        etRating.setText(String.valueOf(product.getRating()));

        // Initialize image handling
        List<Uri> selectedImageUris = new ArrayList<>();
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);

        loadCategories(spinnerCategory);

        btnSelectImages.setOnClickListener(v -> {
            currentProductIdForImages = product.getProductId();
            selectImages();
        });

        builder.setView(dialogView)
                .setTitle("Editar Producto")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String category = spinnerCategory.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String ratingStr = etRating.getText().toString().trim();


                    if (validateProductInput(name, priceStr, costStr, stockStr, category, ratingStr)) {
                        if (!selectedImageUris.isEmpty()) {
                            // If new images were selected, upload them first
                            uploadImagesAndUpdateProduct(selectedImageUris, product.getProductId(),
                                    name, priceStr, costStr, stockStr, category, description, ratingStr);
                        } else {
                            // If no new images, just update with existing URLs
                            updateProduct(product.getProductId(), name, priceStr, costStr, stockStr,
                                    category, description, ratingStr);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void uploadImagesAndUpdateProduct(List<Uri> imageUris, String productId, String name,
                                              String priceStr, String costStr, String stockStr,
                                              String category, String description, String ratingStr) {
        // Clear selectedImageUrls to avoid duplicates
        selectedImageUrls.clear();

        // Keep track of uploads
        final int[] uploadCount = {0};
        final int totalImages = imageUris.size();

        for (Uri imageUri : imageUris) {
            uploadImage(imageUri, new ImageUploadCallback() {
                @Override
                public void onUploadComplete() {
                    uploadCount[0]++;
                    if (uploadCount[0] == totalImages) {
                        // Combine new URLs with existing ones
                        List<String> allImageUrls = new ArrayList<>();
                        allImageUrls.addAll(selectedImageUrls);
                        updateProduct(productId, name, priceStr, costStr, stockStr,
                                category, description, ratingStr);
                    }
                }

                @Override
                public void onUploadFailed() {
                    Toast.makeText(getContext(), "Error al subir imágenes", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProduct(String productId, String name, String priceStr, String costStr,
                               String stockStr, String category, String description,
                               String ratingStr) {
        try {
            // Parse values
            double price = Double.parseDouble(priceStr);
            double cost = Double.parseDouble(costStr);
            int stock = Integer.parseInt(stockStr);
            double rating = Double.parseDouble(ratingStr);


            // Create updated product
            Product updatedProduct = new Product();
            updatedProduct.setProductId(productId);
            updatedProduct.setName(name);
            updatedProduct.setPrice(price);
            updatedProduct.setCost(cost);
            updatedProduct.setStock(stock);
            updatedProduct.setCategory(category);
            updatedProduct.setDescription(description);
            updatedProduct.setRating(rating);

            // Update in database
            productsRef.child(productId).setValue(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Producto actualizado", Toast.LENGTH_SHORT).show();
                        loadProducts();

                        // Clear temporary data
                        selectedImageUrls.clear();
                        selectedImageUris.clear();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UpdateProduct", "Error updating product", e);
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error en valores numéricos", Toast.LENGTH_SHORT).show();
            Log.e("NumberFormat", "Error parsing numeric values", e);
        }
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