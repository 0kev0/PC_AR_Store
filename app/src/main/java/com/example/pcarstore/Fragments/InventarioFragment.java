package com.example.pcarstore.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class InventarioFragment extends Fragment implements AdminProductAdapter.OnProductActionsListener,
        AddProductDialog.ProductDialogListener,EditProductDialog.EditProductDialogListener {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int RESULT_OK = Activity.RESULT_OK;
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private DatabaseReference productsRef;
    private MaterialButton btnAddProduct;
    private List<Product> productList = new ArrayList<>();
    private List<String> selectedImageUrls = new ArrayList<>();

    public InventarioFragment() {
        // Constructor vacío requerido
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

        //btn para agregar nuevo producto
        btnAddProduct = view.findViewById(R.id.btnAddProduct);
        btnAddProduct.setOnClickListener(v -> AddProductDialog.showAddProductDialog(view, this));

        // Cargar productos
        loadProducts();

        return view;
    }

    @Override
    public void onProductAdded(Product product) {
        loadProducts();

    }

    // Implementación de ProductDialogListener
    @Override
    public boolean validateProductInput(String name, String price, String cost, String stock, String category, String rating) {
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
            Toast.makeText(getContext(), "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int stockValue = Integer.parseInt(stock);
            if (stockValue < 0) {
                Toast.makeText(getContext(), "Stock no puede ser negativo", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Stock debe ser un número entero", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double ratingValue = Double.parseDouble(rating);
            if (ratingValue < 0 || ratingValue > 5) {
                Toast.makeText(getContext(), "Rating debe estar entre 0 y 5", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Rating debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void selectImages(List<String> selectedImageUrls, ImagesAdapter imagesAdapter) {
        this.selectedImageUrls = selectedImageUrls;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void addProductToFirebase(Product product) {
        DatabaseReference newProductRef = productsRef.push();
        String productId = "prod_" + System.currentTimeMillis();
        product.setProductId(productId);

        newProductRef.setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                    selectedImageUrls.clear();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al agregar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
                        selectedImageUrls.add(imageUrl);
                        // Notificar al adapter si es necesario
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Resto de los métodos permanecen igual...
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

    // Implementación de los métodos de EditProductDialogListener
    @Override
    public boolean validateInputs(String name, String price, String stock) {
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
    @Override
    public void updateProduct(String productId, String name, double price, int stock) {
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
        EditProductDialog.showEditDialog(getView(), product, this);
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
}