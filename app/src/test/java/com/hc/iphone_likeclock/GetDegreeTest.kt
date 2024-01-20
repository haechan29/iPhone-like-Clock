package com.hc.iphone_likeclock

import org.junit.Assert
import org.junit.Test
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

class GetDegreeTest {
    @Test
    fun returns_0_when_height_is_0() {
        Assert.assertEquals(getDegree(0.0), 0.0, 0.1)
    }

    @Test
    fun test_special_values() {
        Assert.assertEquals(getDegree(1 / sqrt(3.0)), 30.0, 1.0)
        Assert.assertEquals(getDegree(1.0), 45.0, 1.0)
        Assert.assertEquals(getDegree(sqrt(3.0)), 60.0, 1.0)
    }

    @Test
    fun test_negative_values() {
        Assert.assertEquals(getDegree(-1 / sqrt(3.0)), -30.0, 1.0)
        Assert.assertEquals(getDegree(-1.0), -45.0, 1.0)
    }

    private fun getDegree(height: Double): Double {
        return atan(height) * 180 / PI
    }
}