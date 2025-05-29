package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;

public class EditProductDialog {
    public interface EditProductDialogListener {
        boolean validateInputs(String name, String price, String stock);
        void updateProduct(String productId, String name, double price, int stock);
    }

    public static void showEditDialog(View view, @NonNull Product product, EditProductDialogListener listener) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_edit_product, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etStock = dialogView.findViewById(R.id.etProductStock);

        etName.setText(product.getName());
        etPrice.setText(String.valueOf(product.getPrice()));
        etStock.setText(String.valueOf(product.getStock()));

        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setTitle("Editar Producto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialogInterface, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();

                    if (listener.validateInputs(name, priceStr, stockStr)) {
                        double price = Double.parseDouble(priceStr);
                        int stock = Integer.parseInt(stockStr);

                        listener.updateProduct(product.getProductId(), name, price, stock);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        etName.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

}