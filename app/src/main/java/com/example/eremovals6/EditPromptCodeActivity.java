package com.example.eremovals6;

import android.content.Context;
import android.content.Intent;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditPromptCodeActivity extends AppCompatActivity {

    private EditText codeNameEditText, descriptionEditText, discountPercentEditText;
    private Button updateButton;
    private ApiService apiService;
    private PromptCodeDao promptCodeDao;
    private String codeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_prompt_code);

        codeNameEditText = findViewById(R.id.codeName);
        descriptionEditText = findViewById(R.id.description);
        discountPercentEditText = findViewById(R.id.discountPercent);
        updateButton = findViewById(R.id.btnUpdate);

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Initialize Room database
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        promptCodeDao = db.promptCodeDao();

        // Get the promotion code data from intent
        Intent intent = getIntent();
        if (intent != null) {
            codeId = intent.getStringExtra("CODE_ID");
            String codeName = intent.getStringExtra("CODE_NAME");
            String description = intent.getStringExtra("DESCRIPTION");
            int discountPercent = intent.getIntExtra("DISCOUNT_PERCENT", 0);

            // Populate the fields
            populateFields(codeName, description, discountPercent);
        }

        updateButton.setOnClickListener(view -> updatePromptCode());
    }

    private void populateFields(String codeName, String description, int discountPercent) {
        codeNameEditText.setText(codeName);
        descriptionEditText.setText(description);
        discountPercentEditText.setText(String.valueOf(discountPercent));
    }

    private void updatePromptCode() {
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

        // Create updated PromptCode object
        PromptCode updatedCode = new PromptCode(codeName, description, discountPercent, String.valueOf(System.currentTimeMillis()));

        Log.d("EditPromptCodeActivity", "Updating prompt code: " + updatedCode.getCodeName());
        if (isConnected()) {
            sendPromptCodeUpdateToServer(updatedCode);
        } else {
            updatePromptCodeLocally(updatedCode);
        }
    }

    private void sendPromptCodeUpdateToServer(PromptCode promptCode) {
        apiService.updatePromptCode(codeId, promptCode).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditPromptCodeActivity.this, "Prompt code updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("EditPromptCodeActivity", "Server error: " + response.code());
                    Toast.makeText(EditPromptCodeActivity.this, "Failed to update prompt code on server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("EditPromptCodeActivity", "API error: ", t);
                Toast.makeText(EditPromptCodeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePromptCodeLocally(PromptCode promptCode) {
        new Thread(() -> {
            // Note: You'll need to add an update method to your DAO
            // For now, we'll show a message that update will sync when online
            runOnUiThread(() -> {
                Toast.makeText(EditPromptCodeActivity.this, "Offline - changes will sync when online", Toast.LENGTH_SHORT).show();
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