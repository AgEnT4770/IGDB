package com.example.igdb.data.repository

import com.example.igdb.Remote.RawgApiService

import com.example.igdb.model.Game

class GameRepository(private val apiService: RawgApiService) {

    suspend fun getGamesByGenre(apiKey: String, genreName: String, genreSlug: String): List<Game> {
        val isTrending = genreName == "Trending"
        val response = apiService.getGames(
            apiKey = apiKey,
            genres = if (!isTrending) genreSlug else null,
            ordering = if (isTrending) genreSlug else null,
            pageSize = if (isTrending) 10 else 40
        )
        return response.results
    }

    suspend fun searchGames(apiKey: String, query: String): List<Game> {
        val response = apiService.getGames(apiKey = apiKey, search = query, pageSize = 20)
        return response.results
    }

    suspend fun getGameDetails(apiKey: String, id: Int): Game {
        return apiService.getGameDetails(apiKey = apiKey, id = id)
    }
}
