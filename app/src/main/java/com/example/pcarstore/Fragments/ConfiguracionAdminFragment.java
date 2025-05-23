package com.example.pcarstore.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.Dialogs.ChangePasswordDialog;
import com.example.pcarstore.Dialogs.EditAdminDialog;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfiguracionAdminFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    private TextView tvName, tvEmail, tvRole;
    private Button btnEditInfo, changePassword, btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion_admin, container, false);

        changePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        initFirebase();
        initViews(view);
        setupListeners();
        loadAdminData();

        return view;
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvAdminName);
        tvEmail = view.findViewById(R.id.tvAdminEmail);
        tvRole = view.findViewById(R.id.tvAdminRole);
        btnEditInfo = view.findViewById(R.id.btnEditInfo);
    }

    private void setupListeners() {
        btnEditInfo.setOnClickListener(v -> showEditDialog());
        changePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void showEditDialog() {
        EditAdminDialog.show(requireContext(),
                tvName.getText().toString(),
                tvEmail.getText().toString(),
                tvRole.getText().toString(),
                mDatabase,
                currentUserId,
                this::loadAdminData);
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog.show(requireContext(), new ChangePasswordDialog.OnPasswordResetListener() {
            @Override
            public void onResetEmailSent(String email) {
                // Puedes agregar lógica adicional aquí si es necesario
            }

            @Override
            public void onResetError(String error) {
                // Manejo de errores específico del fragmento si es necesario
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

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());

            mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User admin = snapshot.getValue(User.class);
                        if (admin != null) {
                            tvName.setText(admin.getName());
                            tvRole.setText(admin.getRole());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}