package com.qarena.android.util

import com.qarena.android.data.remote.dto.ProfileUpdateRequest
import com.qarena.android.data.remote.dto.RegisterRequest
import com.qarena.android.data.remote.dto.UserResponse

data class AcademicProfileInput(
    val academicLevel: String,
    val universityId: Int? = null,
    val departmentId: Int? = null,
    val curriculum: String? = null,
    val streamGroup: String? = null,
    val classLevel: String? = null,
    val program: String? = null,
    val batchSession: String? = null
)

object AcademicProfile {
    const val ACADEMIC_LEVEL_UNIVERSITY = "university"
    const val ACADEMIC_LEVEL_SSC = "ssc"
    const val ACADEMIC_LEVEL_HSC = "hsc"
    const val ACADEMIC_LEVEL_SCHOOL = "school"
    const val ACADEMIC_LEVEL_DIPLOMA = "diploma"
    const val ACADEMIC_LEVEL_ADMISSION = "admission"

    const val INSTITUTION_TYPE_UNIVERSITY = "university"
    const val INSTITUTION_TYPE_SCHOOL = "school"
    const val INSTITUTION_TYPE_COLLEGE = "college"
    const val INSTITUTION_TYPE_POLYTECHNIC = "polytechnic"
    const val INSTITUTION_TYPE_COACHING = "coaching"

    const val CURRICULUM_NATIONAL = "national"
    const val CURRICULUM_ENGLISH_VERSION = "english_version"
    const val CURRICULUM_MADRASA = "madrasa"
    const val CURRICULUM_UNIVERSITY_SPECIFIC = "university_specific"

    const val STREAM_GROUP_SCIENCE = "science"
    const val STREAM_GROUP_BUSINESS_STUDIES = "business_studies"
    const val STREAM_GROUP_COMMERCE = "commerce"
    const val STREAM_GROUP_HUMANITIES = "humanities"

    fun availableAcademicLevels(): List<String> {
        return listOf(
            ACADEMIC_LEVEL_UNIVERSITY,
            ACADEMIC_LEVEL_SSC,
            ACADEMIC_LEVEL_HSC
        )
    }

    fun academicLevelLabel(value: String?): String {
        return when (normalized(value)) {
            ACADEMIC_LEVEL_UNIVERSITY -> "University"
            ACADEMIC_LEVEL_SSC -> "SSC"
            ACADEMIC_LEVEL_HSC -> "HSC"
            ACADEMIC_LEVEL_SCHOOL -> "School"
            ACADEMIC_LEVEL_DIPLOMA -> "Diploma"
            ACADEMIC_LEVEL_ADMISSION -> "Admission"
            else -> value?.trim().orEmpty()
        }
    }

    fun curriculumLabel(value: String?): String {
        return when (normalized(value)) {
            CURRICULUM_NATIONAL -> "National"
            CURRICULUM_ENGLISH_VERSION -> "English version"
            CURRICULUM_MADRASA -> "Madrasa"
            CURRICULUM_UNIVERSITY_SPECIFIC -> "University specific"
            else -> value?.trim().orEmpty()
        }
    }

    fun streamGroupLabel(value: String?): String {
        return when (normalized(value)) {
            STREAM_GROUP_SCIENCE -> "Science"
            STREAM_GROUP_BUSINESS_STUDIES,
            STREAM_GROUP_COMMERCE -> "Business Studies"
            STREAM_GROUP_HUMANITIES -> "Humanities"
            "common" -> "Common"
            else -> value?.trim().orEmpty()
        }
    }

    fun studentStreamGroupLabel(value: String?): String {
        return when (normalizeStudentStreamGroup(value)) {
            STREAM_GROUP_SCIENCE -> "Science"
            STREAM_GROUP_BUSINESS_STUDIES -> "Business Studies"
            STREAM_GROUP_HUMANITIES -> "Humanities"
            else -> ""
        }
    }

    fun classLevelLabel(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        return if (trimmed.isBlank()) "" else "Class $trimmed"
    }

    fun curriculumOptions(): List<String> {
        return listOf(
            CURRICULUM_NATIONAL,
            CURRICULUM_ENGLISH_VERSION,
            CURRICULUM_MADRASA
        )
    }

    fun syllabusCurriculumOptions(): List<String> {
        return curriculumOptions()
    }

    fun streamGroupOptions(): List<String> {
        return listOf(
            STREAM_GROUP_SCIENCE,
            STREAM_GROUP_BUSINESS_STUDIES,
            STREAM_GROUP_HUMANITIES
        )
    }

    fun studentStreamGroupOptions(): List<String> {
        return streamGroupOptions()
    }

    fun classLevelOptions(academicLevel: String?): List<String> {
        return when (normalized(academicLevel)) {
            ACADEMIC_LEVEL_SSC -> listOf("9", "10")
            ACADEMIC_LEVEL_HSC -> listOf("11", "12")
            else -> emptyList()
        }
    }

    fun defaultSyllabusCurriculum(): String {
        return CURRICULUM_NATIONAL
    }

    fun defaultClassLevel(academicLevel: String?): String? {
        return when (normalized(academicLevel)) {
            ACADEMIC_LEVEL_SSC -> "10"
            ACADEMIC_LEVEL_HSC -> "12"
            else -> null
        }
    }

    fun normalizeInput(
        academicLevel: String?,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null
    ): AcademicProfileInput {
        val normalizedLevel = normalized(academicLevel)

        return if (isUniversityScoped(normalizedLevel)) {
            AcademicProfileInput(
                academicLevel = normalizedLevel.ifBlank { ACADEMIC_LEVEL_UNIVERSITY },
                universityId = universityId,
                departmentId = departmentId,
                curriculum = CURRICULUM_UNIVERSITY_SPECIFIC,
                streamGroup = null,
                classLevel = null,
                program = program?.trim()?.takeIf { it.isNotBlank() },
                batchSession = batchSession?.trim()?.takeIf { it.isNotBlank() }
            )
        } else if (isSyllabusScoped(normalizedLevel)) {
            AcademicProfileInput(
                academicLevel = normalizedLevel,
                curriculum = normalized(curriculum).ifBlank { defaultSyllabusCurriculum() },
                streamGroup = normalizeStudentStreamGroup(streamGroup),
                classLevel = normalized(classLevel).ifBlank { null },
                program = null,
                batchSession = null
            )
        } else {
            AcademicProfileInput(
                academicLevel = normalizedLevel,
                universityId = universityId,
                departmentId = departmentId,
                curriculum = normalized(curriculum).ifBlank { null },
                streamGroup = normalizeStudentStreamGroup(streamGroup),
                classLevel = normalized(classLevel).ifBlank { null },
                program = program?.trim()?.takeIf { it.isNotBlank() },
                batchSession = batchSession?.trim()?.takeIf { it.isNotBlank() }
            )
        }
    }

    fun toAcademicProfileInput(
        academicLevel: String?,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null
    ): AcademicProfileInput {
        return normalizeInput(
            academicLevel = academicLevel,
            universityId = universityId,
            departmentId = departmentId,
            curriculum = curriculum,
            streamGroup = streamGroup,
            classLevel = classLevel,
            program = program,
            batchSession = batchSession
        )
    }

    fun validateRegistration(input: RegisterFormInput): String? {
        val academicLevel = normalized(input.academicLevel)

        return when {
            academicLevel.isBlank() -> "Academic level is required"
            isUniversityScoped(academicLevel) && input.universityId == null -> "University is required"
            isUniversityScoped(academicLevel) && input.departmentId == null -> "Department is required"
            isSyllabusScoped(academicLevel) && input.curriculum.isNullOrBlank() -> "Curriculum is required"
            isSyllabusScoped(academicLevel) && input.streamGroup.isNullOrBlank() -> "Group is required"
            !input.termsAccepted -> "You must accept the terms to register"
            else -> null
        }
    }

    fun validateProfile(input: AcademicProfileInput): String? {
        val normalizedInput = normalizeInput(
            academicLevel = input.academicLevel,
            universityId = input.universityId,
            departmentId = input.departmentId,
            curriculum = input.curriculum,
            streamGroup = input.streamGroup,
            classLevel = input.classLevel,
            program = input.program,
            batchSession = input.batchSession
        )

        return when {
            normalizedInput.academicLevel.isBlank() -> "Academic level is required"
            isUniversityScoped(normalizedInput.academicLevel) && normalizedInput.universityId == null -> "University is required"
            isUniversityScoped(normalizedInput.academicLevel) && normalizedInput.departmentId == null -> "Department is required"
            isSyllabusScoped(normalizedInput.academicLevel) && normalizedInput.curriculum.isNullOrBlank() -> "Curriculum is required"
            isSyllabusScoped(normalizedInput.academicLevel) && normalizedInput.streamGroup.isNullOrBlank() -> "Group is required"
            else -> null
        }
    }

    fun switchAcademicLevel(
        profile: AcademicProfileInput,
        academicLevel: String?
    ): AcademicProfileInput {
        val normalizedLevel = normalized(academicLevel)

        return when {
            isUniversityScoped(normalizedLevel) -> AcademicProfileInput(
                academicLevel = normalizedLevel.ifBlank { ACADEMIC_LEVEL_UNIVERSITY },
                universityId = profile.universityId,
                departmentId = profile.departmentId,
                curriculum = CURRICULUM_UNIVERSITY_SPECIFIC,
                streamGroup = null,
                classLevel = null,
                program = profile.program?.trim()?.takeIf { it.isNotBlank() },
                batchSession = profile.batchSession?.trim()?.takeIf { it.isNotBlank() }
            )

            isSyllabusScoped(normalizedLevel) -> AcademicProfileInput(
                academicLevel = normalizedLevel,
                universityId = null,
                departmentId = null,
                curriculum = defaultSyllabusCurriculum(),
                streamGroup = normalizeStudentStreamGroup(profile.streamGroup) ?: STREAM_GROUP_SCIENCE,
                classLevel = null,
                program = null,
                batchSession = null
            )

            else -> normalizeInput(
                academicLevel = academicLevel,
                universityId = profile.universityId,
                departmentId = profile.departmentId,
                curriculum = profile.curriculum,
                streamGroup = profile.streamGroup,
                classLevel = profile.classLevel,
                program = profile.program,
                batchSession = profile.batchSession
            )
        }
    }

    fun isUniversityScoped(academicLevel: String?): Boolean {
        return normalized(academicLevel) == ACADEMIC_LEVEL_UNIVERSITY
    }

    fun isSyllabusScoped(academicLevel: String?): Boolean {
        return normalized(academicLevel) in setOf(ACADEMIC_LEVEL_SSC, ACADEMIC_LEVEL_HSC)
    }

    fun institutionTypeFor(academicLevel: String?): String? {
        return when (normalized(academicLevel)) {
            ACADEMIC_LEVEL_UNIVERSITY -> INSTITUTION_TYPE_UNIVERSITY
            ACADEMIC_LEVEL_SSC -> INSTITUTION_TYPE_SCHOOL
            ACADEMIC_LEVEL_HSC -> INSTITUTION_TYPE_COLLEGE
            ACADEMIC_LEVEL_SCHOOL -> INSTITUTION_TYPE_SCHOOL
            ACADEMIC_LEVEL_DIPLOMA -> INSTITUTION_TYPE_POLYTECHNIC
            ACADEMIC_LEVEL_ADMISSION -> INSTITUTION_TYPE_COACHING
            else -> null
        }
    }

    fun isProfileComplete(user: UserResponse): Boolean {
        val academicLevel = normalized(user.academicLevel)

        return when {
            academicLevel == ACADEMIC_LEVEL_UNIVERSITY ||
                (academicLevel.isBlank() && resolveAcademicLevel(user) == ACADEMIC_LEVEL_UNIVERSITY) -> {
                user.universityId != null && user.departmentId != null
            }

            academicLevel in setOf(ACADEMIC_LEVEL_SSC, ACADEMIC_LEVEL_HSC) -> {
                !user.curriculum.isNullOrBlank() &&
                    !normalizeStudentStreamGroup(user.streamGroup).isNullOrBlank()
            }

            academicLevel.isBlank() -> {
                when (resolveAcademicLevel(user)) {
                    ACADEMIC_LEVEL_UNIVERSITY -> {
                        user.universityId != null && user.departmentId != null
                    }

                    ACADEMIC_LEVEL_SSC, ACADEMIC_LEVEL_HSC -> {
                        !user.curriculum.isNullOrBlank() &&
                            !normalizeStudentStreamGroup(user.streamGroup).isNullOrBlank()
                    }

                    else -> false
                }
            }

            else -> false
        }
    }

    fun buildRegisterRequest(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        academicLevel: String,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null,
        termsAccepted: Boolean
    ): RegisterRequest {
        val normalizedInput = normalizeInput(
            academicLevel = academicLevel,
            universityId = universityId,
            departmentId = departmentId,
            curriculum = curriculum,
            streamGroup = streamGroup,
            classLevel = classLevel,
            program = program,
            batchSession = batchSession
        )
        val universityScoped = isUniversityScoped(normalizedInput.academicLevel)
        val syllabusScoped = isSyllabusScoped(normalizedInput.academicLevel)

        return RegisterRequest(
            full_name = fullName.trim(),
            email = email.trim(),
            phone_number = phone.trim(),
            password = password,
            academic_level = backendAcademicLevel(normalizedInput.academicLevel),
            institution_type = institutionTypeFor(normalizedInput.academicLevel),
            curriculum = when {
                universityScoped -> CURRICULUM_UNIVERSITY_SPECIFIC
                syllabusScoped -> normalizedInput.curriculum
                else -> normalizedInput.curriculum
            },
            stream_group = if (syllabusScoped) normalizedInput.streamGroup else null,
            class_level = null,
            university_id = if (universityScoped) normalizedInput.universityId else null,
            department_id = if (universityScoped) normalizedInput.departmentId else null,
            program = if (universityScoped) normalizedInput.program else null,
            batch_session = if (universityScoped) normalizedInput.batchSession else null,
            terms_accepted = termsAccepted
        )
    }

    fun buildProfileUpdateRequest(
        fullName: String? = null,
        academicLevel: String,
        universityId: Int? = null,
        departmentId: Int? = null,
        curriculum: String? = null,
        streamGroup: String? = null,
        classLevel: String? = null,
        program: String? = null,
        batchSession: String? = null
    ): ProfileUpdateRequest {
        val normalizedInput = normalizeInput(
            academicLevel = academicLevel,
            universityId = universityId,
            departmentId = departmentId,
            curriculum = curriculum,
            streamGroup = streamGroup,
            classLevel = classLevel,
            program = program,
            batchSession = batchSession
        )
        val universityScoped = isUniversityScoped(normalizedInput.academicLevel)
        val syllabusScoped = isSyllabusScoped(normalizedInput.academicLevel)

        return ProfileUpdateRequest(
            fullName = fullName?.trim()?.takeIf { it.isNotBlank() },
            academicLevel = backendAcademicLevel(normalizedInput.academicLevel),
            institutionType = institutionTypeFor(normalizedInput.academicLevel),
            curriculum = when {
                universityScoped -> CURRICULUM_UNIVERSITY_SPECIFIC
                syllabusScoped -> normalizedInput.curriculum
                else -> normalizedInput.curriculum
            },
            streamGroup = if (syllabusScoped) normalizedInput.streamGroup else null,
            classLevel = null,
            universityId = if (universityScoped) normalizedInput.universityId else null,
            departmentId = if (universityScoped) normalizedInput.departmentId else null,
            program = if (universityScoped) normalizedInput.program else null,
            batchSession = if (universityScoped) normalizedInput.batchSession else null
        )
    }

    fun resolveAcademicLevel(user: UserResponse): String {
        val existingLevel = normalized(user.academicLevel)

        if (existingLevel.isNotBlank()) {
            return existingLevel
        }

        return when (normalized(user.institutionType)) {
            INSTITUTION_TYPE_UNIVERSITY -> ACADEMIC_LEVEL_UNIVERSITY
            INSTITUTION_TYPE_SCHOOL -> ACADEMIC_LEVEL_SSC
            INSTITUTION_TYPE_COLLEGE -> ACADEMIC_LEVEL_HSC
            INSTITUTION_TYPE_POLYTECHNIC -> ACADEMIC_LEVEL_DIPLOMA
            INSTITUTION_TYPE_COACHING -> ACADEMIC_LEVEL_ADMISSION
            else -> when (normalized(user.classLevel)) {
                "11", "12" -> ACADEMIC_LEVEL_HSC
                "9", "10" -> ACADEMIC_LEVEL_SSC
                else -> when {
                    user.universityId != null && user.departmentId != null -> ACADEMIC_LEVEL_UNIVERSITY
                    !user.curriculum.isNullOrBlank() || !normalizeStudentStreamGroup(user.streamGroup).isNullOrBlank() -> ACADEMIC_LEVEL_SSC
                    else -> ACADEMIC_LEVEL_UNIVERSITY
                }
            }
        }
    }

    fun shouldLoadDepartments(user: UserResponse): Boolean {
        return resolveAcademicLevel(user) == ACADEMIC_LEVEL_UNIVERSITY && user.universityId != null
    }

    private fun normalized(value: String?): String {
        return value?.trim()?.lowercase().orEmpty()
    }

    private fun backendAcademicLevel(value: String?): String? {
        return when (normalized(value)) {
            ACADEMIC_LEVEL_UNIVERSITY -> "UNIVERSITY"
            ACADEMIC_LEVEL_SSC -> "SSC"
            ACADEMIC_LEVEL_HSC -> "HSC"
            else -> value?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    fun normalizeStudentStreamGroup(value: String?): String? {
        return when (normalized(value)) {
            STREAM_GROUP_SCIENCE -> STREAM_GROUP_SCIENCE
            STREAM_GROUP_BUSINESS_STUDIES,
            STREAM_GROUP_COMMERCE -> STREAM_GROUP_BUSINESS_STUDIES
            STREAM_GROUP_HUMANITIES -> STREAM_GROUP_HUMANITIES
            else -> null
        }
    }
}
