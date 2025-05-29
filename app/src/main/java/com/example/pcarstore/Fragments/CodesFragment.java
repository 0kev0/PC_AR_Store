package com.example.pcarstore.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pcarstore.R;

public class CodesFragment extends Fragment {

    public CodesFragment() {

    }
    private void setupClickListeners(View rootView){
        rootView.findViewById(R.id.gift_cards).setOnClickListener(v -> {
            replaceFragment(new Gift_CardsFragment());
        });
        rootView.findViewById(R.id.card_discount_codes).setOnClickListener(v -> {
            replaceFragment(new DiscountCodesFragment());
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cupones, container, false);
        setupClickListeners(view);
        return view;
    }

    private void replaceFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragmentContaineradmin, fragment);
            transaction.addToBackStack("gift_navigation");
            transaction.commit();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al cargar el fragmento", Toast.LENGTH_SHORT).show();
            Log.e("GiftFragment", "Error en replaceFragment", e);
        }
    }

}