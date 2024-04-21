package com.hc.iphone_likeclock

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import kotlinx.coroutines.Job
import kotlin.math.abs

internal class FlingBehaviorWithOnFinished(
    private val decayAnimSpec: DecayAnimationSpec<Float>,
    private val onFinished: () -> Unit
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        var isAnimationRunning = true
        var velocityLeft = initialVelocity
        var lastValue = 0f
        val animationState = AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        )
        animationState.animateDecay(decayAnimSpec) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity
            if (isAnimationRunning != isRunning) {
                if (!isRunning) {
                    onFinished()
                }
                isAnimationRunning = isRunning
            }
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
        return velocityLeft
    }
}