package com.qarena.android.data.repository

import com.google.gson.JsonParser
import com.qarena.android.data.remote.api.PublicApi
import com.qarena.android.data.remote.dto.HealthResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class InstitutionRepositoryTest {

    private val repository = InstitutionRepository(FakePublicApi())

    @Test
    fun parseUniversitiesPrefersItemsWrapper() {
        val response = repository.parseUniversities(
            JsonParser.parseString(
                """
                {
                  "success": true,
                  "items": [
                    {
                      "id": 1,
                      "name": "Institute of Science and Technology",
                      "short_name": "IST"
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        assertEquals(1, response.universities.size)
        assertEquals(1, response.universities.first().id)
        assertEquals("Institute of Science and Technology", response.universities.first().name)
        assertEquals("IST", response.universities.first().shortName)
    }

    @Test
    fun parseDepartmentsPrefersItemsWrapper() {
        val response = repository.parseDepartments(
            JsonParser.parseString(
                """
                {
                  "success": true,
                  "items": [
                    {
                      "id": 2,
                      "name": "Computer Science and Engineering",
                      "short_name": "CSE",
                      "university_id": 1
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        assertEquals(1, response.departments.size)
        assertEquals(2, response.departments.first().id)
        assertEquals("Computer Science and Engineering", response.departments.first().name)
        assertEquals("CSE", response.departments.first().shortName)
        assertEquals(1, response.departments.first().universityId)
    }

    @Test
    fun parseUniversitiesSupportsUniversitiesWrapper() {
        val response = repository.parseUniversities(
            JsonParser.parseString("""{"universities":[{"id":1,"name":"Demo University"}]}""")
        )

        assertEquals(1, response.universities.size)
        assertEquals("Demo University", response.universities.first().name)
    }

    @Test
    fun parseDepartmentsSupportsItemsWrapper() {
        val response = repository.parseDepartments(
            JsonParser.parseString("""{"departments":[{"id":2,"name":"CSE","university_id":1}]}""")
        )

        assertEquals(1, response.departments.size)
        assertEquals("CSE", response.departments.first().name)
    }

    @Test
    fun parseUniversitiesSupportsRawArray() {
        val response = repository.parseUniversities(
            JsonParser.parseString("""[{"id":1,"name":"Demo University"}]""")
        )

        assertEquals(1, response.universities.size)
        assertEquals(1, response.universities.first().id)
    }

    @Test
    fun parseDepartmentsSupportsRawArray() {
        val response = repository.parseDepartments(
            JsonParser.parseString("""[{"id":2,"name":"CSE","university_id":1}]""")
        )

        assertEquals(1, response.departments.size)
        assertEquals(2, response.departments.first().id)
        assertEquals(1, response.departments.first().universityId)
    }

    private class FakePublicApi : PublicApi {
        override suspend fun getUniversities() = JsonParser.parseString("[]")

        override suspend fun getDepartments(universityId: Int) = JsonParser.parseString("[]")

        override suspend fun getHealth() = HealthResponse(status = "ok")
    }
}
