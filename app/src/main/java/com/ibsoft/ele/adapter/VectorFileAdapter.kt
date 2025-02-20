package com.ibsoft.ele.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibsoft.ele.databinding.ItemVectorFileBinding
import com.ibsoft.ele.model.VectorFile

class VectorFileAdapter(
    private val vectorFiles: List<VectorFile>,
    private val onDeleteClick: (VectorFile) -> Unit
) : RecyclerView.Adapter<VectorFileAdapter.VectorFileViewHolder>() {

    inner class VectorFileViewHolder(val binding: ItemVectorFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vectorFile: VectorFile) {
            binding.textFileName.text = vectorFile.fileName
            binding.textFileId.text = vectorFile.fileId
            binding.buttonDelete.setOnClickListener { onDeleteClick(vectorFile) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VectorFileViewHolder {
        val binding = ItemVectorFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VectorFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VectorFileViewHolder, position: Int) {
        holder.bind(vectorFiles[position])
    }

    override fun getItemCount(): Int = vectorFiles.size
}
