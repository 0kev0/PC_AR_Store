package com.example.pcarstore.Dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class CategoryArrayAdapter extends ArrayAdapter<String> {
    private final List<String> categories;
    private final List<String> filteredCategories;

    public CategoryArrayAdapter(Context context, int resource, List<String> categories) {
        super(context, resource, categories);
        this.categories = new ArrayList<>(categories);
        this.filteredCategories = new ArrayList<>(categories);
    }

    @Override
    public int getCount() {
        return filteredCategories.size();
    }

    @Override
    public String getItem(int position) {
        return filteredCategories.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(categories);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (String category : categories) {
                        if (category.toLowerCase().contains(filterPattern)) {
                            filteredList.add(category);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredCategories.clear();
                if (results.values != null) {
                    filteredCategories.addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }
}