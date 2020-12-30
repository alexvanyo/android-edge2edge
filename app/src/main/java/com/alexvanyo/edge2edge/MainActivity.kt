package com.alexvanyo.edge2edge

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.alexvanyo.edge2edge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val resultsAdapter = ResultsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupContent()
        setupInsetHandling()
    }

    /**
     * Sets up the inset handling for the views.
     *
     * This method performs the common setup, and then calls into the API-specific setup methods.
     */
    private fun setupInsetHandling() {
        // Tell the system that we are going to handle all insets ourselves.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Add the top system bars as padding to the toolbar
        binding.toolbar.doOnApplyWindowInsets { insetView, windowInsets, initialPadding, _ ->
            insetView.updatePadding(top = initialPadding.top + windowInsets.getInsets(systemBars()).top)
        }

        binding.results.doOnApplyWindowInsets { insetView, windowInsets, initialPadding, _ ->
            insetView.updatePadding(bottom = initialPadding.bottom + windowInsets.getInsets(systemBars()).bottom)
        }

        binding.fixedContent.doOnApplyWindowInsets { insetView, windowInsets, initialPadding, _ ->
            insetView.updatePadding(bottom = initialPadding.bottom + windowInsets.getInsets(systemBars()).bottom)
        }

        if (Build.VERSION.SDK_INT >= 30) {
            setupInsetHandlingImpl30()
        } else {
            setupInsetHandlingImpl()
        }
    }

    /**
     * Sets up additional inset handling on API 30 and above. This adds a window inset animation callback, to smoothly
     * animate the container via its progress.
     */
    @RequiresApi(30)
    private fun setupInsetHandlingImpl30() {
        val listener = ImeProgressWindowInsetAnimation(
            onProgress = { binding.container.progress = it },
            windowInsetsListener = { _, insets -> spacesWindowInsetListener(insets) }
        )

        binding.root.setWindowInsetsAnimationCallback(listener)

        // Delegate to the ImeProgressWindowInsetAnimation's inset listener.
        binding.root.doOnApplyWindowInsets { insetView, windowInsets, _, _ ->
            listener.onApplyWindowInsets(insetView, windowInsets)
        }
    }

    /**
     * Sets up additional inset handling for API 29 and below. This just updates the progress of the container
     * immediately, based on if the IME is visible.
     */
    private fun setupInsetHandlingImpl() {
        binding.root.doOnApplyWindowInsets { _, windowInsets, _, _ ->
            spacesWindowInsetListener(windowInsets)

            binding.container.progress = if (windowInsets.isVisible(ime())) 1f else 0f
        }
    }

    private fun spacesWindowInsetListener(windowInsets: WindowInsetsCompat) {
        binding.bottomSystemBarsSpace.updateLayoutParams {
            height = windowInsets.getInsets(systemBars()).bottom
        }

        binding.imeMinusBottomSystemBarsSpace.updateLayoutParams {
            height = windowInsets.getInsets(systemBars()).bottom
        }

        binding.imeSpace.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            // Only update the height if the IME is visible. This ensures that when the IME is in the process of
            // being closed, we retain the last known size for it.
            if (windowInsets.isVisible(ime())) {
                height = windowInsets.getInsets(ime()).bottom
            }
        }
    }

    private fun setupContent() {
        binding.results.adapter = resultsAdapter

        binding.searchBox.doOnTextChanged { text, _, _, _ ->
            updateResults(text.toString())
        }

        binding.fab.setOnClickListener {
            MainBottomSheetDialogFragment().show(supportFragmentManager, null)
        }

        updateResults(binding.searchBox.text.toString())
    }

    private fun updateResults(searchText: String?) {
        when {
            searchText.isNullOrEmpty() -> {
                resultsAdapter.submitList(emptyList())
                binding.fixedContent.isVisible = false
            }
            searchText.isBlank() -> {
                resultsAdapter.submitList(emptyList())
                binding.fixedContent.isVisible = true
            }
            else -> {
                resultsAdapter.submitList((0 until 20).map { searchText + it })
                binding.fixedContent.isVisible = false
            }
        }
    }
}
