package com.example.pcarstore.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pcarstore.R;


public class GiftFragment extends Fragment {

    public GiftFragment() {

    }
    public static GiftFragment newInstance(String param1, String param2) {
        GiftFragment fragment = new GiftFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gift, container, false);
        setupClickListeners(view);
        return view;
    }

    private void replaceFragment(Fragment fragment2) {
        try {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.GiftCardsFragment, fragment2);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al cargar el fragmento", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}