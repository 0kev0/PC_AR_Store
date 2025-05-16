package com.example.pcarstore.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.Activities.OrdersActivity;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.app.Activity.RESULT_OK;
//import static android.os.Build.VERSION_CODES.R;

import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class PerfilFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    private ImageView ivProfilePicture;
    private TextView tvUserName, tvUserEmail,tvUserBalance;
    private Button btnEditProfile, btnLogout;
    private Button btnOrders, btnWishlist, btnGifCard, btnSettings;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Inicializar vistas
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnOrders = view.findViewById(R.id.btnOrders);
        btnWishlist = view.findViewById(R.id.btnWishlist);
        btnGifCard = view.findViewById(R.id.btnGifCard);
        btnSettings = view.findViewById(R.id.btnSettings);
        tvUserBalance =  view.findViewById(R.id.tvUserBalance);

        loadUserData();

        // Configurar listeners
        btnEditProfile.setOnClickListener(v -> editProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnOrders.setOnClickListener(v -> showOrders());
        btnWishlist.setOnClickListener(v -> showWishlist());
        btnGifCard.setOnClickListener(v -> showGifCard());
        btnSettings.setOnClickListener(v -> showSettings());

        return view;
    }

    private void loadUserData() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Mostrar información básica del usuario de Firebase
            tvUserName.setText(currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : "Usuario");
            tvUserEmail.setText(currentUser.getEmail() != null ?
                    currentUser.getEmail() : "No especificado");

            // Cargar imagen de perfil desde Firebase Auth
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(requireContext())
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_account_circle)
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_account_circle);
            }

            // Cargar datos adicionales desde Realtime Database
            loadUserDetailsFromDatabase(currentUser.getUid());
        } else {
            // Modo invitado
            tvUserName.setText("Invitado");
            tvUserEmail.setText("No has iniciado sesión");
            ivProfilePicture.setImageResource(R.drawable.ic_account_circle);
            tvUserBalance.setText("S/ 0.00");
            tvUserBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        }
    }

    private void loadUserDetailsFromDatabase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Cargar datos básicos que pueden estar en diferentes nodos
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    // Actualizar UI con datos básicos si es necesario
                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText(name);
                    }

                    // Cargar y mostrar saldo
                    Double saldo = snapshot.child("saldo").getValue(Double.class);
                    if (saldo != null) {
                        tvUserBalance.setText(String.format("S/ %.2f", saldo));
                        int colorId = saldo < 20.0 ? R.color.red : R.color.green;
                        tvUserBalance.setTextColor(ContextCompat.getColor(requireContext(), colorId));
                    }

                    // Cargar y mostrar membresía Prime
                    Boolean membresiaPrime = snapshot.child("membresiaPrime").getValue(Boolean.class);
                    if (membresiaPrime != null) {
                        // Puedes mostrar un icono o texto indicando el estado
                        if (membresiaPrime) {
                            // Mostrar que tiene membresía Prime
                        }
                    }

                    // Cargar datos de ubicación si existen
                    String departamento = snapshot.child("departamento").getValue(String.class);
                    String ciudad = snapshot.child("ciudad").getValue(String.class);

                    // Cargar imagen de perfil personalizada si existe
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImageUrl)
                                .circleCrop()
                                .into(ivProfilePicture);
                    }
                } else {
                    Log.d("PerfilFragment", "No se encontraron datos adicionales del usuario");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PerfilFragment", "Error al cargar detalles del usuario", error.toException());
                tvUserBalance.setText("S/ --");
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showOrders() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getActivity(), OrdersActivity.class));
        } else {
            Toast.makeText(getContext(),
                    "Debes iniciar sesión para ver tus pedidos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showWishlist() {
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(getContext(),
                    "Mostrando lista de deseos",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(),
                    "Debes iniciar sesión para ver tus pedidos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showGifCard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_discount, null);
        EditText etCode = dialogView.findViewById(R.id.etDiscountCode);
        Button btnRedeem = dialogView.findViewById(R.id.btnRedeem);

        builder.setView(dialogView)
                .setTitle("Canjear Descuento")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        btnRedeem.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if(validateDiscountCode(code)) {
                applyDiscount(code);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Código inválido", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private boolean validateDiscountCode(String code) {
        return code != null && code.matches("GIFCARD\\d{4}");
    }

    private void applyDiscount(String code) {
        double discountPercentage = 0.15;
        Toast.makeText(requireContext(), "Descuento aplicado: 15%", Toast.LENGTH_SHORT).show();
    }

    private void showSettings() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Configuración de cuenta");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_settings, null);

        // Configurar elementos
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> editProfile());
        view.findViewById(R.id.btn_payment_methods).setOnClickListener(v -> showPaymentMethods());

        // Configurar switch de notificaciones
        SwitchMaterial switchNotifications = view.findViewById(R.id.switch_notifications);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                enableNotifications();
            } else {
                disableNotifications();
            }
        });

        // Configurar switch de tema oscuro
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode_enabled", false);
        switchDarkMode.setChecked(isDarkMode);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode_enabled", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            requireActivity().recreate();
        });

        builder.setView(view)
                .setNegativeButton("Cerrar", null)
                .setNeutralButton("Cerrar sesión", (d, w) -> logout());

        builder.show();
    }

    private void showPaymentMethods() {
        // Implementar lógica para mostrar métodos de pago
        Toast.makeText(getContext(), "Mostrar métodos de pago", Toast.LENGTH_SHORT).show();
    }

    private void enableNotifications() {
        Toast.makeText(requireContext(), "Notificaciones activadas", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        Toast.makeText(requireContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
    }

    private void editProfile() {
        if (mAuth.getCurrentUser() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            TextInputEditText etName = dialogView.findViewById(R.id.etName);
            Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnSave = dialogView.findViewById(R.id.btnSave);

            if (currentUser != null) {
                etName.setText(currentUser.getDisplayName());
            }

            btnSelectImage.setOnClickListener(v -> openImageChooser());
            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnSave.setOnClickListener(v -> {
                String newName = etName.getText().toString().trim();
                if (newName.isEmpty()) {
                    etName.setError("El nombre no puede estar vacío");
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Actualizando perfil...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                if (imageUri != null) {
                    uploadImageAndUpdateProfile(newName, progressDialog, dialog);
                } else {
                    updateProfileWithoutImage(newName, progressDialog, dialog);
                }
            });
        } else {
            Toast.makeText(getContext(),
                    "Debes iniciar sesión para ver tus pedidos",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Toast.makeText(getContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndUpdateProfile(String newName, ProgressDialog progressDialog, AlertDialog dialog) {
        if (currentUser == null || imageUri == null) return;

        StorageReference fileReference = storageRef.child(currentUser.getUid() + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateFirebaseProfile(newName, uri.toString(), progressDialog, dialog);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileWithoutImage(String newName, ProgressDialog progressDialog, AlertDialog dialog) {
        String currentPhotoUrl = currentUser.getPhotoUrl() != null ?
                currentUser.getPhotoUrl().toString() : null;
        updateFirebaseProfile(newName, currentPhotoUrl, progressDialog, dialog);
    }

    private void updateFirebaseProfile(String newName, String imageUrl,
                                       ProgressDialog progressDialog, AlertDialog dialog) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(imageUrl != null ? Uri.parse(imageUrl) : null)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        currentUser = mAuth.getCurrentUser();
                        loadUserData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(),
                                "Error al actualizar: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser updatedUser = mAuth.getCurrentUser();
        if (updatedUser != currentUser) {
            currentUser = updatedUser;
            loadUserData();
        }
    }
}