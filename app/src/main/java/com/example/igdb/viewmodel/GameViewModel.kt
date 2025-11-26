package com.example.igdb.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igdb.data.Game
import com.example.igdb.data.GameGenre
import com.example.igdb.data.Genre
import com.example.igdb.data.Platform
import com.example.igdb.data.PlatformEntry
import com.example.igdb.data.RawgApiService
import com.example.igdb.data.Requirements
import com.example.igdb.data.Review
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


    private val badWords = setOf(
        "fuck", "fucking", "fucker", "fuckoff", "motherfucker",
        "shit", "bullshit", "shithead",
        "bitch", "bitches", "sonofabitch",
        "ass", "asshole", "dumbass", "jackass",
        "bastard",
        "cunt",
        "dick", "dickhead",
        "pussy",
        "whore", "slut",
        "moron", "idiot", "stupid",
        "crap",
        "jerk",
        "loser",
        "retard",
        "fag", "faggot",
        "hoe",

        "fck", "fcuk", "fuk",
        "sht", "sh1t",
        "biatch",
        "b!tch", "b1tch",
        "a$$", "a55",
        "d1ck",
        "c0ck",

        "عرص",
        "كسمك",
        "كسها",
        "خول",
        "متناك",
        "زبي",
        "زب",
        "منيك",
        "قحب",
        "قحبة",
        "شرموط",
        "شرموطة",
        "وسخ",
        "حيوان",
        "غبي",
        "مغفل",
        "سيس",
        "متخلف",
        "كلب",
        "يا كلب",
        "ابن الوسخة",
        "ابن المتناك",
        "ابن الشرموطة",
        "منيكك",
        "طيز",
        "متناكين",
        "ولاد الكلب",
        "وسخة",
        "وسخين",
        "زبير",

        "يا ابن المرة",
        "يا ابن الكلب",
        "يا ابن الشرموطة",
        "متناك",
        "منتاك",
        "عرص ابن عرص",
        "كسمكوا",
        "كسختك",
        "يا معفن",
        "يا وسخ",
        "قرف",
        "حمار",
        "حمار انت",
        "طياز",
        "طيظ",
        "طياظ",

        "غبي", "هبيل", "تافه", "مسطول"
        ,"بتطس","بتتس","مدعر","مومس",
        "أهطل","اهطل","اهبل","أهبل",


        "3rs", "ars",
        "kosomak", "kos omak",
        "kosomko",
        "manyak", "manyak",
        "sharmouta", "sharmoota",
        "kleb", "kalb",
        "7omar", "homar",
        "ghabi", "ghaby",
        "m5n2", "mkhanza",
        "kos", "ks",

        // Alternate spellings
        "kosk", "kosmk", "ksmk",
        "nk", "mnek",
        "tiz", "tyz",
        "zpy","zpi"
    )


    private fun isReviewInappropriate(text: String): Boolean {
        val lowercasedText = text.lowercase()
        return badWords.any { badWord -> lowercasedText.contains(badWord) }
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

            is retrofit2.HttpException -> {
                val code = exception.code()
                val message = when (code) {
                    502 -> "Server error. Please try again later."
                    503 -> "Service unavailable. Please try again later."
                    504 -> "Request timeout. Please try again."
                    in 500..599 -> "Server error. Please try again later."
                    else -> "Network error. Please try again."
                }
                Log.e(TAG, "HTTP error $code: $message", exception)
                message
            }

            is kotlinx.coroutines.CancellationException -> {
                Log.w(TAG, "Request cancelled", exception)
                "Request cancelled"
            }

            is com.google.firebase.firestore.FirebaseFirestoreException -> {
                if (exception.message?.contains("offline") == true || exception.message?.contains("UNAVAILABLE") == true) {
                    Log.e(TAG, "Firestore offline", exception)
                    "You're offline. Please check your internet connection."
                } else {
                    Log.e(TAG, "Firestore error: $defaultMessage", exception)
                    exception.message ?: defaultMessage
                }
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
            val favoriteRef =
                firestore?.collection("Users")?.document(userId)?.collection("favorites")
                    ?.document(game.id.toString())
            if (isFavorite(game.id)) {
                favoriteRef?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT)
                            .show()
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
                val errorMessage = handleError(e, "Failed to load games")
                Log.e(TAG, errorMessage, e)
                if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) {
                    Log.w(TAG, "Network error - you may be offline")
                }
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Request was cancelled, this is normal")
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fetchGamesByGenreAndSearch(genreName: String, genreSlug: String, query: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val isTrending = genreName == "Trending"
                val response = apiService.getGames(
                    apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
                    genres = if (!isTrending) genreSlug else null,
                    ordering = if (isTrending) genreSlug else null,
                    pageSize = if (isTrending) 10 else 40,
                    search = query,
                )

                val currentMap = games.value.toMutableMap()
                currentMap[genreName] = response.results
                games.value = currentMap
            } catch (e: Exception) {
                val errorMessage = handleError(e, "Failed to load games")
                Log.e(TAG, errorMessage, e)
                if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) {
                    Log.w(TAG, "Network error - you may be offline")
                }
            } finally {
                isLoading.value = false
            }
        }
    }

//    fun searchGames(query: String) {
//        searchJob?.cancel()
//        searchJob = viewModelScope.launch {
//            delay(300) // Debounce
//            isLoading.value = true
//            try {
//                if (query.isNotBlank()) {
//                    val response = apiService.getGames(
//                        apiKey = "6e5ea525d41242d3b765b9e83eba84e7",
//                        search = query,
//                        pageSize = 20
//                    )
//                    searchResults.value = response.results
//                } else {
//                    searchResults.value = emptyList()
//                }
//            } catch (e: Exception) {
//                val errorMessage = handleError(e, "Failed to search games")
//                Log.e(TAG, errorMessage, e)
//                if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) {
//                    Log.w(TAG, "Network error - you may be offline")
//                }
//                searchResults.value = emptyList()
//            } finally {
//                isLoading.value = false
//            }
//        }
//    }

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
                // Using async to fetch details, screenshots, and stores in parallel
                val detailsDeferred = async {
                    apiService.getGameDetails(
                        id = gameId,
                        apiKey = "6e5ea525d41242d3b765b9e83eba84e7"
                    )
                }
                val screenshotsDeferred = async {
                    apiService.getGameScreenshots(
                        id = gameId,
                        apiKey = "6e5ea525d41242d3b765b9e83eba84e7"
                    )
                }

                // Await all results
                val detailsResponse = detailsDeferred.await()
                val screenshotsResponse = screenshotsDeferred.await()

                // Combine the results into a single Game object
                val fullGameDetails = detailsResponse.copy(
                    short_screenshots = screenshotsResponse.results,
                )

                gameDetails.value = fullGameDetails

            } catch (e: Exception) {
                val errorMessage = handleError(e, "Failed to load game details")
                gameDetailsError.value = errorMessage
                Log.e(TAG, errorMessage, e)
                if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) {
                    Log.w(TAG, "Network error - you may be offline")
                }
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
                val errorMessage = handleError(e, "Failed to load related games")
                Log.e(TAG, errorMessage, e)
                if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) {
                    Log.w(TAG, "Network error - you may be offline")
                }
                relatedGames.value = emptyList()
            }
        }
    }

    open fun fetchReviews(gameId: Int, context: Context) {
        areReviewsLoading.value = true
        try {
            firestore?.collection("Reviews")?.document(gameId.toString())
                ?.collection("game_reviews")?.get()
                ?.addOnSuccessListener { documents ->
                    val initialReviewsList =
                        documents.mapNotNull { it.toObject(Review::class.java) }

                    // Filter for bad words
                    val reviewsList = initialReviewsList.map { review ->
                        if (isReviewInappropriate(review.review)) {
                            review.copy(review = "Reviews containing bad words are automatically filtered")
                        } else {
                            review
                        }
                    }


                    val reviewsWithPictures = Array<Review?>(reviewsList.size) { null }
                    var completedFetches = 0
                    val totalReviews = reviewsList.size

                    if (totalReviews == 0) {
                        reviews.value = reviewsList
                        areReviewsLoading.value = false
                        return@addOnSuccessListener
                    }

                    reviewsList.forEachIndexed { index, review ->
                        if (review.reviewerId.isEmpty()) {
                            reviewsWithPictures[index] = review
                            completedFetches++

                            if (completedFetches == totalReviews) {
                                reviews.value = reviewsWithPictures.filterNotNull().toList()
                                areReviewsLoading.value = false
                            }
                        } else {
                            firestore?.collection("Users")?.document(review.reviewerId)?.get()
                                ?.addOnSuccessListener { userDoc ->
                                    val currentUsername =
                                        userDoc.getString("username") ?: review.reviewerName
                                    val profilePictureUrl = userDoc.getString("profilePictureUrl")
                                        ?: review.profilePictureUrl
                                    val updatedReview = review.copy(
                                        reviewerName = currentUsername,
                                        profilePictureUrl = profilePictureUrl
                                    )
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
            firestore?.collection("Reviews")?.document(gameId.toString())
                ?.collection("game_reviews")?.document(userId)?.get()
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
    init {
        val previewGames = listOf(
            Game(
                id = 1,
                name = "The Witcher 3: Wild Hunt",
                background_image = "https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg",
                rating = 4.7,
                genres = listOf(GameGenre("Action"), GameGenre("RPG"))
            ),
            Game(
                id = 2,
                name = "Red Dead Redemption 2",
                background_image = "https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg",
                rating = 4.8,
                genres = listOf(GameGenre("Action"), GameGenre("Adventure"))
            ),
            Game(
                id = 3,
                name = "Grand Theft Auto V",
                background_image = "https://media.rawg.io/media/games/456/456dea5e1c7e3cd07060c14e96612001.jpg",
                rating = 4.5,
                genres = listOf(GameGenre("Action"), GameGenre("Adventure"))
            )
        )
        games.value = mapOf(
            "Trending" to previewGames,
            "Action" to previewGames,
            "Adventure" to previewGames
        )
    }

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