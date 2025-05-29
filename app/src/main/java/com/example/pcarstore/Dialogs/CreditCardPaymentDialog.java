package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pcarstore.R;
import com.example.pcarstore.Services.CardValidatorService;
import com.example.pcarstore.Services.CardValidatorService.CardValidationResult;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class CreditCardPaymentDialog extends DialogFragment {
    public interface CreditCardPaymentListener {
        void onPaymentConfirmed(String cardName, String cardNumber, String expiry, String cvv, double amount);
        void onPaymentCancelled();
    }

    public void setListener(CreditCardPaymentListener listener) {
        this.listener = listener;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_credit_card_payment, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialAmount = getArguments().getDouble("amount", 0.0);
        }

        // Intenta obtener el listener del Fragment padre si no se ha asignado manualmente
        if (listener == null && getParentFragment() instanceof CreditCardPaymentListener) {
            listener = (CreditCardPaymentListener) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Previene memory leaks
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CreditCardPaymentListener) {
            listener = (CreditCardPaymentListener) context;
        } else if (getParentFragment() instanceof CreditCardPaymentListener) {
            listener = (CreditCardPaymentListener) getParentFragment();
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement CreditCardPaymentListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_credit_card_payment, null);

        TextInputEditText etCardName = dialogView.findViewById(R.id.etCardName);
        TextInputEditText etCardNumber = dialogView.findViewById(R.id.etCardNumber);
        TextInputEditText etExpiry = dialogView.findViewById(R.id.etExpiry);
        TextInputEditText etCvv = dialogView.findViewById(R.id.etCvv);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelRecarga);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmRecarga);

        // Format card number as user types
        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String original = s.toString().replaceAll(" ", "");
                if (!original.isEmpty() && original.length() <= 16) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < original.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ");
                        }
                        formatted.append(original.charAt(i));
                    }
                    if (!original.equals(s.toString())) {
                        etCardNumber.removeTextChangedListener(this);
                        etCardNumber.setText(formatted.toString());
                        etCardNumber.setSelection(formatted.length());
                        etCardNumber.addTextChangedListener(this);
                    }
                }
            }
        });

        // Format expiry date as user types
        etExpiry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String original = s.toString().replaceAll("/", "");
                if (!original.isEmpty() && original.length() <= 4) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < original.length(); i++) {
                        if (i == 2) {
                            formatted.append("/");
                        }
                        formatted.append(original.charAt(i));
                    }
                    if (!original.equals(s.toString())) {
                        etExpiry.removeTextChangedListener(this);
                        etExpiry.setText(formatted.toString());
                        etExpiry.setSelection(formatted.length());
                        etExpiry.addTextChangedListener(this);
                    }
                }
            }
        });

        etAmount.setText(String.format(Locale.getDefault(), "%.2f", initialAmount));

        // Configurar el botón Cancelar
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentCancelled();
            }
            dismiss();
        });

        // Configurar el botón Confirmar
        btnConfirm.setOnClickListener(v -> {
            String cardName = etCardName.getText().toString().trim();
            String cardNumber = etCardNumber.getText().toString().replaceAll(" ", "");
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            // Validación de tarjeta
            CardValidationResult cardValidation = CardValidatorService.validateCard(
                    cardNumber, expiry, cvv, cardName);

            if (!cardValidation.isValid) {
                showError(cardValidation.errorMessage);
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showError("La cantidad debe ser mayor a 0");
                    return;
                }

                // All good
                listener.onPaymentConfirmed(cardName, cardNumber, expiry, cvv, amount);
                dismiss();
            } catch (NumberFormatException e) {
                showError("Cantidad inválida");
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}