package com.example.eremovals6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.eremovals6.DisplayPriceItemsActivity;
import com.example.eremovals6.data.PriceItemDao;
import com.example.eremovals6.models.PriceItem;

import java.util.HashMap;
import java.util.List;
import android.util.Log;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PriceItemsAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "MyAppTag";
    private final Context context;
    private final List<String> categories;
    private final HashMap<String, List<String>> categoryItemsMap;
    private final PriceItemDao priceItemDao;
    private ApiService apiService;

    public PriceItemsAdapter(Context context, List<String> categories, HashMap<String, List<String>> categoryItemsMap, PriceItemDao priceItemDao) {
        this.context = context;
        this.categories = categories;
        this.categoryItemsMap = categoryItemsMap;
        this.priceItemDao = priceItemDao;
        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryItemsMap.get(categories.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryItemsMap.get(categories.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, parent, false);
        }
        TextView categoryTextView = (TextView) convertView.findViewById(R.id.listGroup);
        categoryTextView.setText(categoryTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String itemNameWithPrice = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }


        // Find views
        TextView itemNamePriceTextView = convertView.findViewById(R.id.listItem);
        TextView normalPriceTextView = convertView.findViewById(R.id.listItem2); // Second TextView
        TextView helperPriceTextView = convertView.findViewById(R.id.listItem3); // Second TextView
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Parse the itemNameWithPrice string (assume format: "ItemName - Price - HelperPrice")
        String[] parts = itemNameWithPrice.split("\\|");

        String itemName = parts[0];
        String price = parts.length > 1 ? parts[1] : "N/A"; // Fallback if price is missing
        String helperPrice = parts.length > 2 ? parts[2] : "N/A"; // Fallback if helper price is missing


        // Set text
        itemNamePriceTextView.setText(itemName);
        normalPriceTextView.setText(price);
        helperPriceTextView.setText(helperPrice);


        // Set delete button click listener
        deleteButton.setOnClickListener(v -> {
            //String itemName = itemNameWithPrice.split(" - ")[0]; // Assuming item name is the first part
            Log.d(TAG, "Attempting to delete item: " + itemName);

            if (isConnected()) {
                // If online, delete the item from the server
                apiService.deletePriceItem(itemName).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if (response.isSuccessful()) {
                            Log.d(TAG, "Item deleted from server successfully");
                            // Remove from local UI
                            categoryItemsMap.get(categories.get(groupPosition)).remove(itemNameWithPrice);
                            ((DisplayPriceItemsActivity) context).runOnUiThread(() -> notifyDataSetChanged());
                        } else {
                            Log.e(TAG, "Failed to delete item from server");
                            ((DisplayPriceItemsActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Failed to delete item from server", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error deleting item from server: " + t.getMessage());
                        ((DisplayPriceItemsActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error deleting item from server", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                // If offline, delete the item locally
                new Thread(() -> {
                    priceItemDao.deleteByName(itemName); // Delete item from local Room database
                    Log.d(TAG, "Item deleted from local database");
                    // Update local UI
                    categoryItemsMap.get(categories.get(groupPosition)).remove(itemNameWithPrice);
                    ((DisplayPriceItemsActivity) context).runOnUiThread(() -> {
                        notifyDataSetChanged();
                        Toast.makeText(context, "Deleted locally. Will sync when online.", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });


        return convertView;
    }
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
