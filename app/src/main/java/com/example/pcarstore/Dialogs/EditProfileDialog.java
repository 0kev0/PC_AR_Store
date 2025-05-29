package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.Departamento;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 71;
    private final Context context;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase database;
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    private FirebaseUser currentUser;
    private User userData;
    private Uri imageUri;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;
    private OnProfileUpdateListener listener;
    private String selectedDepartamento;
    private String selectedCiudad;
    private ImageView ivProfilePreview;
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/pcarstore.firebasestorage.app/o/R.jpeg?alt=media&token=7c62b33a-f487-4257-8dd8-c6955ed6248e";

    public interface OnProfileUpdateListener {
        void onImageSelectionRequested(Intent intent, int requestCode);
        void onProfileUpdatedSuccessfully(FirebaseUser updatedUser, User updatedUserData);
        void onProfileUpdateFailed(Exception exception);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_profile, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return view;
    }

    public EditProfileDialog(Context context, FirebaseAuth mAuth, FirebaseDatabase database, FirebaseStorage storage) {
        this.context = context;
        this.mAuth = mAuth;
        this.database = database;
        this.storage = storage;
        this.storageRef = storage.getReference("USERS/CLIENTS");
        this.currentUser = mAuth.getCurrentUser();
    }


    public void show() {
        if (currentUser == null) {
            Toast.makeText(context, "Debes iniciar sesión para editar tu perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Spinner spinnerDepartamento = dialogView.findViewById(R.id.spinnerDepartamento);
        Spinner spinnerCiudad = dialogView.findViewById(R.id.spinnerCiudad);
        ivProfilePreview = dialogView.findViewById(R.id.ivProfilePreview);

        loadProfileImage();

        loadSpinnersData(spinnerDepartamento, spinnerCiudad);

        // Configurar valores iniciales
        if (userData != null && userData.getName() != null && !userData.getName().isEmpty()) {
            etName.setText(userData.getName());
        } else if (currentUser.getDisplayName() != null) {
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

            // Validar que se haya seleccionado un departamento y ciudad
            if (selectedDepartamento == null || selectedDepartamento.isEmpty()) {
                Toast.makeText(context, "Por favor selecciona un departamento", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCiudad == null || selectedCiudad.isEmpty()) {
                Toast.makeText(context, "Por favor selecciona una ciudad", Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressDialog("Actualizando perfil...");

            if (imageUri != null) {
                uploadImageAndUpdateProfile(newName, selectedDepartamento, selectedCiudad);
            } else {
                updateProfileWithoutImage(newName, selectedDepartamento, selectedCiudad);
            }
        });
    }

    public void setOnProfileUpdateListener(OnProfileUpdateListener listener) {
        this.listener = listener;
    }

    private void loadProfileImage() {
        // Imagen seleccionada para actualizar
        if (imageUri != null) {
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.limo)
                    .error(R.drawable.limo)
                    .circleCrop()
                    .into(ivProfilePreview);
            Log.d("EditProfile", "Imagen seleccionada para actualizar: " + imageUri);
            return;
        }

        //  Imagen del usuario en la base de datos
        if (userData != null && userData.getProfileImageUrl() != null &&
                !userData.getProfileImageUrl().isEmpty() &&
                !userData.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE_URL)) {
            Glide.with(context)
                    .load(userData.getProfileImageUrl())
                    .placeholder(R.drawable.limo)
                    .error(R.drawable.limo)
                    .circleCrop()
                    .into(ivProfilePreview);
            Log.d("EditProfile", "Imagen del usuario en la base de datos: " + userData.getProfileImageUrl());
            return;
        }

        //  Imagen de Firebase Auth
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(context)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.limo)
                    .error(R.drawable.limo)
                    .circleCrop()
                    .into(ivProfilePreview);
            Log.d("EditProfile", "Imagen de Firebase Auth: " + currentUser.getPhotoUrl());
            return;
        }

        //  Imagen por defecto
        Glide.with(context)
                .load(DEFAULT_PROFILE_IMAGE_URL)
                .placeholder(R.drawable.limo)
                .error(R.drawable.limo)
                .circleCrop()
                .into(ivProfilePreview);
        Log.d("EditProfile", "Imagen por defecto");
    }

    private void loadSpinnersData(Spinner spinnerDepartamento, Spinner spinnerCiudad) {
        DatabaseReference departamentosRef = FirebaseDatabase.getInstance().getReference("departamentos");

        departamentosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> departamentosList = new ArrayList<>();
                final Map<String, List<String>> ciudadesPorDepartamento = new HashMap<>();

                for (DataSnapshot departamentoSnapshot : snapshot.getChildren()) {
                    Departamento departamento = departamentoSnapshot.getValue(Departamento.class);
                    if (departamento != null) {
                        departamentosList.add(departamento.getNombre());
                        ciudadesPorDepartamento.put(departamento.getNombre(), departamento.getCiudades());
                    }
                }

                ArrayAdapter<String> departamentoAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, departamentosList);
                departamentoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDepartamento.setAdapter(departamentoAdapter);

                // Establecer selección actual si existe en userData
                if (userData != null && userData.getDepartamento() != null && !userData.getDepartamento().isEmpty()) {
                    int position = departamentosList.indexOf(userData.getDepartamento());
                    if (position >= 0) {
                        spinnerDepartamento.setSelection(position);
                        selectedDepartamento = userData.getDepartamento();
                    }
                }

                spinnerDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedDepartamento = departamentosList.get(position);
                        List<String> ciudades = ciudadesPorDepartamento.get(selectedDepartamento);

                        ArrayAdapter<String> ciudadAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ciudades);
                        ciudadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCiudad.setAdapter(ciudadAdapter);

                        // Establecer selección actual si existe en userData
                        if (userData != null && userData.getCiudad() != null && !userData.getCiudad().isEmpty()) {
                            int ciudadPosition = ciudades.indexOf(userData.getCiudad());
                            if (ciudadPosition >= 0) {
                                spinnerCiudad.setSelection(ciudadPosition);
                                selectedCiudad = userData.getCiudad();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                spinnerCiudad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCiudad = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setImageUri(Uri imageUri) {
        if (imageUri != null) {
            this.imageUri = imageUri;
            Log.d("EditProfile", "Nueva imagen asignada: " + imageUri.toString());

            // Actualizar la vista previa
            if (ivProfilePreview != null) {
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.limo)
                        .error(R.drawable.limo)
                        .circleCrop()
                        .into(ivProfilePreview);
                Log.d("EditProfile", "Vista previa actualizada con nueva imagen");
            }
        } else {
            Log.e("EditProfile", "Intento de asignar imageUri nulo");
        }
    }

    private void openImageChooser() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            Log.d("EditProfile", "Preparando intent para seleccionar imagen");

            if (listener != null) {
                Log.d("EditProfile", "Solicitando selección de imagen a través del listener");
                listener.onImageSelectionRequested(
                        Intent.createChooser(intent, "Selecciona una imagen"),
                        PICK_IMAGE_REQUEST
                );
            } else {
                Log.e("EditProfile", "Listener nulo, no se puede solicitar selección de imagen");
                Toast.makeText(context, "Error interno al seleccionar imagen", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EditProfile", "Error al abrir selector de imágenes", e);
            Toast.makeText(context, "No se pudo abrir el selector de imágenes", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndUpdateProfile(String newName, String departamento, String ciudad) {
        if (currentUser == null) {
            Log.e("EditProfile", "Usuario no autenticado");
            return;
        }

        if (imageUri == null) {
            Log.e("EditProfile", "No hay imagen seleccionada para subir");
            updateProfileWithoutImage(newName, departamento, ciudad);
            return;
        }

        // Verificar si hay una imagen previa que no sea la por defecto
        boolean shouldDeleteOldImage = userData != null &&
                userData.getProfileImageUrl() != null &&
                !userData.getProfileImageUrl().isEmpty() &&
                !userData.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE_URL);

        Log.d("EditProfile", "Preparando para subir imagen. Eliminar imagen anterior? " + shouldDeleteOldImage);

        // Crear referencia con nueva estructura: USERS/CLIENTS/userId/UserName.jpg
        String imageName = newName.replaceAll("\\s+", "_") + ".jpg"; // Reemplazar espacios por guiones bajos
        StorageReference userFolderRef = storageRef.child(currentUser.getUid()); // Referencia a la carpeta del usuario
        StorageReference fileReference = userFolderRef.child(imageName); // Referencia al archivo

        Log.d("EditProfile", "Ruta de almacenamiento: " + fileReference.getPath());
        Log.d("EditProfile", "URI de la imagen a subir: " + imageUri.toString());

        progressDialog.setMessage("Subiendo imagen...");

        fileReference.putFile(imageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Subiendo imagen: " + (int) progress + "%");
                    Log.d("EditProfile", "Progreso de carga: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("EditProfile", "Imagen subida exitosamente. Obteniendo URL de descarga...");
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("EditProfile", "URL de descarga obtenida: " + uri.toString());

                        // Eliminar imagen anterior si no es la por defecto
                        if (shouldDeleteOldImage) {
                            StorageReference oldImageRef = storage.getReferenceFromUrl(userData.getProfileImageUrl());
                            Log.d("EditProfile", "Intentando eliminar imagen anterior: " + oldImageRef.getPath());

                            oldImageRef.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Log.d("EditProfile", "Imagen anterior eliminada exitosamente");
                                } else {
                                    Log.e("EditProfile", "Error al eliminar imagen anterior", deleteTask.getException());
                                }
                                updateFirebaseProfile(newName, uri.toString(), departamento, ciudad);
                            });
                        } else {
                            updateFirebaseProfile(newName, uri.toString(), departamento, ciudad);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("EditProfile", "Error al obtener URL de descarga", e);
                        dismissProgressDialog();
                        if (listener != null) {
                            listener.onProfileUpdateFailed(e);
                        }
                        Toast.makeText(context, "Error al obtener URL de la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfile", "Error al subir imagen", e);
                    dismissProgressDialog();
                    if (listener != null) {
                        listener.onProfileUpdateFailed(e);
                    }
                    Toast.makeText(context, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileWithoutImage(String newName, String departamento, String ciudad) {
        // Mantener la imagen existente, incluyendo la posibilidad de que sea la por defecto
        String currentPhotoUrl = (userData != null && userData.getProfileImageUrl() != null) ?
                userData.getProfileImageUrl() :
                (currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : DEFAULT_PROFILE_IMAGE_URL);

        updateFirebaseProfile(newName, currentPhotoUrl, departamento, ciudad);

    }

    private void updateFirebaseProfile(String newName, String imageUrl, String departamento, String ciudad) {
        Log.d("EditProfile", "Iniciando actualización de perfil en Firebase");
        Log.d("EditProfile", "Nuevo nombre: " + newName);
        Log.d("EditProfile", "Nueva URL de imagen: " + imageUrl);
        Log.d("EditProfile", "Departamento: " + departamento);
        Log.d("EditProfile", "Ciudad: " + ciudad);

        // 1. Actualizar Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .setPhotoUri(imageUrl != null ? Uri.parse(imageUrl) : null)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Log.d("EditProfile", "Perfil de Auth actualizado exitosamente");
                        // 2. Actualizar Firebase Database
                        updateUserInDatabase(newName, imageUrl, departamento, ciudad);
                    } else {
                        Log.e("EditProfile", "Error al actualizar perfil de Auth", authTask.getException());
                        dismissProgressDialog();
                        if (listener != null && authTask.getException() != null) {
                            listener.onProfileUpdateFailed(authTask.getException());
                        }
                        Toast.makeText(context, "Error al actualizar: " + (authTask.getException() != null ?
                                        authTask.getException().getMessage() : "Error desconocido"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInDatabase(String newName, String imageUrl, String departamento, String ciudad) {
        DatabaseReference userRef = database.getReference("users").child(currentUser.getUid());
        Log.d("EditProfile", "Actualizando usuario en base de datos: " + userRef.getKey());

        // Primero obtener los datos actuales del usuario
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("EditProfile", "Datos actuales del usuario encontrados");
                    // Crear mapa con TODOS los campos necesarios
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    updates.put("departamento", departamento);
                    updates.put("ciudad", ciudad);

                    // Actualizar la URL de la imagen
                    if (imageUrl != null) {
                        updates.put("profileImageUrl", imageUrl);
                        Log.d("EditProfile", "Actualizando URL de imagen a: " + imageUrl);
                    } else if (snapshot.hasChild("profileImageUrl")) {
                        Log.d("EditProfile", "Manteniendo URL de imagen existente");
                        updates.put("profileImageUrl", snapshot.child("profileImageUrl").getValue());
                    }

                    // Mantener otros campos importantes
                    String[] fieldsToKeep = {"email", "role", "saldo", "membresiaPrime"};
                    for (String field : fieldsToKeep) {
                        if (snapshot.hasChild(field)) {
                            updates.put(field, snapshot.child(field).getValue());
                        }
                    }

                    // Actualizar la base de datos
                    Log.d("EditProfile", "Aplicando actualizaciones: " + updates.toString());
                    userRef.updateChildren(updates)
                            .addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Log.d("EditProfile", "Base de datos actualizada exitosamente");
                                    // Actualizar el objeto local userData
                                    if (userData == null) {
                                        userData = new User.Builder(currentUser.getEmail()).build();
                                        userData.setUserId(currentUser.getUid());
                                        Log.d("EditProfile", "Nuevo objeto userData creado");
                                    }
                                    userData.setName(newName);
                                    if (imageUrl != null) {
                                        userData.setProfileImageUrl(imageUrl);
                                    }
                                    userData.setDepartamento(departamento);
                                    userData.setCiudad(ciudad);

                                    currentUser = mAuth.getCurrentUser();
                                    if (listener != null) {
                                        listener.onProfileUpdatedSuccessfully(currentUser, userData);
                                    }
                                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Log.e("EditProfile", "Error al actualizar base de datos", dbTask.getException());
                                    if (listener != null && dbTask.getException() != null) {
                                        listener.onProfileUpdateFailed(dbTask.getException());
                                    }
                                    Toast.makeText(context, "Error al actualizar perfil: " +
                                                    (dbTask.getException() != null ?
                                                            dbTask.getException().getMessage() : "Error desconocido"),
                                            Toast.LENGTH_SHORT).show();
                                }
                                dismissProgressDialog();
                            });
                } else {
                    Log.e("EditProfile", "No se encontraron datos del usuario en la base de datos");
                    dismissProgressDialog();
                    Toast.makeText(context, "Error: usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditProfile", "Error al leer datos del usuario", error.toException());
                dismissProgressDialog();
                Toast.makeText(context, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}