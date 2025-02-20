package com.ibsoft.ele.model

data class AssistantResponse(
    val id: String,
    val name: String,
    val created_at: Long
)

data class Tool(
    val type: String
)

data class CreateAssistantRequest(
    val instructions: String,
    val name: String,
    val tools: List<Tool>,
    val model: String
)
