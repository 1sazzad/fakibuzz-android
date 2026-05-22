package com.qarena.android.model

data class University(
    val id: Int,
    val name: String,
    val shortName: String?
)

fun University.displayLabel(): String {
    val short = shortName?.trim().orEmpty()
    return if (short.isBlank()) name else "$name ($short)"
}
