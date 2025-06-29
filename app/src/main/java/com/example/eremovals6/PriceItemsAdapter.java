package com.example.eremovals6;

import android.content.Context;
import android.content.Intent;
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
    private final HashMap<String, List<PriceItem>> categoryPriceItemsMap;
    private final PriceItemDao priceItemDao;
    private ApiService apiService;

    public PriceItemsAdapter(Context context, List<String> categories,
                             HashMap<String, List<String>> categoryItemsMap,
                             HashMap<String, List<PriceItem>> categoryPriceItemsMap,
                             PriceItemDao priceItemDao) {
        this.context = context;
        this.categories = categories;
        this.categoryItemsMap = categoryItemsMap;
        this.categoryPriceItemsMap = categoryPriceItemsMap;
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

        // Get the actual PriceItem object
        String category = categories.get(groupPosition);
        PriceItem currentPriceItem = null;
        if (categoryPriceItemsMap.containsKey(category)) {
            List<PriceItem> itemsInCategory = categoryPriceItemsMap.get(category);
            if (childPosition < itemsInCategory.size()) {
                currentPriceItem = itemsInCategory.get(childPosition);
            }
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        // Find views
        TextView itemNamePriceTextView = convertView.findViewById(R.id.listItem);
        TextView normalPriceTextView = convertView.findViewById(R.id.listItem2);
        TextView helperPriceTextView = convertView.findViewById(R.id.listItem3);
        Button editButton = convertView.findViewById(R.id.editButton);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Parse the itemNameWithPrice string
        String[] parts = itemNameWithPrice.split("\\|");
        String itemName = parts[0];
        String price = parts.length > 1 ? parts[1] : "N/A";
        String helperPrice = parts.length > 2 ? parts[2] : "N/A";

        // Set text
        itemNamePriceTextView.setText(itemName);
        normalPriceTextView.setText(price);
        helperPriceTextView.setText(helperPrice);

        // Set edit button click listener
        final PriceItem finalCurrentPriceItem = currentPriceItem;
        editButton.setOnClickListener(v -> {
            if (finalCurrentPriceItem != null) {
                Intent intent = new Intent(context, EditPriceItemActivity.class);
                // Use serverId (MongoDB _id) instead of local Room ID
                String idToPass = finalCurrentPriceItem.getServerId() != null ?
                        finalCurrentPriceItem.getServerId() : String.valueOf(finalCurrentPriceItem.getId());
                intent.putExtra("ITEM_ID", idToPass);
                intent.putExtra("ITEM_NAME", finalCurrentPriceItem.getItemName());
                intent.putExtra("ITEM_CATEGORY", finalCurrentPriceItem.getCategory());
                intent.putExtra("NORMAL_PRICE", finalCurrentPriceItem.getNormalPrice());
                intent.putExtra("HELPER_PRICE", finalCurrentPriceItem.getHelperPrice());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Unable to edit this item", Toast.LENGTH_SHORT).show();
            }
        });

        // Set delete button click listener
        deleteButton.setOnClickListener(v -> {
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
                            if (categoryPriceItemsMap.containsKey(category) && finalCurrentPriceItem != null) {
                                categoryPriceItemsMap.get(category).remove(finalCurrentPriceItem);
                            }
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
                    priceItemDao.deleteByName(itemName);
                    Log.d(TAG, "Item deleted from local database");
                    // Update local UI
                    categoryItemsMap.get(categories.get(groupPosition)).remove(itemNameWithPrice);
                    if (categoryPriceItemsMap.containsKey(category) && finalCurrentPriceItem != null) {
                        categoryPriceItemsMap.get(category).remove(finalCurrentPriceItem);
                    }
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