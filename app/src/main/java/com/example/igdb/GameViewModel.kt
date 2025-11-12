package com.example.igdb

import android.content.Context
import android.util.Log
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
import retrofit2.Retrofit
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class GameViewModel(private val inPreview: Boolean = false) : ViewModel() {
    private val apiService = Retrofit.Builder()
        .baseUrl(RawgApiService.BASE_URL)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(RawgApiService::class.java)

    val games = mutableStateOf<Map<String, List<Game>>>(emptyMap())
    val searchResults = mutableStateOf<List<Game>>(emptyList())
    val isLoading = mutableStateOf(false)
    private var searchJob: Job? = null
    val gameDetails = mutableStateOf<Game?>(null)
    val isGameDetailsLoading = mutableStateOf(false)
    val gameDetailsError = mutableStateOf<String?>(null)
    val relatedGames = mutableStateOf<List<Game>>(emptyList())
    val favoriteGames = mutableStateOf<List<Game>>(emptyList())
    val initialDiscoverGenre = mutableStateOf<Genre?>(null)
    val reviews = mutableStateOf<List<Review>>(emptyList())
    val areReviewsLoading = mutableStateOf(false)
    val hasRated = mutableStateOf(false)

    companion object {
        private const val TAG = "GameViewModel"
    }

    private val firestore = if (inPreview) null else FirebaseFirestore.getInstance()
    private val auth = if (inPreview) null else FirebaseAuth.getInstance()
    private val userId = auth?.currentUser?.uid

    private fun handleError(exception: Exception?, defaultMessage: String): String {
        return when (exception) {
            is UnknownHostException -> {
                Log.e(TAG, "No internet connection", exception)
                "No internet connection. Please check your network and try again."
            }
            is SocketTimeoutException -> {
                Log.e(TAG, "Connection timeout", exception)
                "Connection timeout. Please check your internet and try again."
            }
            is ConnectException -> {
                Log.e(TAG, "Connection failed", exception)
                "Unable to connect. Please check your internet connection."
            }
            else -> {
                Log.e(TAG, defaultMessage, exception)
                exception?.message ?: defaultMessage
            }
        }
    }

    init {
        if (!inPreview) {
            fetchFavorites()
        }
    }

    private fun fetchFavorites() {
        if (userId == null) return
        try {
            firestore?.collection("Users")?.document(userId)?.collection("favorites")?.get()
                ?.addOnSuccessListener { snapshot ->
                    val gamesList = snapshot.documents.mapNotNull { it.toObject(Game::class.java) }
                    favoriteGames.value = gamesList
                }
                ?.addOnFailureListener { exception ->
                        val errorMessage = handleError(exception, "Failed to load favorites")
                        Log.e(TAG, errorMessage, exception)
                    }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to load favorites")
            Log.e(TAG, errorMessage, e)
        }
    }

    open fun isFavorite(gameId: Int): Boolean {
        return favoriteGames.value.any { it.id == gameId }
    }

    fun toggleFavorite(game: Game, context: Context) {
        if (userId == null) return
        try {
            val favoriteRef = firestore?.collection("Users")?.document(userId)?.collection("favorites")?.document(game.id.toString())
            if (isFavorite(game.id)) {
                favoriteRef?.delete()
                    ?.addOnSuccessListener { 
                        Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show()
                        fetchFavorites() 
                    }
                    ?.addOnFailureListener { exception ->
                        val errorMessage = handleError(exception, "Failed to remove from favorites")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, errorMessage, exception)
                    }
            } else {
                val gameData = hashMapOf(
                    "id" to game.id,
                    "name" to game.name,
                    "background_image" to game.background_image,
                    "rating" to game.rating
                )
                favoriteRef?.set(gameData)
                    ?.addOnSuccessListener { 
                        Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show()
                        fetchFavorites() 
                    }
                    ?.addOnFailureListener { exception ->
                        val errorMessage = handleError(exception, "Failed to add to favorites")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, errorMessage, exception)
                    }
            }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to update favorites")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMessage, e)
        }
    }

    open fun fetchGames() {
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
                val deferredGames = categories.map { (category, value) ->
                    async {
                        val response = apiService.getGames(
                            apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                            genres = if (category != "Trending") value else null,
                            ordering = if (category == "Trending") value else null,
                            pageSize = 10
                        )
                        category to response.results
                    }
                }
                games.value = deferredGames.awaitAll().toMap()
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


    open fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            isGameDetailsLoading.value = true
            gameDetailsError.value = null
            try {
                val response = apiService.getGameDetails(
                    id = gameId,
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                )
                gameDetails.value = response
            } catch (e: Exception) {
                gameDetailsError.value = e.message
                e.printStackTrace()
            } finally {
                isGameDetailsLoading.value = false
            }
        }

    }

    open fun fetchRelatedGames(genreSlug: String) {
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

    open fun fetchReviews(gameId: Int, context: Context) {
        areReviewsLoading.value = true
        try {
            firestore?.collection("Reviews")?.document(gameId.toString())?.collection("game_reviews")?.get()
                ?.addOnSuccessListener { documents ->
                    val reviewsList = documents.mapNotNull { it.toObject(Review::class.java) }
                    
                    val reviewsWithPictures = Array<Review?>(reviewsList.size) { null }
                    var completedFetches = 0
                    val totalReviews = reviewsList.size
                    
                    if (totalReviews == 0) {
                        reviews.value = reviewsList
                        areReviewsLoading.value = false
                        return@addOnSuccessListener
                    }
                    
                    reviewsList.forEachIndexed { index, review ->
                        if (review.profilePictureUrl.isNotEmpty() || review.reviewerId.isEmpty()) {
                            reviewsWithPictures[index] = review
                            completedFetches++
                            
                            if (completedFetches == totalReviews) {
                                reviews.value = reviewsWithPictures.filterNotNull().toList()
                                areReviewsLoading.value = false
                            }
                        } else {
                            firestore?.collection("Users")?.document(review.reviewerId)?.get()
                                ?.addOnSuccessListener { userDoc ->
                                    val profilePictureUrl = userDoc.getString("profilePictureUrl") ?: ""
                                    val updatedReview = review.copy(profilePictureUrl = profilePictureUrl)
                                    reviewsWithPictures[index] = updatedReview
                                    completedFetches++
                                    
                                    if (completedFetches == totalReviews) {
                                        reviews.value = reviewsWithPictures.filterNotNull()
                                        areReviewsLoading.value = false
                                    }
                                }
                                ?.addOnFailureListener {
                                    reviewsWithPictures[index] = review
                                    completedFetches++
                                    
                                    if (completedFetches == totalReviews) {
                                        reviews.value = reviewsWithPictures.filterNotNull()
                                        areReviewsLoading.value = false
                                    }
                                }
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Could not fetch reviews")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e(TAG, errorMessage, exception)
                    areReviewsLoading.value = false
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Could not fetch reviews")
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            Log.e(TAG, errorMessage, e)
            areReviewsLoading.value = false
        }
    }

    open fun checkIfUserHasRated(gameId: Int) {
        if (userId == null) return
        try {
            firestore?.collection("Reviews")?.document(gameId.toString())?.collection("game_reviews")?.document(userId)?.get()
                ?.addOnSuccessListener { document ->
                    hasRated.value = document.exists()
                }
                ?.addOnFailureListener { exception ->
                        val errorMessage = handleError(exception, "Failed to check rating status")
                        Log.e(TAG, errorMessage, exception)
                    }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to check rating status")
            Log.e(TAG, errorMessage, e)
        }
    }

    open fun addReview(gameId: Int, rating: String, reviewText: String, context: Context) {
        if (userId == null) {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            firestore?.collection("Users")?.document(userId)?.get()
                ?.addOnSuccessListener { userDoc ->
                    val username = userDoc.getString("username") ?: ""
                    val profilePictureUrl = userDoc.getString("profilePictureUrl") ?: ""
                    val review = Review(
                        reviewerId = userId,
                        reviewerName = username,
                        rating = rating,
                        review = reviewText,
                        profilePictureUrl = profilePictureUrl
                    )
                    firestore?.collection("Reviews")?.document(gameId.toString())
                        ?.collection("game_reviews")?.document(userId)?.set(review)
                        ?.addOnSuccessListener {
                            Toast.makeText(context, "Rate Added", Toast.LENGTH_SHORT).show()
                            fetchReviews(gameId, context)
                            checkIfUserHasRated(gameId)
                        }
                        ?.addOnFailureListener { exception ->
                            val errorMessage = handleError(exception, "Failed to submit review")
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, errorMessage, exception)
                        }
                }
                ?.addOnFailureListener { exception ->
                    val errorMessage = handleError(exception, "Failed to fetch user profile")
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, errorMessage, exception)
                }
        } catch (e: Exception) {
            val errorMessage = handleError(e, "Failed to submit review")
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMessage, e)
        }
    }
}

class PreviewGameViewModel : GameViewModel(inPreview = true) {
    override fun isFavorite(gameId: Int): Boolean = false
    override fun fetchGames() {}
    override fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            isGameDetailsLoading.value = true
            delay(1500)
            gameDetails.value = Game(
                id = 1,
                name = "The Witcher 3: Wild Hunt",
                background_image = "https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg",
                description = "<p>The Witcher 3: Wild Hunt is a 2015 action role-playing game developed and published by Polish developer CD Projekt Red and is based on The Witcher series of fantasy novels by Andrzej Sapkowski. The game is the sequel to the 2011 game The Witcher 2: Assassins of Kings, and the third main installment in The Witcher video game series, played in an open world with a third-person perspective.</p>",
                platforms = listOf(
                    PlatformEntry(
                        platform = Platform(1, "PC", "pc"),
                        requirements = Requirements(
                            minimum = "Intel CPU Core i5-2500K 3.3GHz / AMD CPU Phenom II X4 940",
                            recommended = "Intel CPU Core i7 3770 3.4 GHz / AMD CPU AMD FX-8350 4 GHz"
                        )
                    )
                ),
                rating = 3.6,
                genres = listOf(GameGenre("Action", "Action"), GameGenre("RPG", "RPG"))
            )
            isGameDetailsLoading.value = false
        }
    }
    override fun fetchRelatedGames(genreSlug: String) {}
    override fun fetchReviews(gameId: Int, context: Context) {}
    override fun checkIfUserHasRated(gameId: Int) {}
    override fun addReview(gameId: Int, rating: String, reviewText: String, context: Context) {}
}