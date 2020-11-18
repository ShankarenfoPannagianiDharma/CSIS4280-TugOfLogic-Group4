package com.example.project1

import com.squareup.moshi.Json

//data class for json url asset
data class DataUrl(
    @Json(name = "server") val server : String,
    @Json(name = "port")val port : String
)