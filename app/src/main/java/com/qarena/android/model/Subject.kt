package com.qarena.android.model

import com.qarena.android.util.AcademicProfile
import com.qarena.android.util.PaperTypeLookups

data class Subject(
    val id: Int? = null,
    val subjectCode: String,
    val subjectName: String,
    val universityId: Int?,
    val departmentId: Int?,
    val academicLevel: String? = null,
    val group: String? = null,
    val supportedPaperTypes: List<String>? = null
)

fun Subject.displayLabel(): String {
    val code = subjectCode.trim()
    val name = subjectName.trim()
    return if (code.isBlank() && name.isBlank()) {
        ""
    } else if (code.isBlank()) {
        name
    } else if (name.isBlank()) {
        code
    } else {
        "$code — $name"
    }
}

fun Subject.displaySubtitle(): String? {
    val normalizedLevel = academicLevel?.trim()?.takeIf { it.isNotBlank() }
    val levelLabel = normalizedLevel?.let { AcademicProfile.academicLevelLabel(it) }?.takeIf { it.isNotBlank() }

    if (levelLabel.equals("University", ignoreCase = true)) {
        return null
    }

    val pieces = mutableListOf<String>()

    levelLabel?.takeIf { it.isNotBlank() }?.let { pieces.add(it) }

    group?.trim()?.takeIf { it.isNotBlank() }?.let { streamGroup ->
        pieces.add(AcademicProfile.streamGroupLabel(streamGroup))
    }

    PaperTypeLookups.formatSupportedPaperTypesLabel(supportedPaperTypes)?.let { paperTypesLabel ->
        pieces.add(paperTypesLabel)
    }

    return pieces.joinToString(" · ").takeIf { it.isNotBlank() }
}
