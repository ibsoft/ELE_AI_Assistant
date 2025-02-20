package com.ibsoft.ele.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibsoft.ele.databinding.ItemAssistantBinding
import com.ibsoft.ele.model.AssistantResponse

class AssistantAdapter(
    private val assistants: List<AssistantResponse>,
    private val onDeleteClick: (AssistantResponse) -> Unit,
    private val onCopyClick: (String) -> Unit
) : RecyclerView.Adapter<AssistantAdapter.AssistantViewHolder>() {

    inner class AssistantViewHolder(val binding: ItemAssistantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(assistant: AssistantResponse) {
            binding.textAssistantName.text = assistant.name
            binding.textAssistantId.text = assistant.id
            binding.buttonCopy.setOnClickListener { onCopyClick(assistant.id) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(assistant) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistantViewHolder {
        val binding = ItemAssistantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssistantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssistantViewHolder, position: Int) {
        holder.bind(assistants[position])
    }

    override fun getItemCount(): Int = assistants.size
}
