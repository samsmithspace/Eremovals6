package com.example.eremovals6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eremovals6.data.AppDatabase;
import com.example.eremovals6.data.PriceItemDao;
import com.example.eremovals6.models.PriceItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayPriceItemsActivity extends AppCompatActivity {
    private ExpandableListView expandableListView;
    private PriceItemsAdapter adapter;
    private HashMap<String, List<String>> categoryItemsMap = new HashMap<>();
    private HashMap<String, List<PriceItem>> categoryPriceItemsMap = new HashMap<>();
    private List<String> categories = new ArrayList<>();
    private PriceItemDao priceItemDao;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_price_items);

        expandableListView = findViewById(R.id.expandableListViewPriceItems);

        // Initialize Room database and DAO
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        priceItemDao = db.priceItemDao();

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Check for internet connection and fetch data accordingly
        if (isConnected()) {
            fetchPriceItemsFromServer();
        } else {
            fetchPriceItemsFromDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit activity
        if (isConnected()) {
            fetchPriceItemsFromServer();
        } else {
            fetchPriceItemsFromDatabase();
        }
    }

    // Fetch price items from the server when online
    private void fetchPriceItemsFromServer() {
        apiService.getAllPriceItems().enqueue(new Callback<List<PriceItem>>() {
            @Override
            public void onResponse(Call<List<PriceItem>> call, Response<List<PriceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PriceItem> items = response.body();
                    displayItems(items);

                    // Save the fetched items locally in the database for offline access
                    new Thread(() -> {
                        priceItemDao.clearAll(); // Clear existing data
                        priceItemDao.insertAll(items); // Insert new data
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<PriceItem>> call, Throwable t) {
                Toast.makeText(DisplayPriceItemsActivity.this, "Failed to load data from server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch price items from the local Room database when offline
    private void fetchPriceItemsFromDatabase() {
        new Thread(() -> {
            List<PriceItem> items = priceItemDao.getAllPriceItems();
            runOnUiThread(() -> {
                if (items.isEmpty()) {
                    Toast.makeText(DisplayPriceItemsActivity.this, "No data available offline", Toast.LENGTH_SHORT).show();
                } else {
                    displayItems(items);
                }
            });
        }).start();
    }

    // Display the list of items grouped by category in the ExpandableListView
    private void displayItems(List<PriceItem> items) {
        // Clear existing data to avoid duplicates in the display
        categoryItemsMap.clear();
        categoryPriceItemsMap.clear();
        categories.clear();

        for (PriceItem item : items) {
            String category = item.getCategory();
            if (!categoryItemsMap.containsKey(category)) {
                categoryItemsMap.put(category, new ArrayList<>());
                categoryPriceItemsMap.put(category, new ArrayList<>());
                categories.add(category);
            }
            categoryItemsMap.get(category).add(item.getItemName() + "|Normal Price-£" + item.getNormalPrice() + "|Helper Price-£" + item.getHelperPrice());
            categoryPriceItemsMap.get(category).add(item);
        }

        // Pass both hashmaps to the adapter
        adapter = new PriceItemsAdapter(this, categories, categoryItemsMap, categoryPriceItemsMap, priceItemDao);
        expandableListView.setAdapter(adapter);
    }

    // Helper method to check network connectivity
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}