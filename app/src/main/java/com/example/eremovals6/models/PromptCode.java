// Updated PromptCode.java
package com.example.eremovals6.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.eremovals6.utils.TimestampConverter;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "prompt_code")
@TypeConverters(TimestampConverter.class)
public class PromptCode {

    @PrimaryKey(autoGenerate = true)
    private int id; // Local Room database ID

    @SerializedName("_id")
    private String serverId; // MongoDB _id from server

    private String codeName;
    private String description;
    private int discountPercent;
    private String createdDate;

    // Constructor
    public PromptCode(String codeName, String description, int discountPercent, String createdDate) {
        this.codeName = codeName;
        this.description = description;
        this.discountPercent = discountPercent;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}