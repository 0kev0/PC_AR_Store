package com.example.pcarstore.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class FragmentCatalogo extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabFilter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerViewCatalogo);
        //fabFilter = view.findViewById(R.id.fabFilter);

        return view;
    }

    private void setupRecyclerView() {
        // Configurar el LayoutManager como GridLayoutManager con 2 columnas
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        recyclerView.setAdapter(null);
    }
}