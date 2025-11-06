package com.example.igdb.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igdb.Genre

import com.example.igdb.domain.usecase.GetGamesByGenreUseCase
import com.example.igdb.domain.usecase.SearchGamesUseCase
import com.example.igdb.domain.usecase.GetGameDetailsUseCase
import com.example.igdb.domain.usecase.GetRelatedGamesUseCase
import com.example.igdb.model.Game
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(
    private val getGamesByGenreUseCase: GetGamesByGenreUseCase,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val getGameDetailsUseCase: GetGameDetailsUseCase,
    private val getRelatedGamesUseCase: GetRelatedGamesUseCase
) : ViewModel() {

    // UI states
    val games = mutableStateOf<Map<String, List<Game>>>(emptyMap())
    val searchResults = mutableStateOf<List<Game>>(emptyList())
    val isLoading = mutableStateOf(false)
    val gameDetails = mutableStateOf<Game?>(null)
    val relatedGames = mutableStateOf<List<Game>>(emptyList())
    val initialDiscoverGenre = mutableStateOf<Genre?>(null)
    private var searchJob: Job? = null

    fun fetchGames() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val categories = mapOf(
                    "Trending" to "-popularity",
                    "Action" to "action",
                    "Adventure" to "adventure",
                    "RPG" to "role-playing-games-rpg",
                    "Strategy" to "strategy",
                    "Indie" to "indie"
                )

                val fetchedGames = mutableMapOf<String, List<Game>>()

                for ((category, value) in categories) {
                    val result = getGamesByGenreUseCase(genreName = category, genreSlug = value)
                    fetchedGames[category] = result
                }

                // Optional: filter Action duplicates
                val trendingGames = fetchedGames["Trending"] ?: emptyList()
                val actionGames = fetchedGames["Action"] ?: emptyList()
                if (trendingGames.isNotEmpty() && actionGames.isNotEmpty()) {
                    val trendingIds = trendingGames.map { it.id }.toSet()
                    fetchedGames["Action"] = actionGames.filterNot { it.id in trendingIds }.take(10)
                }

                games.value = fetchedGames
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchGamesByGenre(genreName: String, genreSlug: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val results = getGamesByGenreUseCase(genreName, genreSlug)
                val current = games.value.toMutableMap()
                current[genreName] = results
                games.value = current
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun searchGames(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            isLoading.value = true
            try {
                searchResults.value =
                    if (query.isNotBlank()) searchGamesUseCase(query) else emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            try {
                gameDetails.value = getGameDetailsUseCase(gameId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRelatedGames(genreSlug: String) {
        viewModelScope.launch {
            try {
                relatedGames.value = getRelatedGamesUseCase(genreSlug)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun setInitialGenreForDiscover(genre: Genre) {
        initialDiscoverGenre.value = genre
    }

    fun consumeInitialGenre() {
        initialDiscoverGenre.value = null
    }
}
