package com.qarena.android.util

object PaperTypeLookups {
    const val CQ = "CQ"
    const val MCQ = "MCQ"
    const val WRITTEN = "WRITTEN"

    private val validPaperTypes = setOf(CQ, MCQ, WRITTEN)

    fun allPaperTypes(): List<String> = listOf(CQ, MCQ, WRITTEN)

    fun normalizePaperType(value: String?): String? {
        return value
            ?.trim()
            ?.uppercase()
            ?.takeIf { it in validPaperTypes }
    }

    fun normalizeSupportedPaperTypes(values: List<String>?): List<String> {
        return values.orEmpty()
            .mapNotNull { normalizePaperType(it) }
            .distinct()
    }

    fun resolveSelectedPaperType(
        preferredPaperType: String?,
        supportedPaperTypes: List<String>?
    ): String? {
        val normalizedSupportedPaperTypes = normalizeSupportedPaperTypes(supportedPaperTypes)

        if (normalizedSupportedPaperTypes.isEmpty()) {
            return null
        }

        val normalizedPreferredPaperType = normalizePaperType(preferredPaperType)
        if (normalizedPreferredPaperType != null && normalizedPreferredPaperType in normalizedSupportedPaperTypes) {
            return normalizedPreferredPaperType
        }

        return normalizedSupportedPaperTypes.firstOrNull()
    }

    fun formatSupportedPaperTypesLabel(values: List<String>?): String? {
        val normalized = normalizeSupportedPaperTypes(values)

        if (normalized.isEmpty()) {
            return null
        }

        return when (normalized.size) {
            1 -> "${normalized.first()} only"
            else -> normalized.joinToString(" + ")
        }
    }

    fun isSelectionSupported(values: List<String>?): Boolean {
        return normalizeSupportedPaperTypes(values).isNotEmpty()
    }
}
