package com.example.pcarstore.helpers;

public interface FingerprintAuthListener {
    void onFingerprintAuthSuccess();
    void onFingerprintAuthError(String error);
}
