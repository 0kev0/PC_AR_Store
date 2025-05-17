package com.example.pcarstore.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pcarstore.Adapters.UserAdapter;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UsuariosFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private DatabaseReference usersRef;

    private Button btnNewUser;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);

        // 1. Inicializar Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        btnNewUser = view.findViewById(R.id.btnAddUser);
        btnNewUser.setOnClickListener(v -> showAddUserDialog());
        // 2. Configurar RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rvUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(usersRef, this);
        recyclerView.setAdapter(userAdapter);

        // 3. Configurar búsqueda y filtros
        setupSearchAndFilters(view);

        return view;
    }

    private void setupSearchAndFilters(View view) {
        // Configurar ChipGroup para filtrar por roles
        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = view.findViewById(checkedId);
            if (chip != null) {
                String filterText = chip.getText().toString();
                if (filterText.equals("Todos")) {
                    userAdapter.loadUsers();
                } else {
                    userAdapter.filterByRole(filterText);
                }
            }
        });
    }

    @Override
    public void onEditClick(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Editar Usuario");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        builder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        TextInputEditText etRole = view.findViewById(R.id.etRole);

        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etRole.setText(user.getRole());


        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newRole = etRole.getText().toString().trim();

            if(newName.isEmpty() || newEmail.isEmpty() || newRole.isEmpty()) {
                Toast.makeText(getContext(), "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserInFirebase(user.getUserId(), newName, newEmail, newRole);
        });

        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateUserInFirebase(String userId, String newName, String newEmail, String newRole) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("email", newEmail);
        updates.put("role", newRole);
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteClick(User user) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Eliminar permanentemente a " + user.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Mostrar progreso
                    ProgressDialog progress = new ProgressDialog(getContext());
                    progress.setMessage("Eliminando usuario...");
                    progress.setCancelable(false);
                    progress.show();

                    deleteUserFromDatabase(user, progress);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_remove)
                .show();
    }

    @Override
    public void onViewDetailsClick(User user) {

    }

    private void deleteUserFromDatabase(User user, ProgressDialog progress) {
        if (user == null || user.getUserId() == null) {
            progress.dismiss();
            Toast.makeText(getContext(), "Error: Usuario inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(user.getUserId());

        // Primero verifica que el nodo existe
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    progress.dismiss();
                    Toast.makeText(getContext(), "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Eliminar de Firebase
                userRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            progress.dismiss();
                            Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();

                            // Opcional: Actualizar la lista manualmente
                            if (userAdapter != null) {
                                userAdapter.removeUserFromList(user.getUserId());
                            }
                        })
                        .addOnFailureListener(e -> {
                            progress.dismiss();
                            Toast.makeText(getContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("FirebaseDelete", "Error: ", e);
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.dismiss();
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Agregar Nuevo Usuario");

        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);

        // Obtener referencias a los campos
        TextInputEditText etName = dialogView.findViewById(R.id.etName );
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etRole = dialogView.findViewById(R.id.etRole);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Validar y obtener datos
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = etRole.getText().toString().trim();

            if (validateUserInput(name, email, password, role)) {
                //saveNewUserToFirebase(name, email, password, role);
            }
        });

        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateUserInput(String name, String email, String password, String role) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "El nombre es requerido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Email inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (role.isEmpty()) {
            Toast.makeText(getContext(), "El rol es requerido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void saveNewUserToFirebase(String name, String email, String role) {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage("Guardando usuario...");
        progress.show();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        String userId = usersRef.push().getKey(); // Genera ID único

        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setRole(role);
        //newUser.setCreatedAt(System.currentTimeMillis());

        usersRef.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    progress.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Usuario guardado", Toast.LENGTH_SHORT).show();
                        userAdapter.loadUsers();
                    } else {
                        Toast.makeText(getContext(), "Error: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}