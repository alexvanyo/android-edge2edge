package com.alexvanyo.edge2edge

import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars

/**
 * A [WindowInsetsAnimation.Callback] and [OnApplyWindowInsetsListener] for nicely handling IME animations.
 *
 * When the keyboard is opened and closed, [onProgress] will be called with a float value between 0 and 1 that
 * corresponds to how far the IME has opened, where 0 means the IME is closed and 1 means the IME is fully open.
 *
 * [windowInsetsListener] will be forwarded on by the implementation of [OnApplyWindowInsetsListener].
 *
 * This approach is loosely based on the
 * [WindowInsetsAnimation](https://github.com/android/user-interface-samples/tree/master/WindowInsetsAnimation) sample.
 */
@RequiresApi(30)
class ImeProgressWindowInsetAnimation(
    private val onProgress: (Float) -> Unit,
    private val windowInsetsListener: (view: View, insets: WindowInsetsCompat) -> Unit
) : WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE), OnApplyWindowInsetsListener {
    private var isAnimating = false
    private var insetView: View? = null
    private var lastWindowInsets: WindowInsetsCompat? = null
    private var imeFullSize: Int = 0

    override fun onPrepare(animation: WindowInsetsAnimation) {
        super.onPrepare(animation)
        isAnimating = true
    }

    override fun onProgress(
        insets: WindowInsets,
        runningAnims: MutableList<WindowInsetsAnimation>
    ): WindowInsets {
        val windowInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)

        // Determine the positive difference between the ime insets and the system bar insets.
        val diff = imeMinusSystemBars(windowInsets)

        onProgress(
            // Avoid divide-by-zero
            if (imeFullSize == 0) {
                0f
            } else {
                diff.toFloat() / imeFullSize
            }
        )

        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimation) {
        super.onEnd(animation)
        if (isAnimating) {
            isAnimating = false

            // Ideally we would just call view.requestApplyInsets() and let the normal dispatch
            // cycle happen, but this happens too late resulting in a visual flicker.
            // Instead we manually dispatch the most recent WindowInsets to the view.
            lastWindowInsets?.let { lastWindowInsets ->
                insetView?.let { insetView ->
                    ViewCompat.dispatchApplyWindowInsets(insetView, lastWindowInsets)
                }
            }
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        insetView = v
        lastWindowInsets = insets

        val imeVisible = insets.isVisible(ime())

        // Only update imeFullSize if the IME is visible. This ensures that when the IME is in the process of being
        // closed, we retain the last known size for it.
        if (imeVisible) {
            imeFullSize = imeMinusSystemBars(insets)
        }

        windowInsetsListener(v, insets)

        // If the inset application comes through and we aren't animating it, update the progress appropriately.
        if (!isAnimating) {
            onProgress(if (imeVisible) 1f else 0f)
        }

        return insets
    }

    private fun imeMinusSystemBars(insets: WindowInsetsCompat): Int {
        val imeMinusSystemBars = Insets.max(
            Insets.subtract(insets.getInsets(ime()), insets.getInsets(systemBars())),
            Insets.NONE
        )

        // Determine the positive difference between the ime insets and the system bar insets.
        return imeMinusSystemBars.bottom - imeMinusSystemBars.top
    }
}
