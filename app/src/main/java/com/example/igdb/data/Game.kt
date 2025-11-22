package com.example.igdb.data

data class Game(
    val id: Int = 0,
    val name: String = "",
    val background_image: String? = null,
    val description: String? = null,
    val rating: Double? = null,
    val platforms: List<PlatformEntry>? = null,
    val genres: List<GameGenre>? = null,
    var short_screenshots: List<ShortScreenshot>? = null
)

data class GameGenre(
    val name: String = "",
    val slug: String = ""
)

data class GameResponse(
    val results: List<Game> = emptyList()
)

data class GameScreenshotsResponse(
    val results: List<ShortScreenshot> = emptyList()
)
data class PlatformEntry(
    val platform: Platform = Platform(),
    val requirements: Requirements = Requirements()
)

data class Platform(
    val id: Int = 0,
    val name: String = "",
    val slug: String = ""
)

data class Requirements(
    val minimum: String? = null,
    val recommended: String? = null
)

data class ShortScreenshot(
    val id: Int = 0,
    val image: String = ""
)
