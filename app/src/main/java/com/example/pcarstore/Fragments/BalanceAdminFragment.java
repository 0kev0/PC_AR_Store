package com.example.pcarstore.Fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.TransactionAdapter;
import com.example.pcarstore.ModelsDB.Transaction;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.PDFGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BalanceAdminFragment extends Fragment {
    /*************************************************************VARIABLES******************************************************************************************/
    private TextView tvIncome, tvExpenses, tvNetBalance;
    private DatabaseReference transactionsRef;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private double totalIncome = 0.0;
    private double totalExpenses = 0.0;
    private static final String[] PERIOD_OPTIONS = {"Hoy", "Esta semana", "Este mes", "Este año", "Todos"};
    public BalanceAdminFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_balance_admin, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        transactionsRef = database.getReference("transactions");

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpenses = view.findViewById(R.id.tvExpenses);
        tvNetBalance = view.findViewById(R.id.tvNetBalance);
        Spinner spinnerPeriod = view.findViewById(R.id.spinnerPeriod);
        RecyclerView rvTransactions = view.findViewById(R.id.rvTransactions);
        Button btnExport = view.findViewById(R.id.btnExport);

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, PERIOD_OPTIONS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(spinnerAdapter);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTransactionsForPeriod(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnExport.setOnClickListener(v -> generateAndShareBalancePDF());

        loadTransactionsForPeriod(0);

        return view;
    }

    private void loadTransactionsForPeriod(int periodIndex) {
        Query query;
        Calendar calendar = Calendar.getInstance();
        long startTime = 0;

        if (periodIndex != 4) {
            query = transactionsRef.orderByChild("date").startAt(startTime);
        } else {
            query = transactionsRef.orderByChild("date");
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                totalIncome = 0.0;
                totalExpenses = 0.0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String type = dataSnapshot.child("type").getValue(String.class);
                    Double amount = dataSnapshot.child("amount").getValue(Double.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    Long date = dataSnapshot.child("date").getValue(Long.class);
                    String userId = dataSnapshot.child("userId").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    if (type != null && amount != null && date != null && "completed".equals(status)) {
                        Transaction transaction = new Transaction();
                        transaction.setId(dataSnapshot.getKey());
                        transaction.setType(type);
                        transaction.setAmount(amount);
                        transaction.setDescription(description != null ? description : "");
                        transaction.setTimestamp(date);
                        transaction.setUserId(userId != null ? userId : "Admin");

                        transactionList.add(transaction);

                        if (type.contains("purchase") || type.equals("expense")) {
                            totalExpenses += amount;
                        } else {
                            totalIncome += amount;
                        }
                    }
                }

                updateBalanceUI();
                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar transacciones: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalanceUI() {
        tvIncome.setText(String.format(Locale.getDefault(), "$%.2f", totalIncome));
        tvExpenses.setText(String.format(Locale.getDefault(), "$%.2f", totalExpenses));

        double netBalance = totalIncome - totalExpenses;
        tvNetBalance.setText(String.format(Locale.getDefault(), "$%.2f", netBalance));
        setBalanceColor(tvNetBalance, netBalance);
    }

    private void setBalanceColor(TextView tvNetBalance, double netBalance) {
        if (tvNetBalance == null || getActivity() == null) {
            return;
        }

        try {
            int colorId = (netBalance >= 0) ? R.color.green_700 : R.color.red_700;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvNetBalance.setTextColor(ContextCompat.getColor(requireContext(), colorId));
            }
            else {
                tvNetBalance.setTextColor(getResources().getColor(colorId));
            }
        } catch (Resources.NotFoundException e) {
            Log.e("BalanceColor", "Color no encontrado", e);
            tvNetBalance.setTextColor(Color.BLACK);
        }
    }

    private void generateAndShareBalancePDF() {
        double income = totalIncome;
        double expenses = totalExpenses;
        double netBalance = income - expenses;

        File pdfFile = PDFGenerator.generateFinancialReport(requireContext(), income, expenses, netBalance, transactionList);

        if (pdfFile != null && pdfFile.exists()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    pdfFile);

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Compartir Reporte"));
        } else {
            Toast.makeText(getContext(), "Error al generar el reporte", Toast.LENGTH_SHORT).show();
        }
    }

}