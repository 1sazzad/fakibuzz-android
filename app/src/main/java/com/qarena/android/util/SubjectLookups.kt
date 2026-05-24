package com.qarena.android.util

import com.qarena.android.data.remote.dto.SubjectDto
import com.qarena.android.data.remote.dto.SubjectListResponse
import com.qarena.android.data.remote.dto.SubjectResponse
import com.qarena.android.data.remote.dto.resolvedSubjects
import com.qarena.android.model.Subject
import com.qarena.android.model.displayLabel
import com.qarena.android.util.PaperTypeLookups.normalizeSupportedPaperTypes

object SubjectLookups {

    fun normalizeSubjects(response: SubjectListResponse): List<Subject> {
        val dtoList = response.resolvedSubjects()
        return dtoList.map { it.toSubject() }
    }

    fun SubjectDto.toSubject(): Subject {
        return Subject(
            id = this.id,
            subjectCode = this.subject_code?.trim().orEmpty(),
            subjectName = this.subject_name?.trim().orEmpty(),
            universityId = this.university_id,
            departmentId = this.department_id,
            academicLevel = this.academic_level?.trim()?.takeIf { it.isNotBlank() },
            group = this.group?.trim()?.takeIf { it.isNotBlank() },
            supportedPaperTypes = normalizeSupportedPaperTypes(this.supported_paper_types).takeIf { it.isNotEmpty() }
        )
    }

    fun SubjectResponse.toSubject(): Subject {
        return Subject(
            id = this.id,
            subjectCode = this.subjectCode?.trim().orEmpty(),
            subjectName = this.subjectName?.trim().orEmpty(),
            universityId = this.universityId,
            departmentId = this.departmentId,
            academicLevel = this.academicLevel?.trim()?.takeIf { it.isNotBlank() },
            group = this.group?.trim()?.takeIf { it.isNotBlank() },
            supportedPaperTypes = normalizeSupportedPaperTypes(this.supportedPaperTypes).takeIf { it.isNotEmpty() }
        )
    }

    fun formatLabel(subject: Subject): String = subject.displayLabel()

    fun normalizeQuery(query: String, maxLen: Int = 100): String {
        val t = query.trim()
        return if (t.length > maxLen) t.substring(0, maxLen) else t
    }
}
