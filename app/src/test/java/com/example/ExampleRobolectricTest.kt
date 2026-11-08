package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مخفي التطبيقات", appName)
    }

    @Test
    fun `test default PIN verification and update`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        // Default PIN should be 1234
        assertTrue(prefs.isPinCorrect("1234"))
        assertFalse(prefs.isPinCorrect("0000"))

        // Update PIN to 5678
        prefs.setPin("5678")
        assertTrue(prefs.isPinCorrect("5678"))
        assertFalse(prefs.isPinCorrect("1234"))
    }

    @Test
    fun `test decoy PIN verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        assertFalse(prefs.hasDecoyPin())
        prefs.setDecoyPin("9999")
        assertTrue(prefs.hasDecoyPin())
        assertTrue(prefs.isDecoyPin("9999"))
        assertFalse(prefs.isDecoyPin("1234"))
    }
}
