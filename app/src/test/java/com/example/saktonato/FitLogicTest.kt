package com.example.saktonato

import com.example.saktonato.data.Listing
import com.example.saktonato.data.Profile
import com.example.saktonato.domain.computeVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FitLogicTest {

    @Test
    fun testStyleTierReturnsNullVerdict() {
        val listing = Listing(id = "1", name = "Test Tee", chestCm = null, lengthCm = null, sizeTag = "")
        val profile = Profile(heightCm = 170.0, size = "M")
        val verdict = computeVerdict(listing, profile)
        assertNull(verdict)
    }

    @Test
    fun testEstimatedTierVerdict() {
        val listing = Listing(id = "2", name = "Test Tee", sizeTag = "M")
        val profile = Profile(heightCm = 170.0, size = "M")
        val verdict = computeVerdict(listing, profile)
        assertNotNull(verdict)
        assertEquals("Regular fit (estimate)", verdict?.label)
    }

    @Test
    fun testVerifiedTierVerdictWithChest() {
        val listing = Listing(id = "3", name = "Test Tee", chestCm = 52.0, lengthCm = 68.0)
        val profile = Profile(heightCm = 170.0, size = "M", chestCm = 100.0)
        val verdict = computeVerdict(listing, profile)
        assertNotNull(verdict)
        // Garment circ = 52 * 2 = 104, chest = 100, ease = 4 -> "Regular fit"
        assertEquals("Regular fit", verdict?.label)
    }
}
