package com.example.eremovals6;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eremovals6.data.AppDatabase;
import com.example.eremovals6.data.PromptCodeDao;
import com.example.eremovals6.models.PromptCode;
import com.example.eremovals6.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayPromotionCodesActivity extends AppCompatActivity {
    private ListView listView;
    private PromotionCodeAdapter adapter;
    private PromptCodeDao promptCodeDao;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "create------------------: "); // This log ensures onCreate is called

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_promotion_codes);

        listView = findViewById(R.id.listViewPromotionCodes);

        // Initialize Room database and DAO
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        promptCodeDao = db.promptCodeDao();

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        if (isConnected()) {
            Log.d(TAG, "server------------------: ");
            fetchPromotionCodesFromServer();
        } else {
            Log.d(TAG, "database------------------: ");
            fetchPromotionCodesFromDatabase();
        }
    }

    // Fetch promotion codes from the server when online
    private void fetchPromotionCodesFromServer() {
        Log.d(TAG, "Fetching codes from server..."); // Added log
        apiService.getAllPromotionCodes().enqueue(new Callback<List<PromptCode>>() {

            @Override
            public void onResponse(Call<List<PromptCode>> call, Response<List<PromptCode>> response) {
                Log.d(TAG, "Server response received"); // Added log
                if (response.isSuccessful() && response.body() != null) {
                    List<PromptCode> codes = response.body();

                    Log.d(TAG, "log out------------------: Codes fetched successfully"); // Added missing log
                    Log.d(TAG, "code------------------: " + codes);

                    displayCodes(codes);

                    // Save the retrieved codes to local database for offline access
                    new Thread(() -> {
                        Log.d(TAG, "Saving codes to local database"); // Added log
                        promptCodeDao.clearAll(); // Clear existing data before inserting
                        promptCodeDao.insertAll(codes); // Insert new data
                    }).start();
                } else {
                    Log.d(TAG, "Response unsuccessful or body null"); // Added log
                }
            }

            @Override
            public void onFailure(Call<List<PromptCode>> call, Throwable t) {
                Log.d(TAG, "API call failed: " + t.getMessage()); // Enhanced log
                Toast.makeText(DisplayPromotionCodesActivity.this, "Failed to load data from server", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Fetch promotion codes from the local Room database when offline
    private void fetchPromotionCodesFromDatabase() {
        new Thread(() -> {
            List<PromptCode> codes = promptCodeDao.getAllPromptCodes();
            runOnUiThread(() -> {
                if (codes.isEmpty()) {
                    Toast.makeText(DisplayPromotionCodesActivity.this, "No data available offline", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "code------------------: " + codes);
                    displayCodes(codes);
                }
            });
        }).start();
    }


    private void displayCodes(List<PromptCode> codes) {
        PromotionCodeAdapter adapter = new PromotionCodeAdapter(this, codes, promptCodeDao);
        listView.setAdapter(adapter);
    }


    // Helper method to check network connectivity
    private boolean isConnected() {


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
