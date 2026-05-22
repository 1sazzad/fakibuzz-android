package com.qarena.android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InstitutionLabelTest {

    @Test
    fun universityLabelIncludesShortNameWhenPresent() {
        val university = University(
            id = 1,
            name = "Institute of Science and Technology",
            shortName = "IST"
        )

        assertEquals(
            "Institute of Science and Technology (IST)",
            university.displayLabel()
        )
    }

    @Test
    fun departmentLabelIncludesShortNameWhenPresent() {
        val department = Department(
            id = 1,
            name = "Computer Science and Engineering",
            shortName = "CSE",
            universityId = 1
        )

        assertEquals(
            "Computer Science and Engineering (CSE)",
            department.displayLabel()
        )
    }

    @Test
    fun labelsOmitBlankShortName() {
        assertEquals(
            "Demo University",
            University(id = 1, name = "Demo University", shortName = null).displayLabel()
        )
        assertEquals(
            "Demo Department",
            Department(id = 1, name = "Demo Department", shortName = " ", universityId = 1)
                .displayLabel()
        )
    }
}
