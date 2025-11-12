package com.example.igdb

data class Review(
    val reviewerId: String = "",
    val reviewerName: String = "",
    val review: String = "",
    val rating: String = "",
    val profilePictureUrl: String = ""
)

data class User(
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String = ""
)