package com.example.igdb.data.repository

import com.example.igdb.Remote.RawgApiService
import com.example.igdb.data.Remote.ApiConstants
import com.example.igdb.domain.repository.GameRepository
import com.example.igdb.model.Game

class GameRepositoryImpl(
    private val apiService: RawgApiService
) : GameRepository {

    override suspend fun getGames(
        genres: String?,
        ordering: String?,
        pageSize: Int
    ): List<Game> {
        val response = apiService.getGames(
            apiKey = ApiConstants.API_KEY,
            genres = genres,
            ordering = ordering,
            pageSize = pageSize
        )
        return response.results
    }

    override suspend fun getGamesByGenre(
        genreName: String,
        genreSlug: String
    ): List<Game> {
        val isTrending = genreName == "Trending"
        val response = apiService.getGames(
            apiKey = ApiConstants.API_KEY,
            genres = if (!isTrending) genreSlug else null,
            ordering = if (isTrending) genreSlug else null,
            pageSize = if (isTrending) 10 else 40
        )
        return response.results
    }

    override suspend fun searchGames(query: String): List<Game> {
        val response = apiService.getGames(
            apiKey = ApiConstants.API_KEY,
            search = query,
            pageSize = 20
        )
        return response.results
    }

    override suspend fun getGameDetails(id: Int): Game {
        return apiService.getGameDetails(
            id = id,
            apiKey = ApiConstants.API_KEY
        )
    }
}
