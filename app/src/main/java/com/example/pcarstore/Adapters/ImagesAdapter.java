package com.example.pcarstore.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.R;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private final List<Uri> imageUris;
    private final Context context;
    private int selectedPosition = -1;

    public ImagesAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        Glide.with(context)
                .load(imageUri)
                .centerCrop()
                .placeholder(R.drawable.ic_account_circle)
                .into(holder.imageView);

        holder.itemView.setBackgroundResource(
                selectedPosition == position ? R.drawable.ic_account_circle : android.R.color.transparent
        );

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPosition;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public Uri getSelectedImage() {
        if (selectedPosition != -1 && selectedPosition < imageUris.size()) {
            return imageUris.get(selectedPosition);
        }
        return null;
    }

    public void removeSelectedImage() {
        if (selectedPosition != -1 && selectedPosition < imageUris.size()) {
            imageUris.remove(selectedPosition);
            selectedPosition = -1;
            notifyDataSetChanged();
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSelectedImage);
        }
    }
}