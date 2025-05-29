package com.example.pcarstore.Services;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pcarstore.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Locale;

public class DownloadService {
    /*************************************************************VARIABLES******************************************************************************************/
    private static final String TAG = "DownloadService";
    private final Context context;
    private final FirebaseStorage storage;
    private File modelFile;
    private File textureFile;
    private boolean modelLoaded = false;
    private boolean textureLoaded = false;
    private TextView tvProgressMessage;
    private TextView tvProgressPercent;
    private TextView tvFileSize;
    private ProgressBar progressBar;
    private Dialog progressDialog;

    public interface DownloadCallback {
        void onDownloadComplete(File modelFile, File textureFile);
        void onDownloadFailed(String errorMessage);
    }

    public DownloadService(Context context) {
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
        initProgressDialog();
    }
    private void initProgressDialog() {
        // Inflar el layout personalizado
        // Views para el diálogo personalizado
        View progressView = LayoutInflater.from(context).inflate(R.layout.custom_progress_layout, null);

        // Configurar vistas
        tvProgressMessage = progressView.findViewById(R.id.tvProgressMessage);
        tvProgressPercent = progressView.findViewById(R.id.tvProgressPercent);
        tvFileSize = progressView.findViewById(R.id.tvFileSize);
        progressBar = progressView.findViewById(R.id.progressBar);
        Button btnCancel = progressView.findViewById(R.id.btnCancel);

        // Configurar el diálogo
        progressDialog = new Dialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(progressView);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Configurar botón de cancelar
        btnCancel.setOnClickListener(v -> {
            progressDialog.dismiss();
            // Aquí puedes agregar lógica para cancelar las descargas si es necesario
        });
    }
    public void downloadModelAndTexture(String modelUrl, String textureUrl, String modelFilename, String textureFilename, DownloadCallback callback) {
        showProgress("Preparando descargas...", 0, null);

        // Crear archivos con nombres específicos en el directorio de caché
        File cacheDir = context.getCacheDir();
        this.modelFile = new File(cacheDir, modelFilename);
        this.textureFile = new File(cacheDir, textureFilename);

        StorageReference modelRef = storage.getReferenceFromUrl(modelUrl);
        StorageReference textureRef = storage.getReferenceFromUrl(textureUrl);

        // Obtener metadatos para los tamaños
        modelRef.getMetadata().addOnSuccessListener(modelMetadata -> {
            long modelSize = modelMetadata.getSizeBytes();

            textureRef.getMetadata().addOnSuccessListener(textureMetadata -> {
                long textureSize = textureMetadata.getSizeBytes();
                startDownloads(modelRef, textureRef, modelSize, textureSize, callback);
            }).addOnFailureListener(e -> {
                Log.w(TAG, "No se pudo obtener tamaño de textura", e);
                startDownloads(modelRef, textureRef, -1, -1, callback);
            });
        }).addOnFailureListener(e -> {
            Log.w(TAG, "No se pudo obtener tamaño de modelo", e);
            startDownloads(modelRef, textureRef, -1, -1, callback);
        });
    }
    private void startDownloads(StorageReference modelRef, StorageReference textureRef, long modelSize, long textureSize, DownloadCallback callback) {
        // Eliminar la creación de archivos temporales ya que ahora los recibimos como parámetro
        String modelMsg = "Descargando modelo 3D";
        showProgress(modelMsg, 0, modelSize > 0 ? "Tamaño: " + formatFileSize(modelSize) : null);

        downloadModel(modelRef, modelSize, () -> {
            String textureMsg = "Descargando texturas";
            showProgress(textureMsg, 0, textureSize > 0 ? "Tamaño: " + formatFileSize(textureSize) : null);

            downloadTexture(textureRef, textureSize, () -> {
                showProgress("Descargas completadas", 100, null);
                checkFilesReady(callback);
            });
        });
    }
    private void downloadModel(StorageReference modelRef, long modelSize, Runnable onSuccess) {
        modelRef.getFile(modelFile).addOnProgressListener(taskSnapshot -> {
            if (modelSize > 0) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / modelSize);
                String fileSizeInfo = String.format(Locale.getDefault(),
                        "%s / %s",
                        formatFileSize(taskSnapshot.getBytesTransferred()),
                        formatFileSize(modelSize));

                updateProgress("Descargando modelo 3D", progress, fileSizeInfo);
            }
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Model downloaded: " + modelFile.getAbsolutePath());
            if (modelFile.setReadable(true, false)) {
                Log.d(TAG, "Model file permissions set");
            }
            modelLoaded = true;
            onSuccess.run();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error downloading model", e);
            dismissProgress();
            Toast.makeText(context, "Error al descargar el modelo 3D", Toast.LENGTH_SHORT).show();
        });
    }
    private void downloadTexture(StorageReference textureRef, long textureSize, Runnable onSuccess) {
        textureRef.getFile(textureFile).addOnProgressListener(taskSnapshot -> {
            if (textureSize > 0) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / textureSize);
                String fileSizeInfo = String.format(Locale.getDefault(),
                        "%s / %s",
                        formatFileSize(taskSnapshot.getBytesTransferred()),
                        formatFileSize(textureSize));

                updateProgress("Descargando texturas", progress, fileSizeInfo);
            }
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Texture downloaded: " + textureFile.getAbsolutePath());
            if (textureFile.setReadable(true, false)) {
                Log.d(TAG, "Texture file permissions set");
            }
            textureLoaded = true;
            onSuccess.run();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error downloading texture", e);
            dismissProgress();
            Toast.makeText(context, "Error al descargar la textura", Toast.LENGTH_SHORT).show();
        });
    }
    private void checkFilesReady(DownloadCallback callback) {
        new Handler().postDelayed(() -> {
            dismissProgress();

            if (modelLoaded && textureLoaded && modelFile.exists() && textureFile.exists()) {
                Log.d(TAG, "Archivos listos:\nModelo: " + modelFile.getAbsolutePath() +
                        "\nTextura: " + textureFile.getAbsolutePath());
                callback.onDownloadComplete(modelFile, textureFile);
            } else {
                callback.onDownloadFailed("Error: Archivos no disponibles");
            }
        }, 500);
    }
    private void showProgress(String message, int progress, String fileSizeInfo) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        updateProgress(message, progress, fileSizeInfo);
    }
    private void updateProgress(String message, int progress, String fileSizeInfo) {
        if (progressDialog != null && progressDialog.isShowing()) {
            tvProgressMessage.setText(message);
            progressBar.setProgress(progress);
            tvProgressPercent.setText(progress + "%");

            if (fileSizeInfo != null) {
                tvFileSize.setText(fileSizeInfo);
                tvFileSize.setVisibility(View.VISIBLE);
            } else {
                tvFileSize.setVisibility(View.GONE);
            }
        }
    }
    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format(Locale.getDefault(), "%.1f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}