package com.alexvanyo.edge2edge

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import com.alexvanyo.edge2edge.databinding.FragmentMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MainBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding: FragmentMainBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.results.adapter = ResultsAdapter().apply {
            submitList((1..100).map { it.toString() })
        }

        binding.results.doOnApplyWindowInsets { insetView, windowInsets, initialPadding, _ ->
            insetView.updatePadding(
                bottom = initialPadding.bottom + windowInsets.getInsets(systemBars() or ime()).bottom
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                }

                findViewById<View>(com.google.android.material.R.id.container)?.apply {
                    fitsSystemWindows = false
                    doOnApplyWindowInsets { insetView, windowInsets, _, initialMargins ->
                        insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            updateMargins(top = initialMargins.top + windowInsets.getInsets(systemBars()).top)
                        }
                    }
                }

                findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows = false
            }
        }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
