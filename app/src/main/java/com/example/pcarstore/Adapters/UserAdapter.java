package com.example.pcarstore.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "UserAdapter";
    private List<User> userList = new ArrayList<>();
    private DatabaseReference usersRef;
    private OnUserClickListener listener;
    private ValueEventListener valueEventListener;

    public interface OnUserClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
        void onViewDetailsClick(User user);
    }

    public UserAdapter(DatabaseReference usersRef, OnUserClickListener listener) {
        this.usersRef = usersRef;
        this.listener = listener;
        loadUsers();
    }

    public void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                Log.d(TAG, "Número de usuarios encontrados: " + snapshot.getChildrenCount());

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        userList.add(user);
                        Log.d(TAG, "Usuario añadido: " + user.getName());
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar usuarios: " + error.getMessage());
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void filterUsers(String query) {
        if (query.isEmpty()) {
            loadUsers();
        } else {
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null &&
                                (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                                        user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                                        (user.getRole() != null && user.getRole().toLowerCase().contains(query.toLowerCase())))) {
                            user.setUserId(userSnapshot.getKey());
                            userList.add(user);
                        }
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al filtrar usuarios: " + error.getMessage());
                }
            });
        }
    }

    public void filterByRole(String role) {
        if (role.equals("Todos")) {
            loadUsers();
        } else {
            usersRef.orderByChild("role").equalTo(role)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    user.setUserId(userSnapshot.getKey());
                                    userList.add(user);
                                }
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error al filtrar por rol: " + error.getMessage());
                        }
                    });
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserAvatar;
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserRole;
        private final TextView tvUserLocation;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;
        private final MaterialButton btnDetails;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserLocation = itemView.findViewById(R.id.tvUserLocation);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }

        public void bind(User user, OnUserClickListener listener) {
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText(user.getRole() != null ? user.getRole() : "Sin rol");

            String location = (user.getDepartamento() != null ? user.getDepartamento() : "") +
                    (user.getCiudad() != null ? ", " + user.getCiudad() : "");
            tvUserLocation.setText(location.isEmpty() ? "Ubicación no especificada" : location);

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.ic_account_circle)
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setImageResource(R.drawable.ic_account_circle);
            }

            if (user.isMembresiaPrime()) {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.prime_member_background));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent));
            }

            btnEdit.setOnClickListener(v -> listener.onEditClick(user));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
            btnDetails.setOnClickListener(v -> listener.onViewDetailsClick(user));
            itemView.setOnClickListener(v -> listener.onViewDetailsClick(user));
        }
    }
    public void removeUserFromList(String userId) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId().equals(userId)) {
                userList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId().equals(userId)) {
                userList.remove(i);
                break;
            }
        }
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (userList.isEmpty()) {} else {}
    }
}