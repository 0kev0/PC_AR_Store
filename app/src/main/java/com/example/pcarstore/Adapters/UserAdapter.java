package com.example.pcarstore.Adapters;

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
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    /*************************************************************VARIABLES******************************************************************************************/
    private static final String TAG = "UserAdapter";
    private List<User> userList = new ArrayList<>();
    private List<User> userListFull = new ArrayList<>();
    private final OnUserClickListener listener;
    public interface OnUserClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
        void onViewDetailsClick(User user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
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

    public void updateList(List<User> newList) {
        this.userList = new ArrayList<>(newList);
        this.userListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filterByRole(String role) {
        List<User> filteredList = new ArrayList<>();
        if (role.equals("Todos")) {
            filteredList.addAll(userListFull);
        } else {
            for (User user : userListFull) {
                if (user.getRole() != null && user.getRole().equalsIgnoreCase(role)) {
                    filteredList.add(user);
                }
            }
        }
        this.userList = filteredList;
        notifyDataSetChanged();
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


}