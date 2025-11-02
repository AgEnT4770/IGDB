package com.example.igdb

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GameViewModel : ViewModel() {
    private val apiService = Retrofit.Builder()
        .baseUrl(RawgApiService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RawgApiService::class.java)

    val games = mutableStateOf<Map<String, List<Game>>>(emptyMap())
    val searchResults = mutableStateOf<List<Game>>(emptyList())
    val isLoading = mutableStateOf(false)
    private var searchJob: Job? = null
    val gameDetails = mutableStateOf<Game?>(null)
    val relatedGames = mutableStateOf<List<Game>>(emptyList())

    // State to manage the genre selected from the home page
    val initialDiscoverGenre = mutableStateOf<Genre?>(null)

    fun fetchGames() {
        viewModelScope.launch {
            isLoading.value = true

            val categories = mapOf(
                "Trending" to "-popularity",
                "Action" to "action",
                "Adventure" to "adventure",
                "RPG" to "role-playing-games-rpg",
                "Strategy" to "strategy",
                "Indie" to "indie"
            )

            try {
                val deferredGames = categories.map { (category, value) ->
                    async {
                        val response = apiService.getGames(
                            apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                            genres = if (category != "Trending") value else null,
                            ordering = if (category == "Trending") value else null,
                            pageSize = when (category) {
                                "Trending" -> 5
                                "Action" -> 15
                                else -> 10
                            }
                        )
                        category to response.results
                    }
                }

                val gamesMap = deferredGames.awaitAll().toMap().toMutableMap()

                val trendingGames = gamesMap["Trending"] ?: emptyList()
                val actionGames = gamesMap["Action"] ?: emptyList()

                if (trendingGames.isNotEmpty() && actionGames.isNotEmpty()) {
                    val trendingGameIds = trendingGames.map { it.id }.toSet()
                    gamesMap["Action"] =
                        actionGames.filterNot { trendingGameIds.contains(it.id) }.take(10)
                }

                games.value = gamesMap
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
                val isTrending = genreName == "Trending"
                val response = apiService.getGames(
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                    genres = if (!isTrending) genreSlug else null,
                    ordering = if (isTrending) genreSlug else null,
                    pageSize = if (isTrending) 10 else 40
                )

                val currentMap = games.value.toMutableMap()
                currentMap[genreName] = response.results
                games.value = currentMap
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
            delay(300) // Debounce
            isLoading.value = true
            try {
                if (query.isNotBlank()) {
                    val response = apiService.getGames(
                        apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                        search = query,
                        pageSize = 20
                    )
                    searchResults.value = response.results
                } else {
                    searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun setInitialGenreForDiscover(genre: Genre) {
        initialDiscoverGenre.value = genre
    }

    fun consumeInitialGenre() {
        initialDiscoverGenre.value = null
    }


    fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getGameDetails(
                    id = gameId,
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                )
                gameDetails.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun fetchRelatedGames(genreSlug: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getGames(
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                    genres = genreSlug,
                    pageSize = 10
                )
                relatedGames.value = response.results
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
