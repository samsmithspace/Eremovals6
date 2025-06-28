package com.example.eremovals6;

import android.content.Context;
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

public class AddPriceItemActivity extends AppCompatActivity {

    private EditText itemNameEditText, normalPriceEditText, helperPriceEditText;
    private Spinner categorySpinner;
    private Button saveButton;
    private ApiService apiService;
    private PriceItemDao priceItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_price_item);

        itemNameEditText = findViewById(R.id.itemName);
        categorySpinner = findViewById(R.id.categorySpinner);
        normalPriceEditText = findViewById(R.id.normalPrice);
        helperPriceEditText = findViewById(R.id.helperPrice);
        saveButton = findViewById(R.id.btnSave);

        // Set up the category spinner with options "Appliances" and "Furniture"
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

        saveButton.setOnClickListener(view -> addPriceItem());
    }

    private void addPriceItem() {
        String itemName = itemNameEditText.getText().toString().trim(); // Trim leading/trailing spaces
        String normalPriceInput = normalPriceEditText.getText().toString().trim();
        String helperPriceInput = helperPriceEditText.getText().toString().trim();

        // Validate item name
        if (itemName.isEmpty()) {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (itemName.length() > 50) { // Optional: limit the length
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

        String category = categorySpinner.getSelectedItem().toString(); // Get selected category

        // Create a PriceItem object
        PriceItem priceItem = new PriceItem(itemName, category, normalPrice, helperPrice);

        if (isConnected()) {
            // If online, send the item to the server
            apiService.addPriceItem(priceItem).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddPriceItemActivity.this, "Price item added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddPriceItemActivity.this, "Failed to add item to server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AddPriceItemActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If offline, save the item locally
            new Thread(() -> {
                priceItemDao.insert(priceItem);
                runOnUiThread(() -> Toast.makeText(AddPriceItemActivity.this, "Saved locally for later sync", Toast.LENGTH_SHORT).show());
                finish();
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
