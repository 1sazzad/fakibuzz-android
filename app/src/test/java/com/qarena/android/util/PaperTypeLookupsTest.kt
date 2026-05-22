package com.qarena.android.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaperTypeLookupsTest {

    @Test
    fun normalizePaperTypeAllowsWritten() {
        assertEquals(PaperTypeLookups.WRITTEN, PaperTypeLookups.normalizePaperType(" written "))
    }

    @Test
    fun normalizeSupportedPaperTypesKeepsWritten() {
        val result = PaperTypeLookups.normalizeSupportedPaperTypes(
            listOf("CQ", "written", "MCQ", "WRITTEN", "invalid")
        )

        assertEquals(
            listOf(PaperTypeLookups.CQ, PaperTypeLookups.WRITTEN, PaperTypeLookups.MCQ),
            result
        )
    }

    @Test
    fun resolveSelectedPaperTypeAutoSelectsWrittenOnlySubject() {
        val result = PaperTypeLookups.resolveSelectedPaperType(
            preferredPaperType = null,
            supportedPaperTypes = listOf("WRITTEN")
        )

        assertEquals(PaperTypeLookups.WRITTEN, result)
    }

    @Test
    fun normalizePaperTypeRejectsUnknownValues() {
        assertNull(PaperTypeLookups.normalizePaperType("common"))
    }
}
