package com.example.fithub.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Note: Because this uses android.util.Patterns, ensure
// `unitTests.isReturnDefaultValues = true` is in your app/build.gradle.kts
class InputValidatorTest {

    @Test
    fun `valid emails are accepted`() {
        assertTrue(InputValidator.isValidEmail("student@iie.ac.za"))
        assertTrue(InputValidator.isValidEmail("name.surname@gmail.com"))
    }

    @Test
    fun `invalid emails are rejected`() {
        assertFalse(InputValidator.isValidEmail("student"))
        assertFalse(InputValidator.isValidEmail("student@"))
        assertFalse(InputValidator.isValidEmail(""))
    }

    @Test
    fun `password under 6 chars is rejected`() {
        assertFalse(InputValidator.isValidPassword("12345"))
    }

    @Test
    fun `password 6 chars or more is accepted`() {
        assertTrue(InputValidator.isValidPassword("123456"))
        assertTrue(InputValidator.isValidPassword("Password123!"))
    }

    @Test
    fun `weight within human range is accepted`() {
        assertTrue(InputValidator.isValidWeight("75.5"))
        assertTrue(InputValidator.isValidWeight("100"))
    }

    @Test
    fun `weight outside human range is rejected`() {
        assertFalse(InputValidator.isValidWeight("0"))
        assertFalse(InputValidator.isValidWeight("-5"))
        assertFalse(InputValidator.isValidWeight("500"))
        assertFalse(InputValidator.isValidWeight("abc"))
    }
}