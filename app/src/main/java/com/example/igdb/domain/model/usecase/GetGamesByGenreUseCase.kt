package com.example.igdb.domain.usecase

import com.example.igdb.Remote.RawgApiService
import com.example.igdb.data.Remote.ApiConstants
import com.example.igdb.data.repository.GameRepository
import com.example.igdb.model.Game

class GetGamesByGenreUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(genreName: String, genreSlug: String) =
        repository.getGamesByGenre(ApiConstants.API_KEY, genreName, genreSlug)
}
