package com.qarena.android.model

import com.qarena.android.data.remote.dto.SubjectDto
import com.qarena.android.data.remote.dto.SubjectListResponse
import com.qarena.android.data.remote.dto.SubjectResponse
import com.qarena.android.util.SubjectLookups
import org.junit.Assert.assertEquals
import org.junit.Test

class SubjectLookupsTest {

    @Test
    fun `items preferred over subjects`() {
        val dtoA = SubjectDto(id = 1, subject_code = "A101", subject_name = "Alpha")
        val dtoB = SubjectDto(id = 2, subject_code = "B101", subject_name = "Beta")

        val response = SubjectListResponse(success = true, items = listOf(dtoA), subjects = listOf(dtoB))

        val normalized = SubjectLookups.normalizeSubjects(response)

        assertEquals(1, normalized.size)
        assertEquals("A101", normalized[0].subjectCode)
    }

    @Test
    fun `subjects fallback used when items missing`() {
        val dtoB = SubjectDto(id = 2, subject_code = "B101", subject_name = "Beta")
        val response = SubjectListResponse(success = true, items = null, subjects = listOf(dtoB))

        val normalized = SubjectLookups.normalizeSubjects(response)

        assertEquals(1, normalized.size)
        assertEquals("B101", normalized[0].subjectCode)
    }

    @Test
    fun `format label`() {
        val s = Subject(id = 10, subjectCode = "CSE101", subjectName = "Structured Programming", universityId = null, departmentId = null)
        assertEquals("CSE101 — Structured Programming", SubjectLookups.formatLabel(s))
    }
}
