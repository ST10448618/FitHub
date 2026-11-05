package com.example.fithub.core.notifications

import com.example.fithub.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationRoutesTest {

    @Test
    fun `valid routes are accepted`() {
        assertEquals(Screen.DASHBOARD, NotificationRoutes.sanitise(Screen.DASHBOARD))
        assertEquals(Screen.SETTINGS, NotificationRoutes.sanitise(Screen.SETTINGS))
    }

    @Test
    fun `invalid routes are rejected`() {
        assertNull(NotificationRoutes.sanitise("onboarding/age"))
        assertNull(NotificationRoutes.sanitise("malicious_route"))
        assertNull(NotificationRoutes.sanitise(""))
        assertNull(NotificationRoutes.sanitise(null))
    }
}