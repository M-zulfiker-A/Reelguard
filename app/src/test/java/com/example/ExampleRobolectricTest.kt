package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrainRotStage
import com.example.data.ReelGuardRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `verify 5 minute banner delay and BrainRotStage escalation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ReelGuard", appName)

        val repo = ReelGuardRepository.getInstance(context)
        repo.resetCurrentSessionCount()
        repo.updateBannerDelayMinutes(5)

        // Banner should NOT be eligible immediately when opening Instagram
        assertEquals(5, repo.sessionState.value.bannerDelayMinutes)
        assertFalse(repo.sessionState.value.isBannerDelayElapsed)

        // After 4 minutes (240s), banner should still be hidden
        repo.onInstagramSessionTick(240)
        assertFalse(repo.sessionState.value.isBannerDelayElapsed)

        // After reaching 5 minutes (300s total), banner delay is satisfied
        repo.onInstagramSessionTick(60)
        assertTrue(repo.sessionState.value.isBannerDelayElapsed)

        repeat(6) {
            repo.recordReelScrolled("Unit Test")
        }
        assertEquals(6, repo.sessionState.value.sessionReelsScrolled)
        assertTrue(repo.sessionState.value.currentStage.level >= BrainRotStage.BRAIN_FOG.level)
    }
}
