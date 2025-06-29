// Updated PriceItem.java
package com.example.eremovals6.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "priceitems")
public class PriceItem {
    @PrimaryKey(autoGenerate = true)
    private int id; // Local Room database ID

    @SerializedName("_id")
    private String serverId; // MongoDB _id from server

    private String itemName;
    private String category;
    private double normalPrice;
    private double helperPrice;

    // Constructor with parameter names matching field names
    public PriceItem(String itemName, String category, double normalPrice, double helperPrice) {
        this.itemName = itemName;
        this.category = category;
        this.normalPrice = normalPrice;
        this.helperPrice = helperPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getNormalPrice() { return normalPrice; }
    public void setNormalPrice(double normalPrice) { this.normalPrice = normalPrice; }

    public double getHelperPrice() { return helperPrice; }
    public void setHelperPrice(double helperPrice) { this.helperPrice = helperPrice; }
}

// ================================================================

