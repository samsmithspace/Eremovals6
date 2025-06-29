package com.example.eremovals6;

import com.example.eremovals6.models.PriceItem;
import com.example.eremovals6.models.PromptCode;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // ================ PROMOTION CODE ENDPOINTS ================

    @POST("/api/promocode/add")
    Call<Void> addPromptCode(@Body PromptCode promptCode);

    @GET("/api/promocode/getall")
    Call<List<PromptCode>> getAllPromotionCodes();

    // Updated to match your backend PUT route
    @PUT("/api/promocode/{id}")
    Call<Void> updatePromptCode(@Path("id") String id, @Body PromptCode promptCode);

    @DELETE("/api/promocode/delete/{codeId}")
    Call<Void> deletePromotionCode(@Path("codeId") String codeId);

    // ================ PRICE ITEM ENDPOINTS ================

    @POST("/api/price-item")
    Call<Void> addPriceItem(@Body PriceItem priceItem);

    // Correct path as per your backend
    @GET("/api/price-item/api/price-item")
    Call<List<PriceItem>> getAllPriceItems();

    // Updated to match your backend PUT route
    @PUT("/api/price-item/{id}")
    Call<Void> updatePriceItem(@Path("id") String id, @Body PriceItem priceItem);

    @DELETE("/api/price-item/{itemName}")
    Call<Void> deletePriceItem(@Path("itemName") String itemName);
}