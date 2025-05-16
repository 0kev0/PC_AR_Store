package com.example.pcarstore.Services;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationService {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordSecure(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return Pattern.compile(passwordRegex).matcher(password).matches();
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) &&
                !name.matches(".*\\d.*") &&
                name.length() >= 3;
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) &&
                password.equals(confirmPassword);
    }
}