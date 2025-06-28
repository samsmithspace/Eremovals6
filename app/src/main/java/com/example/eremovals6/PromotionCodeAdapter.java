package com.example.eremovals6;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.eremovals6.data.PromptCodeDao;
import com.example.eremovals6.models.PromptCode;

import java.util.List;
import android.util.Log;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PromotionCodeAdapter extends ArrayAdapter<PromptCode> {
    private static final String TAG = "PromotionCodeAdapter";
    private final Context context;
    private final List<PromptCode> codes;
    private final PromptCodeDao promptCodeDao;
    private ApiService apiService;

    public PromotionCodeAdapter(Context context, List<PromptCode> codes, PromptCodeDao promptCodeDao) {
        super(context, R.layout.list_item_promotion_code, codes);
        this.context = context;
        this.codes = codes;
        this.promptCodeDao = promptCodeDao;

        // Initialize Retrofit for API calls
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_promotion_code, parent, false);
        }

        // Get the current code
        PromptCode code = codes.get(position);

        // Find views
        TextView codeTextView = convertView.findViewById(R.id.tvPromotionCode);
        Button deleteButton = convertView.findViewById(R.id.btnDelete);

        // Set the promotion code text
        codeTextView.setText(code.getCodeName() + " - " + code.getDescription());

        // Set delete button click listener
        deleteButton.setOnClickListener(v -> {
            Log.d(TAG, "Attempting to delete code: " + code.getCodeName());

            if (isConnected()) {
                // If online, delete the code from the server
                apiService.deletePromotionCode(code.getCodeName()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Code deleted from server successfully");
                            // Remove from local UI
                            codes.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Failed to delete code from server");
                            Toast.makeText(context, "Failed to delete code from server", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error deleting code from server: " + t.getMessage());
                        Toast.makeText(context, "Error deleting code from server", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // If offline, delete the code locally
                new Thread(() -> {
                    promptCodeDao.delete(code); // Delete code from local Room database
                    Log.d(TAG, "Code deleted from local database");

                    // Update local UI
                    codes.remove(position);
                    ((DisplayPromotionCodesActivity) context).runOnUiThread(() -> {
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
}



