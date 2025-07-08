package com.example.madcamp1

data class SearchResponse(
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<PlaceItem>
)

data class PlaceItem(
    val title: String,
    val link: String,
    val category: String,
    val description: String,
    val telephone: String,
    val address: String,
    val roadAddress: String,
    val mapx: Int,
    val mapy: Int
)