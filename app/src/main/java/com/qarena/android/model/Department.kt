package com.qarena.android.model

data class Department(
    val id: Int,
    val name: String,
    val shortName: String?,
    val universityId: Int
)

fun Department.displayLabel(): String {
    val short = shortName?.trim().orEmpty()
    return if (short.isBlank()) name else "$name ($short)"
}
