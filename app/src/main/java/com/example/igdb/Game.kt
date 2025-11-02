package com.example.igdb

data class Game(
    val id: Int,
    val name: String,
    val background_image: String? = null,
    val description: String? = null,
    val rating: Double? = null,
    val platforms: List<PlatformEntry>?,
    val genres: List<GameGenre>?
)

data class GameGenre(
    val name: String,
    val slug: String
)

data class GameResponse(
    val results: List<Game>
)

data class PlatformEntry(
    val platform: Platform,
    val requirements: Requirements
)

data class Platform(
    val id: Int,
    val name: String,
    val slug: String
)

data class Requirements(
    val minimum: String?,
    val recommended: String?
)
