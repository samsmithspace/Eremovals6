package com.example.eremovals6.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.eremovals6.models.PromptCode;
import java.util.List;

@Dao
public interface PromptCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PromptCode promptCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PromptCode> promptCodes);

    @Query("SELECT * FROM prompt_code")
    List<PromptCode> getAllPromptCodes();

    // New method to clear all records from the prompt_code table
    @Query("DELETE FROM prompt_code")
    void clearAll();
    @Delete
    void delete(PromptCode code); // Add this method
}
