package com.example.igdb.domain.usecase


import com.example.igdb.domain.repository.GameRepository
import com.example.igdb.model.Game

class GetRelatedGamesUseCase(
    private val repository: GameRepository
) {
    suspend operator fun invoke(genreSlug: String): List<Game> {
        return repository.getGames(
            genres = genreSlug,
            ordering = null,
            pageSize = 10
        )
    }
}