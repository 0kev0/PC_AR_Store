package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.ImagesAdapter;
import com.example.pcarstore.ModelsDB.Category;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddProductDialog {

    public interface ProductDialogListener {
        void onProductAdded(Product product);
        boolean validateProductInput(String name, String price, String cost, String stock, String category, String rating);
        void selectImages(List<String> selectedImageUrls, ImagesAdapter imagesAdapter);
        void addProductToFirebase(Product product);
    }

    public static void showAddProductDialog(View view, ProductDialogListener listener) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add_product, null);

        // Obtener referencias a las vistas
        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etCost = dialogView.findViewById(R.id.etProductCost);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinerCategoria);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etProductDescription);
        TextInputEditText etRating = dialogView.findViewById(R.id.etProductRating);
        Button btnSelectImages = dialogView.findViewById(R.id.btnSelectImages);
        RecyclerView rvSelectedImages = dialogView.findViewById(R.id.rvSelectedImages);

        // Configurar RecyclerView para imágenes seleccionadas
        List<String> selectedImageUrls = new ArrayList<>();
        ImagesAdapter imagesAdapter = new ImagesAdapter(selectedImageUrls);
        rvSelectedImages.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imagesAdapter);

        // Cargar categorías desde Firebase
        loadCategoriesFromFirebase(view, spinnerCategory);

        // Seleccionar imágenes
        btnSelectImages.setOnClickListener(v -> listener.selectImages(selectedImageUrls, imagesAdapter));

        // Crear diálogo
        new AlertDialog.Builder(view.getContext())
                .setTitle("Agregar Nuevo Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String category = spinnerCategory.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String ratingStr = etRating.getText().toString().trim();

                    if (listener.validateProductInput(name, priceStr, costStr, stockStr, category, ratingStr)) {
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
                        listener.addProductToFirebase(newProduct);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static void loadCategoriesFromFirebase(View view, AutoCompleteTextView spinnerCategory) {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> categories = new ArrayList<>();

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null && category.getName() != null) {
                        categories.add(category.getName());
                    }
                }

                if (categories.isEmpty()) {
                    categories.add("Sin categorías disponibles");
                    Toast.makeText(view.getContext(), "No se encontraron categorías", Toast.LENGTH_SHORT).show();
                }

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        view.getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categories);
                spinnerCategory.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(view.getContext(), "Error al cargar categorías: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Agregar un valor por defecto
                List<String> defaultCategories = new ArrayList<>();
                defaultCategories.add("Seleccione categoría");

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        view.getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        defaultCategories);
                spinnerCategory.setAdapter(categoryAdapter);
            }
        });
    }
}