package com.example.eremovals6.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.eremovals6.models.PromptCode;
import com.example.eremovals6.models.PriceItem;

@Database(entities = {PriceItem.class, PromptCode.class}, version = 4, exportSchema = false) // Incremented version
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract PromptCodeDao promptCodeDao();
    public abstract PriceItemDao priceItemDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "removal_service_db")

                    .fallbackToDestructiveMigration() // Use destructive migration for other cases
                    .build();
        }
        return instance;
    }


}
