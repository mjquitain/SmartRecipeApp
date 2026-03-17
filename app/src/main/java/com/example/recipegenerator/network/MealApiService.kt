package com.example.recipegenerator.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") name: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    @GET("categories.php")
    suspend fun getCategories(): MealResponse

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealResponse
}