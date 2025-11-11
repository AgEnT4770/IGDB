package com.example.igdb

import com.google.firebase.firestore.IgnoreExtraProperties

// Firestore requires a no-argument constructor. Adding default values creates one.
@IgnoreExtraProperties
data class Game(
    val id: Int = 0,
    val name: String = "",
    val background_image: String? = null,
    val description: String? = null,
    val rating: Double? = null,
    val platforms: List<PlatformEntry>? = null,
    val genres: List<GameGenre>? = null
)

@IgnoreExtraProperties
data class GameGenre(
    val name: String = "",
    val slug: String = ""
)

@IgnoreExtraProperties
data class GameResponse(
    val results: List<Game> = emptyList()
)

@IgnoreExtraProperties
data class PlatformEntry(
    val platform: Platform? = null,
    val requirements: Requirements? = null
)

@IgnoreExtraProperties
data class Platform(
    val id: Int = 0,
    val name: String = "",
    val slug: String = ""
)

@IgnoreExtraProperties
data class Requirements(
    val minimum: String? = null,
    val recommended: String? = null
)
