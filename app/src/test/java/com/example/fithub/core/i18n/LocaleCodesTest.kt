package com.example.fithub.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM unit tests for [LocaleCodes] — no Android imports, no Robolectric,
 * runs in `./gradlew :app:testDebugUnitTest` in CI.
 */
class LocaleCodesTest {

    @Test fun supportedContainsEnZuAfInOrder() {
        assertEquals(listOf("en", "zu", "af"), LocaleCodes.SUPPORTED.map { it.code })
    }

    @Test fun defaultIsEnglish() {
        assertEquals("en", LocaleCodes.DEFAULT)
    }

    @Test fun normaliseLegacyEnglish() {
        assertEquals("en", LocaleCodes.normalise("ENGLISH"))
        assertEquals("en", LocaleCodes.normalise("English"))
        assertEquals("en", LocaleCodes.normalise("en"))
    }

    @Test fun normaliseLegacyZulu() {
        assertEquals("zu", LocaleCodes.normalise("ZULU"))
        assertEquals("zu", LocaleCodes.normalise("isiZulu"))
        assertEquals("zu", LocaleCodes.normalise("zu"))
        assertEquals("zu", LocaleCodes.normalise("ZUL"))
    }

    @Test fun normaliseLegacyAfrikaans() {
        assertEquals("af", LocaleCodes.normalise("AFRIKAANS"))
        assertEquals("af", LocaleCodes.normalise("Afrikaans"))
        assertEquals("af", LocaleCodes.normalise("AFR"))
    }

    @Test fun normaliseBcp47RegionSuffix() {
        assertEquals("zu", LocaleCodes.normalise("zu-ZA"))
        assertEquals("af", LocaleCodes.normalise("af_ZA"))
    }

    @Test fun normaliseUnknownFallsBackToDefault() {
        assertEquals("en", LocaleCodes.normalise(null))
        assertEquals("en", LocaleCodes.normalise(""))
        assertEquals("en", LocaleCodes.normalise("xx"))
        assertEquals("en", LocaleCodes.normalise("klingon"))
    }

    @Test fun isSupported() {
        assertTrue(LocaleCodes.isSupported("en"))
        assertTrue(LocaleCodes.isSupported("zu"))
        assertTrue(LocaleCodes.isSupported("af"))
        assertFalse(LocaleCodes.isSupported(null))
        assertFalse(LocaleCodes.isSupported("fr"))
    }

    @Test fun nativeNameForEach() {
        assertEquals("English", LocaleCodes.nativeName("en"))
        assertEquals("isiZulu", LocaleCodes.nativeName("zu"))
        assertEquals("Afrikaans", LocaleCodes.nativeName("af"))
    }

    @Test fun nativeNameUnknownFallsBack() {
        assertEquals("English", LocaleCodes.nativeName("xx"))
    }

    @Test fun englishNameMatchesNativeForTheseThree() {
        assertEquals("English", LocaleCodes.englishName("en"))
        assertEquals("isiZulu", LocaleCodes.englishName("zu"))
        assertEquals("Afrikaans", LocaleCodes.englishName("af"))
    }
}