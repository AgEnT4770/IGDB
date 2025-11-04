package com.example.igdb.domain.usecase

import com.example.igdb.Remote.RawgApiService
import com.example.igdb.data.Remote.ApiConstants
import com.example.igdb.data.repository.GameRepository
import com.example.igdb.model.Game

class SearchGamesUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(query: String) =
        repository.searchGames(ApiConstants.API_KEY, query)
}
