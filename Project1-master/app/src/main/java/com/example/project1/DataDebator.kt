package com.example.project1;

/*
    This is the data class representing the lobbies of the game sessions
 */

import androidx.room.ColumnInfo
import com.squareup.moshi.Json;

data class DataDebator(
    @Json(name = "username") val username : String,
    @Json(name = "position")val position : Boolean,
    @Json(name = "switched")val switched : Boolean
)
