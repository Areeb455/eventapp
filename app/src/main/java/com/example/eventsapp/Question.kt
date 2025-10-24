package com.example.eventsapp

data class Question(
    val id: String,
    val text: String,
    var voteCount: Int = 0 // We will update this from RTDB
)