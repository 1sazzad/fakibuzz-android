package com.qarena.android.util

import com.google.gson.Gson
import com.qarena.android.data.remote.dto.UserResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicProfileTest {

    @Test
    fun universityRegisterPayloadUsesUniversityScopeOnly() {
        val json = Gson().toJson(
            AcademicProfile.buildRegisterRequest(
                fullName = "Student User",
                email = "student@example.com",
                phone = "01712345678",
                password = "Password1",
                academicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY,
                universityId = 1,
                departmentId = 2,
                program = "BSc in CSE",
                batchSession = "2021-2022",
                termsAccepted = true
            )
        )

        assertTrue(json.contains("\"academic_level\":\"UNIVERSITY\""))
        assertTrue(json.contains("\"institution_type\":\"university\""))
        assertTrue(json.contains("\"curriculum\":\"university_specific\""))
        assertTrue(json.contains("\"university_id\":1"))
        assertTrue(json.contains("\"department_id\":2"))
        assertTrue(json.contains("\"program\":\"BSc in CSE\""))
        assertTrue(json.contains("\"batch_session\":\"2021-2022\""))
        assertFalse(json.contains("stream_group"))
        assertFalse(json.contains("class_level"))
        assertFalse(json.contains("board"))
    }

    @Test
    fun sscRegisterPayloadUsesSyllabusScopeOnly() {
        val json = Gson().toJson(
            AcademicProfile.buildRegisterRequest(
                fullName = "SSC Student",
                email = "ssc@example.com",
                phone = "01712345678",
                password = "Password1",
                academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC,
                curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE,
                classLevel = "10",
                termsAccepted = true
            )
        )

        assertTrue(json.contains("\"academic_level\":\"SSC\""))
        assertTrue(json.contains("\"institution_type\":\"school\""))
        assertTrue(json.contains("\"curriculum\":\"national\""))
        assertTrue(json.contains("\"stream_group\":\"science\""))
        assertFalse(json.contains("class_level"))
        assertFalse(json.contains("university_id"))
        assertFalse(json.contains("department_id"))
        assertFalse(json.contains("program"))
        assertFalse(json.contains("batch_session"))
        assertFalse(json.contains("board"))
    }

    @Test
    fun hscRegisterPayloadUsesSyllabusScopeOnly() {
        val json = Gson().toJson(
            AcademicProfile.buildRegisterRequest(
                fullName = "HSC Student",
                email = "hsc@example.com",
                phone = "01712345678",
                password = "Password1",
                academicLevel = AcademicProfile.ACADEMIC_LEVEL_HSC,
                curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                streamGroup = AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
                classLevel = "12",
                termsAccepted = true
            )
        )

        assertTrue(json.contains("\"academic_level\":\"HSC\""))
        assertTrue(json.contains("\"institution_type\":\"college\""))
        assertTrue(json.contains("\"curriculum\":\"national\""))
        assertTrue(json.contains("\"stream_group\":\"business_studies\""))
        assertFalse(json.contains("class_level"))
        assertFalse(json.contains("university_id"))
        assertFalse(json.contains("department_id"))
        assertFalse(json.contains("program"))
        assertFalse(json.contains("batch_session"))
        assertFalse(json.contains("board"))
    }

    @Test
    fun profileCompletenessFollowsAcademicLevel() {
        assertTrue(
            AcademicProfile.isProfileComplete(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY,
                    universityId = 1,
                    departmentId = 2
                )
            )
        )

        assertTrue(
            AcademicProfile.isProfileComplete(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC,
                    curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                    streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE,
                    classLevel = null
                )
            )
        )

        assertTrue(
            AcademicProfile.isProfileComplete(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_HSC,
                    curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                    streamGroup = AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
                    classLevel = null
                )
            )
        )

        assertFalse(
            AcademicProfile.isProfileComplete(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC,
                    curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                    streamGroup = null,
                    classLevel = null
                )
            )
        )
    }

    @Test
    fun academicLevelSwitchClearsIrrelevantFields() {
        val universityProfile = AcademicProfile.switchAcademicLevel(
            profile = AcademicProfileInput(
                academicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY,
                universityId = 1,
                departmentId = 2,
                curriculum = AcademicProfile.CURRICULUM_UNIVERSITY_SPECIFIC,
                streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE,
                classLevel = "10",
                program = "BSc",
                batchSession = "2021-2022"
            ),
            academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC
        )

        assertEquals(null, universityProfile.universityId)
        assertEquals(null, universityProfile.departmentId)
        assertEquals(AcademicProfile.CURRICULUM_NATIONAL, universityProfile.curriculum)
        assertEquals(AcademicProfile.STREAM_GROUP_SCIENCE, universityProfile.streamGroup)
        assertEquals(null, universityProfile.classLevel)
        assertEquals(null, universityProfile.program)
        assertEquals(null, universityProfile.batchSession)

        val syllabusProfile = AcademicProfile.switchAcademicLevel(
            profile = AcademicProfileInput(
                academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC,
                universityId = 1,
                departmentId = 2,
                curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                streamGroup = AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
                classLevel = "9",
                program = "BSc",
                batchSession = "2021-2022"
            ),
            academicLevel = AcademicProfile.ACADEMIC_LEVEL_UNIVERSITY
        )

        assertEquals(1, syllabusProfile.universityId)
        assertEquals(2, syllabusProfile.departmentId)
        assertEquals(AcademicProfile.CURRICULUM_UNIVERSITY_SPECIFIC, syllabusProfile.curriculum)
        assertEquals(null, syllabusProfile.streamGroup)
        assertEquals(null, syllabusProfile.classLevel)
        assertEquals("BSc", syllabusProfile.program)
        assertEquals("2021-2022", syllabusProfile.batchSession)
    }

    @Test
    fun departmentLookupIsSkippedForSyllabusProfiles() {
        assertFalse(
            AcademicProfile.shouldLoadDepartments(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_SSC,
                    curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                    streamGroup = AcademicProfile.STREAM_GROUP_SCIENCE,
                    classLevel = "10"
                )
            )
        )

        assertFalse(
            AcademicProfile.shouldLoadDepartments(
                UserResponse(
                    academicLevel = AcademicProfile.ACADEMIC_LEVEL_HSC,
                    curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                    streamGroup = AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
                    classLevel = "12"
                )
            )
        )
    }

    @Test
    fun resolveAcademicLevelInfersHscFromClassLevel() {
        val resolved = AcademicProfile.resolveAcademicLevel(
            UserResponse(
                academicLevel = null,
                institutionType = null,
                classLevel = "12",
                curriculum = AcademicProfile.CURRICULUM_NATIONAL,
                streamGroup = AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES
            )
        )

        assertEquals(AcademicProfile.ACADEMIC_LEVEL_HSC, resolved)
    }

    @Test
    fun studentStreamGroupHelpersHideCommonAndNormalizeCommerce() {
        assertEquals("", AcademicProfile.studentStreamGroupLabel("common"))
        assertEquals(
            AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
            AcademicProfile.normalizeStudentStreamGroup(AcademicProfile.STREAM_GROUP_COMMERCE)
        )
        assertEquals(
            listOf(
                AcademicProfile.STREAM_GROUP_SCIENCE,
                AcademicProfile.STREAM_GROUP_BUSINESS_STUDIES,
                AcademicProfile.STREAM_GROUP_HUMANITIES
            ),
            AcademicProfile.studentStreamGroupOptions()
        )
    }
}
