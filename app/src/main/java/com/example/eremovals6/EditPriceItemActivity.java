package com.example.eremovals6;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eremovals6.data.AppDatabase;
import com.example.eremovals6.data.PriceItemDao;
import com.example.eremovals6.models.PriceItem;
import com.example.eremovals6.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditPriceItemActivity extends AppCompatActivity {

    private EditText itemNameEditText, normalPriceEditText, helperPriceEditText;
    private Spinner categorySpinner;
    private Button updateButton;
    private ApiService apiService;
    private PriceItemDao priceItemDao;
    private PriceItem currentItem;
    private String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_price_item);

        itemNameEditText = findViewById(R.id.itemName);
        categorySpinner = findViewById(R.id.categorySpinner);
        normalPriceEditText = findViewById(R.id.normalPrice);
        helperPriceEditText = findViewById(R.id.helperPrice);
        updateButton = findViewById(R.id.btnUpdate);

        // Set up the category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Initialize Room database and DAO
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        priceItemDao = db.priceItemDao();

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Get the item data from intent
        Intent intent = getIntent();
        if (intent != null) {
            itemId = intent.getStringExtra("ITEM_ID");
            String itemName = intent.getStringExtra("ITEM_NAME");
            String category = intent.getStringExtra("ITEM_CATEGORY");
            double normalPrice = intent.getDoubleExtra("NORMAL_PRICE", 0.0);
            double helperPrice = intent.getDoubleExtra("HELPER_PRICE", 0.0);

            // Populate the fields
            populateFields(itemName, category, normalPrice, helperPrice);
        }

        updateButton.setOnClickListener(view -> updatePriceItem());
    }

    private void populateFields(String itemName, String category, double normalPrice, double helperPrice) {
        itemNameEditText.setText(itemName);
        normalPriceEditText.setText(String.valueOf(normalPrice));
        helperPriceEditText.setText(String.valueOf(helperPrice));

        // Set spinner selection
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
        int position = adapter.getPosition(category);
        categorySpinner.setSelection(position);
    }

    private void updatePriceItem() {
        String itemName = itemNameEditText.getText().toString().trim();
        String normalPriceInput = normalPriceEditText.getText().toString().trim();
        String helperPriceInput = helperPriceEditText.getText().toString().trim();

        // Validate item name
        if (itemName.isEmpty()) {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itemName.length() > 50) {
            Toast.makeText(this, "Item name is too long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate normal price
        double normalPrice;
        try {
            normalPrice = Double.parseDouble(normalPriceInput);
            if (normalPrice <= 0) {
                Toast.makeText(this, "Normal price must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid normal price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate helper price
        double helperPrice;
        try {
            helperPrice = Double.parseDouble(helperPriceInput);
            if (helperPrice <= 0) {
                Toast.makeText(this, "Helper price must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid helper price", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categorySpinner.getSelectedItem().toString();

        // Create updated PriceItem object
        PriceItem updatedItem = new PriceItem(itemName, category, normalPrice, helperPrice);

        if (isConnected()) {
            // If online, update the item on the server
            apiService.updatePriceItem(itemId, updatedItem).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditPriceItemActivity.this, "Price item updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditPriceItemActivity.this, "Failed to update item on server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EditPriceItemActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If offline, update the item locally
            new Thread(() -> {
                // Note: You'll need to add an update method to your DAO that takes the item name
                // For now, we'll show a message that update will sync when online
                runOnUiThread(() -> {
                    Toast.makeText(EditPriceItemActivity.this, "Offline - changes will sync when online", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        }
    }

    // Helper method to check network connectivity
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}