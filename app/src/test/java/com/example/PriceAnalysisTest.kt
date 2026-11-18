package com.example

import android.app.Application
import com.example.menuku.core.utils.GeminiPriceAnalyzer
import com.example.menuku.core.utils.PriceEstimator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class PriceAnalysisTest {
    @Test fun aiResponseRangeIsParsedAndAttributed() {
        val result = GeminiPriceAnalyzer.parse("""{"low":20000,"high":30000,"explanation":"Perkiraan"}""", "test-model")
        assertEquals(25000L, result.midpoint)
        assertEquals("test-model", result.model)
    }
    @Test(expected = IllegalArgumentException::class)
    fun reversedAiPriceRangeIsRejected() {
        GeminiPriceAnalyzer.parse("""{"low":30000,"high":20000,"explanation":"Invalid"}""", "test-model")
    }
    @Test fun formulaUsesQuantityAndUnits() {
        assertEquals(PriceEstimator.estimatePrice("Ayam", 1.0, "kg", "protein"),
            PriceEstimator.estimatePrice("Ayam", 1000.0, "gram", "protein"))
    }
}
