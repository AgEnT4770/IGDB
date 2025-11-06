package com.example.igdb.domain.repository

import com.example.igdb.model.Game

interface GameRepository {
    suspend fun getGames(genres: String?, ordering: String?, pageSize: Int): List<Game>
    suspend fun getGamesByGenre(genreName: String, genreSlug: String): List<Game>
    suspend fun searchGames(query: String): List<Game>
    suspend fun getGameDetails(id: Int): Game
}
