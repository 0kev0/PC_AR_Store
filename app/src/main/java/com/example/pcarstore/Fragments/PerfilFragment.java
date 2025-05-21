package com.example.pcarstore.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pcarstore.Activities.GiftCardStoreActivity;
import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.Activities.OrdersActivity;
import com.example.pcarstore.Dialogs.EditProfileDialog;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.app.Activity.RESULT_OK;

import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class PerfilFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;
    private ImageView ivProfilePicture;
    private TextView tvUserName, tvUserEmail, tvUserBalance;
    private Button btnEditProfile, btnLogout;
    private Button btnOrders, btnWishlist, btnGifCard, btnSettings;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;
    private EditProfileDialog editProfileDialog;
    private User userData;
    private Uri tempImageUri;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Initialize userData as null, will be loaded from database
        userData = null;

        // Initialize the profile dialog with the updated constructor
        editProfileDialog = new EditProfileDialog(
                requireContext(),
                mAuth,
                FirebaseDatabase.getInstance(),
                FirebaseStorage.getInstance()
        );

        // Configure listener for the dialog
        editProfileDialog.setOnProfileUpdateListener(new EditProfileDialog.OnProfileUpdateListener() {
            @Override
            public void onImageSelectionRequested(Intent intent, int requestCode) {
                startActivityForResult(intent, requestCode);
            }

            @Override
            public void onProfileUpdatedSuccessfully(FirebaseUser updatedUser, User updatedUserData) {
                currentUser = updatedUser;
                userData = updatedUserData;
                loadUserData();
            }

            @Override
            public void onProfileUpdateFailed(Exception exception) {
                Toast.makeText(requireContext(),
                        "Error al actualizar perfil: " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Initialize views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnOrders = view.findViewById(R.id.btnOrders);
        btnWishlist = view.findViewById(R.id.btnWishlist);
        btnGifCard = view.findViewById(R.id.btnGifCard);
        btnSettings = view.findViewById(R.id.btnSettings);
        tvUserBalance = view.findViewById(R.id.tvUserBalance);

        loadUserData();

        // Configure listeners
        btnEditProfile.setOnClickListener(v -> editProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnOrders.setOnClickListener(v -> showOrders());
        btnWishlist.setOnClickListener(v -> showWishlist());
        btnGifCard.setOnClickListener(v -> showGifCard());
        btnSettings.setOnClickListener(v -> showSettings());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Log.d("PerfilFragment", "Imagen seleccionada URI: " + (imageUri != null ? imageUri.toString() : "null"));

            if (imageUri != null) {
                // Actualizar la vista previa inmediatamente
                Glide.with(requireContext())
                        .load(imageUri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .into(ivProfilePicture);

                // Pasar la URI al diálogo de edición
                if (editProfileDialog != null) {
                    editProfileDialog.setImageUri(imageUri);
                    Log.d("PerfilFragment", "URI de imagen pasada al diálogo de edición");
                } else {
                    Log.e("PerfilFragment", "El diálogo de edición es nulo");
                }
            } else {
                Log.e("PerfilFragment", "URI de imagen nulo en onActivityResult");
            }
        } else if (resultCode != RESULT_OK) {
            Log.d("PerfilFragment", "Selección de imagen cancelada o fallida");
        }
    }

    private void loadUserData() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            tvUserName.setText(currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : "Usuario");
            tvUserEmail.setText(currentUser.getEmail() != null ?
                    currentUser.getEmail() : "No especificado");

            // Mostrar imagen temporal mientras carga
            ivProfilePicture.setImageResource(R.drawable.ic_account_circle);

            // Cargar datos desde Realtime Database (que incluirá la URL de la imagen si existe)
            loadUserDetailsFromDatabase(currentUser.getUid());

            // Mostrar "Cargando saldo..."
            tvUserBalance.setText("Cargando saldo...");
        } else {
            // Modo invitado
            tvUserName.setText("Invitado");
            tvUserEmail.setText("No has iniciado sesión");
            ivProfilePicture.setImageResource(R.drawable.ic_account_circle);
            tvUserBalance.setText("S/ ----");
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
                    try {
                        userData = snapshot.getValue(User.class);

                        if (userData != null) {
                            // Set user ID if null
                            if (userData.getUserId() == null) {
                                userData.setUserId(userId);
                            }

                            // Update UI
                            if (userData.getName() != null && !userData.getName().isEmpty()) {
                                tvUserName.setText(userData.getName());
                            }

                            // Intenta cargar la imagen desde la URL guardada en la base de datos primero
                            if (userData.getProfileImageUrl() != null && !userData.getProfileImageUrl().isEmpty()) {
                                loadImageWithGlide(userData.getProfileImageUrl());
                            } else {
                                // Si no hay URL en la base de datos, intenta cargar desde Storage
                                loadProfileImageFromStorage(userId);
                            }

                            // Handle balance
                            if (snapshot.hasChild("saldo")) {
                                Double saldo = snapshot.child("saldo").getValue(Double.class);
                                updateBalanceUI(saldo != null ? saldo : 0.0);
                            } else {
                                userRef.child("saldo").setValue(0.0);
                                updateBalanceUI(0.0);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PerfilFragment", "Error parsing user data", e);
                        // Si hay error, intenta cargar imagen desde Storage directamente
                        loadProfileImageFromStorage(userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PerfilFragment", "Error loading user details", error.toException());
                updateBalanceUI(-1); // Mostrar error
                // Intenta cargar imagen desde Storage directamente
                loadProfileImageFromStorage(userId);
            }
        });
    }

    private void loadProfileImageFromStorage(String userId) {
        // Referencia al archivo en Storage con la estructura USERS/CLIENTS/userId.jpeg
        StorageReference profileImageRef = FirebaseStorage.getInstance()
                .getReference("USERS/CLIENTS")
                .child(userId + ".jpeg");

        // Obtener URL de descarga
        profileImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Cargar imagen con Glide
                    loadImageWithGlide(uri.toString());

                    // Guardar esta URL en Realtime Database para futuras cargas rápidas
                    saveImageUrlToDatabase(userId, uri.toString());
                })
                .addOnFailureListener(e -> {
                    Log.d("PerfilFragment", "No se encontró imagen en Storage, usando imagen por defecto");
                    // Mantener la imagen por defecto que ya estaba establecida
                });
    }

    private void loadImageWithGlide(String imageUrl) {
        Glide.with(requireContext())
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle)
                .into(ivProfilePicture);
    }

    private void saveImageUrlToDatabase(String userId, String imageUrl) {
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("profileImageUrl")
                .setValue(imageUrl)
                .addOnFailureListener(e -> {
                    Log.e("PerfilFragment", "Error al guardar URL de imagen en la base de datos", e);
                });
    }

    private void updateBalanceUI(double balance) {
        String formattedBalance;
        int colorId;

        if (balance < 0) { // Error case
            formattedBalance = "Error al cargar";
            colorId = R.color.colorError;
        } else {
            formattedBalance = String.format(Locale.getDefault(), "S/ %.2f", balance);
            colorId = balance < 20.0 ? R.color.red : R.color.colorSuccess;
        }

        tvUserBalance.setText(formattedBalance);
        tvUserBalance.setTextColor(ContextCompat.getColor(requireContext(), colorId));
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
            Fragment wishlistFragment = new WishlistFragment();
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainerView2, wishlistFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(),
                    "Debes iniciar sesión para ver tu Wishlist",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showGifCard() {
       /* if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(),
                    "Debes iniciar sesión para canjear Gift Cards",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        GiftCardDialog giftCardDialog = new GiftCardDialog(requireContext());
        giftCardDialog.setGiftCardCallback(new GiftCardDialog.GiftCardCallback() {
            @Override
            public void onSuccess(double amount) {
                // Actualizar saldo en la UI directamente
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid())
                        .child("saldo");

                userRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Double currentSaldo = currentData.getValue(Double.class);
                        if (currentSaldo == null) {
                            currentData.setValue(amount);
                        } else {
                            currentData.setValue(currentSaldo + amount);
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        if (error != null) {
                            Toast.makeText(requireContext(),
                                    "Error al actualizar saldo",
                                    Toast.LENGTH_SHORT).show();
                        } else if (committed) {
                            Toast.makeText(requireContext(),
                                    String.format("¡Saldo actualizado! +$%.2f", amount),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        giftCardDialog.show();*/
        Intent intent = new Intent(getContext(), GiftCardStoreActivity.class);
        startActivity(intent);
    }
    private void showSettings() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Configuración de cuenta");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_settings, null);

        // Configure elements
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> editProfile());
        view.findViewById(R.id.btn_payment_methods).setOnClickListener(v -> showPaymentMethods());

        // Configure notifications switch
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

        // Configure dark mode switch
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
        // Implement logic to show payment methods
        Toast.makeText(getContext(), "Mostrar métodos de pago", Toast.LENGTH_SHORT).show();
    }

    private void enableNotifications() {
        Toast.makeText(requireContext(), "Notificaciones activadas", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        Toast.makeText(requireContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
    }

    private void editProfile() {
        editProfileDialog.show();
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