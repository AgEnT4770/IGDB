package com.example.igdb.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.igdb.data.Game
import com.example.igdb.data.GameGenre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    // This rule makes LiveData/StateFlow work synchronously in tests
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // ViewModel to test - ALWAYS use inPreview=true for unit tests
    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        // Set the test dispatcher as the Main dispatcher
        Dispatchers.setMain(testDispatcher)
        // IMPORTANT: Always use inPreview=true to avoid Firebase initialization in unit tests
        viewModel = GameViewModel(inPreview = true)
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher
        Dispatchers.resetMain()
    }

    // Test: Initial State
    @Test
    fun `initial state should have empty games map or preview data`() {
        // Given: A fresh ViewModel in preview mode
        // Then: games should not be null
        assertNotNull(viewModel.games.value)
    }

    @Test
    fun `initial state should not be loading`() {
        // Given: A fresh ViewModel
        // Then: should not be loading
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `initial state should have empty search results`() {
        // Given: A fresh ViewModel
        // Then: search results should be empty
        assertTrue(viewModel.searchResults.value.isEmpty())
    }


    // Test: isFavorite Function
    @Test
    fun `isFavorite should return true when game is in favorites`() {
        // Given: A game in favorites
        val testGame = Game(
            id = 123,
            name = "Test Game",
            background_image = "test.jpg",
            rating = 4.5
        )
        viewModel.favoriteGames.value = listOf(testGame)

        // When: Checking if game is favorite
        val result = viewModel.isFavorite(123)

        // Then: Should return true
        assertTrue(result)
    }

    @Test
    fun `isFavorite should return false when game is not in favorites`() {
        // Given: Empty favorites
        viewModel.favoriteGames.value = emptyList()

        // When: Checking if game is favorite
        val result = viewModel.isFavorite(999)

        // Then: Should return false
        assertFalse(result)
    }

    @Test
    fun `isFavorite should return false when favorites has different games`() {
        // Given: Favorites with different games
        val testGame1 = Game(id = 1, name = "Game 1", background_image = "", rating = 4.0)
        val testGame2 = Game(id = 2, name = "Game 2", background_image = "", rating = 4.5)
        viewModel.favoriteGames.value = listOf(testGame1, testGame2)

        // When: Checking for a game not in the list
        val result = viewModel.isFavorite(999)

        // Then: Should return false
        assertFalse(result)
    }

    // Test: Game Details Loading State
    @Test
    fun `gameDetails should be null initially`() {
        // Given: Fresh ViewModel
        // Then: gameDetails should be null
        assertNull(viewModel.gameDetails.value)
    }

    @Test
    fun `isGameDetailsLoading should be false initially`() {
        // Given: Fresh ViewModel
        // Then: should not be loading
        assertFalse(viewModel.isGameDetailsLoading.value)
    }

    @Test
    fun `gameDetailsError should be null initially`() {
        // Given: Fresh ViewModel
        // Then: error should be null
        assertNull(viewModel.gameDetailsError.value)
    }

    // Test: Related Games
    @Test
    fun `relatedGames should be empty initially`() {
        // Given: Fresh ViewModel
        // Then: related games should be empty
        assertTrue(viewModel.relatedGames.value.isEmpty())
    }

    // Test: Reviews
    @Test
    fun `reviews should be empty initially`() {
        // Given: Fresh ViewModel
        // Then: reviews should be empty
        assertTrue(viewModel.reviews.value.isEmpty())
    }

    @Test
    fun `areReviewsLoading should be false initially`() {
        // Given: Fresh ViewModel
        // Then: should not be loading
        assertFalse(viewModel.areReviewsLoading.value)
    }

    @Test
    fun `hasRated should be false initially`() {
        // Given: Fresh ViewModel
        // Then: hasRated should be false
        assertFalse(viewModel.hasRated.value)
    }

    // Test: Initial Genre for Discover
    @Test
    fun `initialDiscoverGenre should be null initially`() {
        // Given: Fresh ViewModel
        // Then: should be null
        assertNull(viewModel.initialDiscoverGenre.value)
    }

    @Test
    fun `setInitialGenreForDiscover should set the genre`() {
        // Given: A genre
        val testGenre = com.example.igdb.data.Genre(
            name = "Action",
            slug = "action"
        )

        // When: Setting initial genre
        viewModel.setInitialGenreForDiscover(testGenre)

        // Then: Genre should be set
        assertEquals(testGenre, viewModel.initialDiscoverGenre.value)
    }

    @Test
    fun `consumeInitialGenre should clear the genre`() {
        // Given: A genre is set
        val testGenre = com.example.igdb.data.Genre(
            name = "Action",
            slug = "action"
        )
        viewModel.setInitialGenreForDiscover(testGenre)

        // When: Consuming the genre
        viewModel.consumeInitialGenre()

        // Then: Genre should be null
        assertNull(viewModel.initialDiscoverGenre.value)
    }

    // Test: PreviewGameViewModel
    @Test
    fun `PreviewGameViewModel should have preview games loaded`() {
        // Given: Preview ViewModel
        val previewViewModel = PreviewGameViewModel()

        // Then: Should have games in multiple categories
        assertTrue(previewViewModel.games.value.isNotEmpty())
        assertTrue(previewViewModel.games.value.containsKey("Trending"))
        assertTrue(previewViewModel.games.value.containsKey("Action"))
        assertTrue(previewViewModel.games.value.containsKey("Adventure"))
    }

    @Test
    fun `PreviewGameViewModel isFavorite should always return false`() {
        // Given: Preview ViewModel
        val previewViewModel = PreviewGameViewModel()

        // When: Checking any game
        val result = previewViewModel.isFavorite(1)

        // Then: Should always be false in preview mode
        assertFalse(result)
    }

    @Test
    fun `PreviewGameViewModel should have 3 games in each category`() {
        // Given: Preview ViewModel
        val previewViewModel = PreviewGameViewModel()

        // Then: Each category should have 3 games
        assertEquals(3, previewViewModel.games.value["Trending"]?.size)
        assertEquals(3, previewViewModel.games.value["Action"]?.size)
        assertEquals(3, previewViewModel.games.value["Adventure"]?.size)
    }

    @Test
    fun `PreviewGameViewModel fetchGameDetails should set mock game details`() = runTest {
        // Given: Preview ViewModel
        val previewViewModel = PreviewGameViewModel()

        // When: Fetching game details
        previewViewModel.fetchGameDetails(1)

        // Advance time to let the coroutine complete
        advanceUntilIdle()

        // Then: Game details should be set
        assertNotNull(previewViewModel.gameDetails.value)
        assertEquals("The Witcher 3: Wild Hunt", previewViewModel.gameDetails.value?.name)
        assertEquals(1, previewViewModel.gameDetails.value?.id)
    }

    // Test: Game Properties
    @Test
    fun `Game object should have correct properties`() {
        // Given: A Game object
        val game = Game(
            id = 100,
            name = "Test Game",
            background_image = "test-image.jpg",
            rating = 4.5,
            genres = listOf(
                GameGenre("Action", "action"),
                GameGenre("RPG", "rpg")
            )
        )

        // Then: Properties should match
        assertEquals(100, game.id)
        assertEquals("Test Game", game.name)
        assertEquals("test-image.jpg", game.background_image)
        assertNotNull(game.rating)
        assertEquals(4.5, game.rating!!, 0.01)
        assertEquals(2, game.genres?.size)
        assertEquals("Action", game.genres?.get(0)?.name)
    }

    // Test: Favorites State Management
    @Test
    fun `favoriteGames should update correctly`() {
        // Given: Initial empty favorites (or preview data)
        // When: Adding games to favorites
        val game1 = Game(id = 1, name = "Game 1", background_image = "", rating = 4.0)
        val game2 = Game(id = 2, name = "Game 2", background_image = "", rating = 4.5)
        viewModel.favoriteGames.value = listOf(game1, game2)

        // Then: Favorites should contain both games
        assertEquals(2, viewModel.favoriteGames.value.size)
        assertTrue(viewModel.favoriteGames.value.contains(game1))
        assertTrue(viewModel.favoriteGames.value.contains(game2))
    }

    // Test: Search Results State
    @Test
    fun `searchResults should update correctly`() {
        // Given: Initial empty search results
        assertTrue(viewModel.searchResults.value.isEmpty())

        // When: Setting search results
        val game1 = Game(id = 1, name = "Search Result 1", background_image = "", rating = 4.0)
        val game2 = Game(id = 2, name = "Search Result 2", background_image = "", rating = 4.5)
        viewModel.searchResults.value = listOf(game1, game2)

        // Then: Search results should be updated
        assertEquals(2, viewModel.searchResults.value.size)
        assertEquals("Search Result 1", viewModel.searchResults.value[0].name)
        assertEquals("Search Result 2", viewModel.searchResults.value[1].name)
    }

    @Test
    fun `clearing searchResults should result in empty list`() {
        // Given: Search results with games
        val game1 = Game(id = 1, name = "Game 1", background_image = "", rating = 4.0)
        viewModel.searchResults.value = listOf(game1)
        assertEquals(1, viewModel.searchResults.value.size)

        // When: Clearing search results
        viewModel.searchResults.value = emptyList()

        // Then: Should be empty
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    // Test: Games Map State
    @Test
    fun `games map should support multiple categories`() {
        // Given: Games map with multiple categories
        val actionGames = listOf(
            Game(id = 1, name = "Action Game 1", background_image = "", rating = 4.0)
        )
        val rpgGames = listOf(
            Game(id = 2, name = "RPG Game 1", background_image = "", rating = 4.5)
        )

        // When: Setting multiple categories
        viewModel.games.value = mapOf(
            "Action" to actionGames,
            "RPG" to rpgGames
        )

        // Then: Should have both categories
        assertEquals(2, viewModel.games.value.keys.size)
        assertTrue(viewModel.games.value.containsKey("Action"))
        assertTrue(viewModel.games.value.containsKey("RPG"))
        assertEquals(1, viewModel.games.value["Action"]?.size)
        assertEquals(1, viewModel.games.value["RPG"]?.size)
    }

    // Test: Loading States
    @Test
    fun `isLoading state should be mutable`() {
        // Given: Initial loading state is false
        assertFalse(viewModel.isLoading.value)

        // When: Setting to true
        viewModel.isLoading.value = true

        // Then: Should be true
        assertTrue(viewModel.isLoading.value)

        // When: Setting back to false
        viewModel.isLoading.value = false

        // Then: Should be false
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `isGameDetailsLoading state should be mutable`() {
        // Given: Initial state is false
        assertFalse(viewModel.isGameDetailsLoading.value)

        // When: Setting to true
        viewModel.isGameDetailsLoading.value = true

        // Then: Should be true
        assertTrue(viewModel.isGameDetailsLoading.value)
    }

    // Test: Error States
    @Test
    fun `gameDetailsError should be settable`() {
        // Given: Initial error is null
        assertNull(viewModel.gameDetailsError.value)

        // When: Setting an error
        viewModel.gameDetailsError.value = "Network error"

        // Then: Error should be set
        assertEquals("Network error", viewModel.gameDetailsError.value)
    }

    @Test
    fun `gameDetailsError should be clearable`() {
        // Given: An error is set
        viewModel.gameDetailsError.value = "Some error"
        assertNotNull(viewModel.gameDetailsError.value)

        // When: Clearing the error
        viewModel.gameDetailsError.value = null

        // Then: Should be null
        assertNull(viewModel.gameDetailsError.value)
    }
}