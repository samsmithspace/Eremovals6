package com.example.eremovals6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eremovals6.data.AppDatabase;
import com.example.eremovals6.data.PromptCodeDao;
import com.example.eremovals6.models.PromptCode;
import com.example.eremovals6.ApiService;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddPromptCodeActivity extends AppCompatActivity {

    private EditText codeNameEditText, descriptionEditText, discountPercentEditText;
    private Button saveButton;
    private ApiService apiService;
    private PromptCodeDao promptCodeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prompt_code);

        codeNameEditText = findViewById(R.id.codeName);
        descriptionEditText = findViewById(R.id.description);
        discountPercentEditText = findViewById(R.id.discountPercent); // New field for discount percentage
        saveButton = findViewById(R.id.btnSave);

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL) // Replace with your server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Initialize Room database
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        promptCodeDao = db.promptCodeDao();

        saveButton.setOnClickListener(view -> addPromptCode());
    }

    private void addPromptCode() {
        String codeName = codeNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String discountPercentText = discountPercentEditText.getText().toString().trim();

        if (codeName.isEmpty() || discountPercentText.isEmpty()) {
            Toast.makeText(this, "Code name and discount percent are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int discountPercent;
        try {
            discountPercent = Integer.parseInt(discountPercentText);
            if (discountPercent < 0 || discountPercent > 100) {
                Toast.makeText(this, "Discount percent must be between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid discount percent", Toast.LENGTH_SHORT).show();
            return;
        }

        long createdDate = new Date().getTime(); // Current timestamp
        String createdDateString = String.valueOf(createdDate); // Convert long to String
        PromptCode promptCode = new PromptCode(codeName, description, discountPercent, createdDateString);


        Log.d("AddPromptCodeActivity", "Adding prompt code: " + promptCode.getCodeName());
        if (isConnected()) {
            sendPromptCodeToServer(promptCode);
        } else {
            savePromptCodeLocally(promptCode);
        }
    }

    private void sendPromptCodeToServer(PromptCode promptCode) {
        apiService.addPromptCode(promptCode).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddPromptCodeActivity.this, "Prompt code added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("AddPromptCodeActivity", "Server error: " + response.code());
                    Toast.makeText(AddPromptCodeActivity.this, "Failed to add prompt code to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AddPromptCodeActivity", "API error: ", t);
                Toast.makeText(AddPromptCodeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePromptCodeLocally(PromptCode promptCode) {
        new Thread(() -> {
            promptCodeDao.insert(promptCode);
            runOnUiThread(() -> {
                Toast.makeText(AddPromptCodeActivity.this, "Saved locally for later sync", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    // Helper method to check network connectivity
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        }
        return false;
    }
}
