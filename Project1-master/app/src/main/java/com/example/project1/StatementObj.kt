package com.example.project1

import androidx.room.ColumnInfo
import com.squareup.moshi.Json;

data class StatementObj (
    @Json(name="content") val content : String,
    @Json(name="position") val position : String,
    @Json(name="status") val status : Int
    )