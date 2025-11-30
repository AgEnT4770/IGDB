package com.example.igdb.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class RawgApiServiceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // LiveData / StateFlow sync

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: RawgApiService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create Moshi with Kotlin adapter
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())   // <-- ADD THIS
            .build()

        // Use Moshi instance in Retrofit
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // <-- USE MOSHI HERE
            .build()
            .create(RawgApiService::class.java)
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `getGames should return GameResponse correctly`() = runTest {
        // Given: A mocked API response
        val mockJson = """
            {
                "results": [
                    {
                        "id": 1,
                        "name": "Mock Game",
                        "background_image": "mock.jpg",
                        "rating": 4.5
                    }
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        // When: Calling getGames
        val response = apiService.getGames(apiKey = "test-key")

        // Then: Response should not be null and have expected data
        assertNotNull(response)
        assertEquals(1, response.results.size)
        assertEquals("Mock Game", response.results[0].name)
        assertEquals(4.5, response.results[0].rating)
    }

    @Test
    fun `getGameDetails should return Game correctly`() = runTest {
        // Given: Mocked Game detail response
        val mockJson = """
            {
                "id": 1,
                "name": "Mock Game",
                "background_image": "mock.jpg",
                "rating": 4.5
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        // When: Calling getGameDetails
        val response = apiService.getGameDetails(id = 1, apiKey = "test-key")

        // Then: Response should match mock data
        assertNotNull(response)
        assertEquals(1, response.id)
        assertEquals("Mock Game", response.name)
        assertEquals("mock.jpg", response.background_image)
        assertEquals(4.5, response.rating)
    }

    @Test
    fun `getGameScreenshots should return GameScreenshotsResponse correctly`() = runTest {
        // Given: Mocked screenshots response
        val mockJson = """
            {
                "results": [
                    {"id": 101, "image": "screenshot1.jpg"},
                    {"id": 102, "image": "screenshot2.jpg"}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        // When: Calling getGameScreenshots
        val response = apiService.getGameScreenshots(id = 1, apiKey = "test-key")

        // Then: Response should match mock data
        assertNotNull(response)
        assertEquals(2, response.results.size)
        assertEquals("screenshot1.jpg", response.results[0].image)
        assertEquals("screenshot2.jpg", response.results[1].image)
    }
}
