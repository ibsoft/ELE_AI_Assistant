package com.ibsoft.ele.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import com.ibsoft.ele.R
import com.ibsoft.ele.databinding.ItemMessageBotBinding
import com.ibsoft.ele.databinding.ItemMessageUserBinding
import com.ibsoft.ele.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val messages: List<Message>,
    private val onCopyClick: (String) -> Unit,
    private val onTTSClick: (String) -> Unit,
    private val onLikeClick: (Message) -> Unit,
    private val onDislikeClick: (Message) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
    }

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    inner class UserMessageViewHolder(val binding: ItemMessageUserBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val finalText = formatMessageContent(message.message ?: "")
            binding.textMessage.text = finalText

            val formattedDate = formatter.format(Date(message.timestamp))
            binding.textTimestamp.text = formattedDate
            binding.imageIcon.setImageResource(R.drawable.ic_user)
        }
    }

    inner class BotMessageViewHolder(val binding: ItemMessageBotBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val formattedText = formatMessageContent(message.message ?: "")
            val codeBlocks = extractCodeBlocks(message.message ?: "")

            if (codeBlocks.isNotEmpty()) {
                binding.codeContainer.visibility = View.VISIBLE
                binding.textCode.text = formattedText
                binding.textMessage.visibility = View.GONE

                binding.buttonCopyCode.visibility = View.VISIBLE
                binding.buttonCopyCode.setOnClickListener {
                    copyToClipboard(codeBlocks.joinToString("\n"))
                }
            } else {
                binding.textMessage.text = formattedText
                binding.codeContainer.visibility = View.GONE
                binding.textMessage.visibility = View.VISIBLE
                binding.buttonCopyCode.visibility = View.GONE
            }

            val formattedDate = formatter.format(Date(message.timestamp))
            binding.textTimestamp.text = formattedDate
            binding.imageIcon.setImageResource(R.drawable.ic_assistant)

            binding.buttonCopy.setOnClickListener { onCopyClick(message.message) }
            binding.buttonTTS.setOnClickListener { onTTSClick(message.message) }
            binding.buttonLike.setOnClickListener { onLikeClick(message) }
            binding.buttonDislike.setOnClickListener { onDislikeClick(message) }

            if (message.responseTime != null) {
                binding.textResponseTime.visibility = View.VISIBLE
                binding.textResponseTime.text = "Reasoned for ${message.responseTime} seconds >"
            } else {
                binding.textResponseTime.visibility = View.GONE
            }
        }

        private fun extractCodeBlocks(text: String): List<String> {
            val codeRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
            return codeRegex.findAll(text).map { it.groups[1]?.value ?: "" }.toList()
        }

        private fun copyToClipboard(text: String) {
            val clipboard = binding.root.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Code Snippet", text)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(binding.root.context, "Code copied!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender.equals("user", ignoreCase = true)) TYPE_USER
        else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val binding = ItemMessageUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            UserMessageViewHolder(binding)
        } else {
            val binding = ItemMessageBotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            BotMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun formatMessageContent(raw: String): CharSequence {
        val codeRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val mathRegex = Regex("\\$\\$(.*?)\\$\\$|\\$(.*?)\\$")

        val keywordRegex = Regex("\\b(fun|val|var|if|else|for|while|return|class|object|interface|import|package|public|private|protected|override|abstract|final|open)\\b")
        val stringRegex = Regex("\"(.*?)\"|\'(.*?)\'")
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        val commentRegex = Regex("//.*|/\\*(.*?)\\*/", RegexOption.DOT_MATCHES_ALL)

        val builder = buildSpannedString {
            var cursor = 0
            val codeMatches = codeRegex.findAll(raw)
            val pieces = mutableListOf<Segment>()

            for (match in codeMatches) {
                val range = match.range
                if (range.first > cursor) {
                    pieces.add(Segment(raw.substring(cursor, range.first), false, false))
                }
                val codeText = match.groups[1]?.value ?: ""
                pieces.add(Segment(codeText, true, false))
                cursor = range.last + 1
            }

            if (cursor < raw.length) {
                pieces.add(Segment(raw.substring(cursor), false, false))
            }

            val finalPieces = mutableListOf<Segment>()
            for (p in pieces) {
                if (!p.isCode) {
                    var idx = 0
                    val matches = mathRegex.findAll(p.text)
                    for (m in matches) {
                        val range = m.range
                        if (range.first > idx) {
                            finalPieces.add(Segment(p.text.substring(idx, range.first), false, false))
                        }
                        val mathSnippet = m.groups[1]?.value ?: m.groups[2]?.value ?: ""
                        finalPieces.add(Segment(mathSnippet, false, true))
                        idx = range.last + 1
                    }
                    if (idx < p.text.length) {
                        finalPieces.add(Segment(p.text.substring(idx), false, false))
                    }
                } else {
                    finalPieces.add(p)
                }
            }

            for (seg in finalPieces) {
                if (seg.isCode) {
                    inSpans(android.text.style.TypefaceSpan("monospace")) {
                        val codeText = seg.text.trim()
                        var lastIndex = 0

                        val matches = keywordRegex.findAll(codeText) +
                                stringRegex.findAll(codeText) +
                                numberRegex.findAll(codeText) +
                                commentRegex.findAll(codeText)

                        matches.sortedBy { it.range.first }.forEach { match ->
                            if (match.range.first > lastIndex) {
                                // Default main code color (Blue)
                                color(0xFF007ACC.toInt()) {
                                    append(codeText.substring(lastIndex, match.range.first))
                                }
                            }

                            val color = when {
                                keywordRegex.matches(match.value) -> 0xFF9C27B0.toInt() // Purple (Keywords)
                                stringRegex.matches(match.value) -> 0xFF388E3C.toInt() // Green (Strings)
                                numberRegex.matches(match.value) -> 0xFFFF9800.toInt() // Orange (Numbers)
                                commentRegex.matches(match.value) -> 0xFF9E9E9E.toInt() // Gray (Comments)
                                else -> 0xFF007ACC.toInt() // Default Blue for code
                            }

                            color(color) { append(match.value) }
                            lastIndex = match.range.last + 1
                        }

                        if (lastIndex < codeText.length) {
                            color(0xFF007ACC.toInt()) { // Keep remaining main code blue
                                append(codeText.substring(lastIndex))
                            }
                        }
                    }
                } else if (seg.isMath) {
                    bold { color(0xFF800000.toInt()) { append(seg.text.trim()) } }
                } else {
                    append(seg.text)
                }
            }
        }
        return builder
    }


    data class Segment(
        val text: String,
        val isCode: Boolean,
        val isMath: Boolean
    )
}
