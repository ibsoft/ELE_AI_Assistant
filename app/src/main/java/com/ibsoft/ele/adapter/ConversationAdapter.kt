package com.ibsoft.ele.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibsoft.ele.databinding.ItemConversationBinding
import com.ibsoft.ele.model.Conversation

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit,
    private val onItemLongClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(val binding: ItemConversationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(conversation: Conversation) {
            binding.textConversationTitle.text = conversation.title
            binding.textLastMessage.text = "Tap to enter conversation" // Extend as needed
            binding.root.setOnClickListener {
                onItemClick(conversation)
            }
            binding.root.setOnLongClickListener {
                onItemLongClick(conversation)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size
}
