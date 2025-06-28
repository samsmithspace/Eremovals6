package com.example.eremovals6.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.eremovals6.models.PriceItem;
import java.util.List;

@Dao
public interface PriceItemDao {
    @Insert
    void insert(PriceItem priceItem);

    @Update
    void update(PriceItem priceItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PriceItem> priceItems);

    @Query("SELECT * FROM priceitems ORDER BY category")
    List<PriceItem> getAllPriceItems();
    @Query("DELETE FROM priceitems WHERE itemName = :itemName") // Adjust column name as needed
    void deleteByName(String itemName);
    // New method to clear all records from the table
    @Query("DELETE FROM priceitems")
    void clearAll();
}
