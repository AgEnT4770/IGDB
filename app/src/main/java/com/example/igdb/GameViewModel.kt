package com.example.igdb

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val favoriteGames = mutableStateOf<List<Game>>(emptyList())
    val initialDiscoverGenre = mutableStateOf<Genre?>(null)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    init {
        fetchFavorites()
    }

    private fun fetchFavorites() {
        if (userId == null) return
        firestore.collection("users").document(userId).collection("favorites").get()
            .addOnSuccessListener { snapshot ->
                val games = mutableListOf<Game>()
                for (document in snapshot.documents) {
                    val id = (document.get("id") as? Number)?.toInt() ?: 0
                    val name = document.getString("name") ?: ""
                    val backgroundImage = document.getString("background_image")
                    val rating = (document.get("rating") as? Number)?.toDouble()
                    if (id != 0) {
                        games.add(
                            Game(
                                id = id,
                                name = name,
                                background_image = backgroundImage,
                                rating = rating,
                                description = null,
                                platforms = null,
                                genres = null
                            )
                        )
                    }
                }
                favoriteGames.value = games
            }
    }

    fun isFavorite(gameId: Int): Boolean {
        return favoriteGames.value.any { it.id == gameId }
    }

    fun toggleFavorite(game: Game, context: Context) {
        if (userId == null) return
        val favoriteRef = firestore.collection("users").document(userId).collection("favorites").document(game.id.toString())
        if (isFavorite(game.id)) {
            favoriteRef.delete().addOnSuccessListener { 
                Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show()
                fetchFavorites() 
            }
        } else {
            val gameData = hashMapOf(
                "id" to game.id,
                "name" to game.name,
                "background_image" to game.background_image,
                "rating" to game.rating
            )
            favoriteRef.set(gameData).addOnSuccessListener { 
                Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show()
                fetchFavorites() 
            }
        }
    }

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

    fun fetchGamesByGenreAndSearch(genreName: String, genreSlug: String , query: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val isTrending = genreName == "Trending"
                val response = apiService.getGames(
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                    genres = if (!isTrending) genreSlug else null,
                    ordering = if (isTrending) genreSlug else null,
                    pageSize = if (isTrending) 10 else 40 ,
                    search = query,
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