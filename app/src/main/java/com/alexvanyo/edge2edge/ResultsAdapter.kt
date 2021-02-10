package com.alexvanyo.edge2edge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexvanyo.edge2edge.databinding.AdapterItemResultBinding

class ResultsAdapter : ListAdapter<String, ResultsAdapter.ResultsViewHolder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
) {

    class ResultsViewHolder private constructor(
        private val binding: AdapterItemResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            AdapterItemResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        init {
            binding.resultText.doOnApplyWindowInsets { insetView, windowInsets, _, initialMargins ->
                insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(
                        left = initialMargins.left + windowInsets.getInsets(systemBars()).left,
                        right = initialMargins.right + windowInsets.getInsets(systemBars()).right
                    )
                }
            }
        }

        fun bind(string: String) {
            binding.resultText.text = string
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder = ResultsViewHolder(parent)

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            holder.bind(getItem(position))
        }
    }
}