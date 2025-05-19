package com.example.pcarstore.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.AdminProductAdapter;
import com.example.pcarstore.Adapters.ImagesAdapter;
import com.example.pcarstore.Adapters.ProductosAdapter;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventarioFragment extends Fragment implements AdminProductAdapter.OnProductActionsListener {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int RESULT_OK = Activity.RESULT_OK;
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private DatabaseReference productsRef;
    private MaterialButton btnAddProduct;
    private List<Product> productList = new ArrayList<>();

    public InventarioFragment() {
        // Constructor vacío requerido
    }

    public static InventarioFragment newInstance() {
        return new InventarioFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);

        // Inicializar Firebase
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.productsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar adapter
        adapter = new AdminProductAdapter(this);
        recyclerView.setAdapter(adapter);

        //btn para agregra nuevo producto
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());


        // Cargar productos
        loadProducts();

        return view;
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
                Toast.makeText(getContext(), "Error al cargar productos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Product product) {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);

        // Obtener referencias a las vistas
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);

        // Rellenar con los valores actuales
        etName.setText(product.getName());
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));

        // Crear el diálogo
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialogInterface, which) -> {
                    // Validar y guardar cambios
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();

                    if (validateInputs(name, priceStr, stockStr)) {
                        double price = Double.parseDouble(priceStr);
                        int stock = Integer.parseInt(stockStr);

                        updateProduct(product.getProductId(), name, price, stock);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        // Asegurar que el teclado se muestre correctamente
        etName.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private boolean validateInputs(String name, String price, String stock) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un nombre válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                Toast.makeText(getContext(), "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Ingrese un precio válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int stockValue = Integer.parseInt(stock);
            if (stockValue < 0) {
                Toast.makeText(getContext(), "El stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Ingrese un stock válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateProduct(String productId, String name, double price, int stock) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("price", price);
        updates.put("stock", stock);

        productsRef.child(productId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Producto actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditProduct(Product product) {
        showEditDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de eliminar " + product.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteProduct(product.getProductId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void deleteProduct(String productId) {
        productsRef.child(productId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("Producto eliminado");
                })
                .addOnFailureListener(e -> {
                    showToast("Error al eliminar: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar listeners si es necesario
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        // Obtener referencias a las vistas
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etCost = dialogView.findViewById(R.id.etProductCost);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etProductCategory);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etRating = dialogView.findViewById(R.id.etProductRating);
        Button btnSelectImages = dialogView.findViewById(R.id.btnSelectImages);
        RecyclerView rvSelectedImages = dialogView.findViewById(R.id.rvSelectedImages);

        // Configurar RecyclerView para imágenes seleccionadas
        List<String> selectedImageUrls = new ArrayList<>();
        ImagesAdapter imagesAdapter = new ImagesAdapter(selectedImageUrls);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);

        // Seleccionar imágenes
        btnSelectImages.setOnClickListener(v -> selectImages(selectedImageUrls, imagesAdapter));

        // Crear diálogo
        new AlertDialog.Builder(getContext())
                .setTitle("Agregar Nuevo Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String ratingStr = etRating.getText().toString().trim();

                    if (validateProductInput(name, priceStr, costStr, stockStr, category, ratingStr)) {
                        double price = Double.parseDouble(priceStr);
                        double cost = Double.parseDouble(costStr);
                        int stock = Integer.parseInt(stockStr);
                        double rating = Double.parseDouble(ratingStr);

                        // Crear nuevo producto
                        Product newProduct = new Product();
                        newProduct.setName(name);
                        newProduct.setPrice(price);
                        newProduct.setCost(cost);
                        newProduct.setStock(stock);
                        newProduct.setCategory(category);
                        newProduct.setDescription(description);
                        newProduct.setRating(rating);
                        newProduct.setImageUrls(selectedImageUrls);

                        // Subir producto a Firebase
                        addProductToFirebase(newProduct);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void selectImages(List<String> imageUrls, ImagesAdapter adapter) {
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
                // Múltiples imágenes seleccionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImageToFirebase(imageUri);
                }
            } else if (data.getData() != null) {
                // Una sola imagen seleccionada
                Uri imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Generar nombre único para la imagen
        String filename = "product_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("product_images/" + filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Agregar URL a la lista de imágenes del producto
                        // (Implementa esta lógica según tu estructura)
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateProductInput(String name, String priceStr, String costStr, String stockStr,
                                         String category, String ratingStr) {
        if (name.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double cost = Double.parseDouble(costStr);
            if (price <= 0 || cost <= 0 || price < cost) {
                Toast.makeText(getContext(), "Precio y costo deben ser válidos", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                Toast.makeText(getContext(), "Stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Stock debe ser un número entero", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) {
                Toast.makeText(getContext(), "Rating debe estar entre 0 y 5", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Rating debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addProductToFirebase(Product product) {
        DatabaseReference newProductRef = productsRef.push();
        String productId = "prod_" + System.currentTimeMillis();
        product.setProductId(productId);

        newProductRef.setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al agregar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}