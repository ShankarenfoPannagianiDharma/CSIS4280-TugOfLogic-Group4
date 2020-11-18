package com.example.project1;

/*
    This is the data class representing the lobbies of the game sessions
 */

import com.squareup.moshi.Json;

data class DataLobby(
    @Json(name = "MainClaim") val MainClaim : String,
    @Json(name = "Judge")val Judge : String
)
