package com.example.pcarstore.helpers;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import android.view.MenuItem;

public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = "FingerprintHelper";
    private final FingerprintAuthListener mListener;
    private MenuItem targetMenuItem;

    public FingerprintHelper(FingerprintAuthListener listener) {
        this.mListener = listener;
    }

    public void startAuth(FingerprintManager fingerprintManager,
                          FingerprintManager.CryptoObject cryptoObject) {
        try {
            // Iniciar nueva autenticación
            fingerprintManager.authenticate(cryptoObject, null, 0, this, null);
            Log.d(TAG, "Autenticación por huella iniciada");
        } catch (SecurityException e) {
            Log.e(TAG, "Error de seguridad en autenticación", e);
            notifyError("Error de permisos. Verifica los permisos de huella digital.");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error de estado en autenticación", e);
            notifyError("El sensor de huella no está disponible actualmente.");
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado en autenticación", e);
            notifyError("Error al iniciar autenticación por huella.");
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Log.d(TAG, "onAuthenticationError: " + errorCode + " - " + errString);
        notifyError(errString.toString());
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Log.d(TAG, "onAuthenticationHelp: " + helpCode + " - " + helpString);
        notifyHelp(helpString.toString());
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Log.d(TAG, "onAuthenticationSucceeded");
        if (mListener != null) {
            mListener.onFingerprintAuthSuccess();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        Log.d(TAG, "onAuthenticationFailed");
        notifyError("Huella digital no reconocida. Intenta nuevamente.");
    }

    private void notifyError(String error) {
        if (mListener != null) {
            mListener.onFingerprintAuthError(error);
        }
    }

    private void notifyHelp(String help) {
        if (mListener != null) {
            if (mListener instanceof FingerprintAuthListenerExtended) {
                ((FingerprintAuthListenerExtended) mListener).onFingerprintAuthHelp(help);
            }
        }
    }

    public void setTargetMenuItem(MenuItem targetItem) {
        this.targetMenuItem = targetItem;
    }


    public interface FingerprintAuthListenerExtended extends FingerprintAuthListener {
        void onFingerprintAuthHelp(String helpMessage);
    }
}