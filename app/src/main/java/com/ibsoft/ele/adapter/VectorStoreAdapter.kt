package com.ibsoft.ele.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibsoft.ele.databinding.ItemVectorStoreBinding
import com.ibsoft.ele.model.VectorStoreResponse

class VectorStoreAdapter(
    private val vectorStores: List<VectorStoreResponse>,
    private val onDeleteClick: (VectorStoreResponse) -> Unit,
    private val onCopyClick: (String) -> Unit
) : RecyclerView.Adapter<VectorStoreAdapter.VectorStoreViewHolder>() {

    inner class VectorStoreViewHolder(val binding: ItemVectorStoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vectorStore: VectorStoreResponse) {
            binding.textStoreName.text = vectorStore.name
            binding.textStoreId.text = vectorStore.id
            binding.buttonCopy.setOnClickListener {
                onCopyClick(vectorStore.id)
            }
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(vectorStore)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VectorStoreViewHolder {
        val binding = ItemVectorStoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VectorStoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VectorStoreViewHolder, position: Int) {
        holder.bind(vectorStores[position])
    }

    override fun getItemCount(): Int = vectorStores.size
}
