package com.example.eremovals6;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addPromptCodeButton = findViewById(R.id.btnAddPromptCode);
        Button addPriceItemButton = findViewById(R.id.btnAddPriceItem);
        Button viewPromotionCodesButton = findViewById(R.id.btnViewPromotionCodes);
        Button viewPriceItemsButton = findViewById(R.id.btnViewPriceItems);

        viewPromotionCodesButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DisplayPromotionCodesActivity.class)));
        viewPriceItemsButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DisplayPriceItemsActivity.class)));

        addPromptCodeButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPromptCodeActivity.class);
            startActivity(intent);
        });

        addPriceItemButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPriceItemActivity.class);
            startActivity(intent);
        });

    }
}
