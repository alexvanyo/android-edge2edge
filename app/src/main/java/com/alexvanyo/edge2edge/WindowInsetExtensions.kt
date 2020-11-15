package com.alexvanyo.edge2edge

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

/**
 * A helper method wrapping [ViewCompat.setOnApplyWindowInsetsListener], additionally providing the initial padding
 * and margins of the view.
 *
 * This method does not consume any window insets, allowing any and all children to receive the same insets.
 *
 * This is a `set` listener, so only the last [windowInsetsListener] applied by [doOnApplyWindowInsets] will be ran.
 *
 * This approach was based on [https://medium.com/androiddevelopers/windowinsets-listeners-to-layouts-8f9ccc8fa4d1].
 */
fun View.doOnApplyWindowInsets(
    windowInsetsListener: (
        insetView: View,
        windowInsets: WindowInsetsCompat,
        initialPadding: Insets,
        initialMargins: Insets
    ) -> Unit
) {
    val initialPadding = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val initialMargins = Insets.of(marginLeft, marginTop, marginRight, marginBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { insetView, windowInsets ->
        windowInsets.also {
            windowInsetsListener(insetView, windowInsets, initialPadding, initialMargins)
        }
    }

    doOnAttach { requestApplyInsets() }
}
