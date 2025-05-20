package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class CreditCardPaymentDialog extends DialogFragment {

    public interface CreditCardPaymentListener {
        void onPaymentConfirmed(double amount);
        void onPaymentCancelled();
    }

    private CreditCardPaymentListener listener;
    private double initialAmount;

    public static CreditCardPaymentDialog newInstance(double amount) {
        CreditCardPaymentDialog dialog = new CreditCardPaymentDialog();
        Bundle args = new Bundle();
        args.putDouble("amount", amount);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialAmount = getArguments().getDouble("amount", 0.0);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CreditCardPaymentListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent fragment must implement CreditCardPaymentListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_credit_card_payment, null);

        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", initialAmount));

        builder.setView(dialogView)
                .setTitle("Agregar saldo con tarjeta")
                .setPositiveButton("Confirmar pago", (dialog, which) -> {
                    try {
                        double amount = Double.parseDouble(etAmount.getText().toString());
                        if (amount > 0) {
                            listener.onPaymentConfirmed(amount);
                        } else {
                            showError("La cantidad debe ser mayor a 0");
                        }
                    } catch (NumberFormatException e) {
                        showError("Cantidad inválida");
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    listener.onPaymentCancelled();
                });

        return builder.create();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}