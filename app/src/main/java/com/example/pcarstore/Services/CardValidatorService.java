package com.example.pcarstore.Services;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import java.util.Calendar;
import java.util.regex.Pattern;

public class CardValidatorService {

    // Expresiones regulares para validación
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{3,50}$");

    public static class CardValidationResult {
        public boolean isValid;
        public String errorMessage;

        public CardValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Valida todos los datos de la tarjeta
     */
    public static CardValidationResult validateCard(
            String cardNumber,
            String expiryDate,
            String cvv,
            String cardHolderName) {

        // Validar número de tarjeta
        CardValidationResult numberValidation = validateCardNumber(cardNumber);
        if (!numberValidation.isValid) {
            return numberValidation;
        }

        // Validar fecha de expiración
        CardValidationResult expiryValidation = validateExpiryDate(expiryDate);
        if (!expiryValidation.isValid) {
            return expiryValidation;
        }

        // Validar CVV
        CardValidationResult cvvValidation = validateCVV(cvv, cardNumber);
        if (!cvvValidation.isValid) {
            return cvvValidation;
        }

        // Validar nombre del titular
        CardValidationResult nameValidation = validateCardHolderName(cardHolderName);
        if (!nameValidation.isValid) {
            return nameValidation;
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida el número de tarjeta usando el algoritmo de Luhn
     */
    public static CardValidationResult validateCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return new CardValidationResult(false, "El número de tarjeta es requerido");
        }

        // Eliminar espacios y guiones
        String cleanedNumber = cardNumber.replaceAll("[\\s-]+", "");

        if (!CARD_NUMBER_PATTERN.matcher(cleanedNumber).matches()) {
            return new CardValidationResult(false, "Número de tarjeta inválido");
        }

        if (!isValidLuhn(cleanedNumber)) {
            return new CardValidationResult(false, "Número de tarjeta no válido");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida la fecha de expiración (MM/YY)
     */
    public static CardValidationResult validateExpiryDate(String expiryDate) {
        if (TextUtils.isEmpty(expiryDate)) {
            return new CardValidationResult(false, "La fecha de expiración es requerida");
        }

        if (!expiryDate.matches("^(0[1-9]|1[0-2])\\/([0-9]{2})$")) {
            return new CardValidationResult(false, "Formato de fecha inválido (MM/YY)");
        }

        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000; // Asumimos siglo XXI

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1; // Enero es 0

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            return new CardValidationResult(false, "La tarjeta ha expirado");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida el código CVV/CVC
     */
    public static CardValidationResult validateCVV(String cvv, String cardNumber) {
        if (TextUtils.isEmpty(cvv)) {
            return new CardValidationResult(false, "El CVV es requerido");
        }

        if (!CVV_PATTERN.matcher(cvv).matches()) {
            return new CardValidationResult(false, "CVV inválido (3-4 dígitos)");
        }

        // Validación adicional basada en tipo de tarjeta
        String cleanedCardNumber = cardNumber.replaceAll("[\\s-]+", "");
        boolean isAmex = cleanedCardNumber.startsWith("34") || cleanedCardNumber.startsWith("37");

        if (isAmex && cvv.length() != 4) {
            return new CardValidationResult(false, "American Express requiere 4 dígitos CVV");
        } else if (!isAmex && cvv.length() != 3) {
            return new CardValidationResult(false, "CVV debe tener 3 dígitos");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Valida el nombre del titular
     */
    public static CardValidationResult validateCardHolderName(String name) {
        if (TextUtils.isEmpty(name)) {
            return new CardValidationResult(false, "El nombre del titular es requerido");
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            return new CardValidationResult(false, "Nombre inválido (solo letras y espacios)");
        }

        return new CardValidationResult(true, null);
    }

    /**
     * Implementación del algoritmo de Luhn
     */
    private static boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }
}