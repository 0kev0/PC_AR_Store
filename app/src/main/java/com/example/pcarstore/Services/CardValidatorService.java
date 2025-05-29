package com.example.pcarstore.Services;

import android.text.TextUtils;
import java.util.Calendar;

public class CardValidatorService {

    public static class CardValidationResult {
        public boolean isValid;
        public String errorMessage;

        public CardValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Valida todos los datos de la tarjeta (versión simplificada)
     */
    public static CardValidationResult validateCard(
            String cardNumber,
            String expiryDate,
            String cvv,
            String cardHolderName) {


        if (TextUtils.isEmpty(cardNumber) || cardNumber.replaceAll("[^0-9]", "").length() != 16) {
            return new CardValidationResult(false, "El número de tarjeta debe tener 16 dígitos");
        }

        CardValidationResult expiryValidation = validateExpiryDate(expiryDate);
        if (!expiryValidation.isValid) {
            return expiryValidation;
        }

        if (TextUtils.isEmpty(cvv) || !cvv.matches("^[0-9]{3,4}$")) {
            return new CardValidationResult(false, "CVV inválido (3-4 dígitos)");
        }


        if (TextUtils.isEmpty(cardHolderName)) {
            return new CardValidationResult(false, "El nombre del titular es requerido");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida solo el número de tarjeta (16 dígitos)
     */
    public static CardValidationResult validateCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber) || cardNumber.replaceAll("[^0-9]", "").length() != 16) {
            return new CardValidationResult(false, "El número de tarjeta debe tener 16 dígitos");
        }
        return new CardValidationResult(true, null);
    }

    /**
     * Valida solo la fecha de expiración (versión más laxa)
     */
    public static CardValidationResult validateExpiryDate(String expiryDate) {
        if (TextUtils.isEmpty(expiryDate)) {
            return new CardValidationResult(false, "La fecha de expiración es requerida");
        }

        String cleanDate = expiryDate.replaceAll("[^0-9]", "");

        if (cleanDate.length() != 4) {
            return new CardValidationResult(false, "Formato de fecha inválido (necesita 4 dígitos)");
        }

        try {
            int month = Integer.parseInt(cleanDate.substring(0, 2));
            int year = Integer.parseInt(cleanDate.substring(2, 4));

            if (month < 1 || month > 12) {
                return new CardValidationResult(false, "Mes inválido (debe ser 01-12)");
            }

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR) % 100;
            int currentMonth = calendar.get(Calendar.MONTH) + 1;

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return new CardValidationResult(false, "La tarjeta ha expirado");
            }

        } catch (NumberFormatException e) {
            return new CardValidationResult(false, "Fecha de expiración inválida");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida solo el CVV (3-4 dígitos)
     */
    public static CardValidationResult validateCVV(String cvv) {
        if (TextUtils.isEmpty(cvv) || !cvv.matches("^[0-9]{3,4}$")) {
            return new CardValidationResult(false, "CVV inválido (3-4 dígitos)");
        }
        return new CardValidationResult(true, null);
    }

    /**
     * Valida solo el nombre del titular (no vacío)
     */
    public static CardValidationResult validateCardHolderName(String name) {
        if (TextUtils.isEmpty(name)) {
            return new CardValidationResult(false, "El nombre del titular es requerido");
        }
        return new CardValidationResult(true, null);
    }
}