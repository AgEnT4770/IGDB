package com.example.igdb.data

import com.example.igdb.data.Game
import com.example.igdb.data.GameResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun getGames(
        @Query("key") apiKey: String,
        @Query("genres") genres: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null,
    ): GameResponse


    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") id: Int,
        @Query("key") apiKey: String
    ): Game

    companion object {
        const val BASE_URL = "https://api.rawg.io/api/"
    }
}
