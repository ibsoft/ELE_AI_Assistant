package com.ibsoft.ele

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import com.ibsoft.ele.model.AssistantResponse
import com.ibsoft.ele.model.CreateVectorStoreRequest
import com.ibsoft.ele.model.VectorStoreResponse
import com.ibsoft.ele.model.VectorStoreListResponse

interface OpenAIAssistantApi {

    // Thread and message endpoints
    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/threads")
    suspend fun createThread(
        @Header("Authorization") apiKey: String,
        @Body request: CreateThreadRequest
    ): ThreadResponse

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/threads/{thread_id}/messages")
    suspend fun addMessageToThread(
        @Path("thread_id") threadId: String,
        @Header("Authorization") apiKey: String,
        @Body request: MessageRequest
    ): MessageResponse

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/threads/{thread_id}/runs")
    suspend fun runAssistant(
        @Path("thread_id") threadId: String,
        @Header("Authorization") apiKey: String,
        @Body request: RunRequest
    ): RunResponse

    @Headers("OpenAI-Beta: assistants=v2")
    @GET("v1/threads/{thread_id}/runs/{run_id}")
    suspend fun getRunStatus(
        @Path("thread_id") threadId: String,
        @Path("run_id") runId: String,
        @Header("Authorization") apiKey: String
    ): RunStatusResponse

    @Headers("OpenAI-Beta: assistants=v2")
    @GET("v1/threads/{thread_id}/messages")
    suspend fun getThreadMessages(
        @Path("thread_id") threadId: String,
        @Header("Authorization") apiKey: String
    ): ThreadMessagesResponse

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @PUT("v1/assistants/{assistant_id}")
    suspend fun updateAssistant(
        @Path("assistant_id") assistantId: String,
        @Header("Authorization") apiKey: String,
        @Body request: UpdateAssistantRequest
    ): Response<AssistantResponse>

    // File upload endpoints
    @Multipart
    @POST("v1/files")
    suspend fun uploadFile(
        @Header("Authorization") apiKey: String,
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody
    ): Response<UploadFileResponse>

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/vector_stores/{vector_store_id}/files")
    suspend fun createVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Header("Authorization") apiKey: String,
        @Body request: VectorStoreFileRequest
    ): Response<VectorStoreFileResponse>

    @Headers("OpenAI-Beta: assistants=v2")
    @DELETE("v1/vector_stores/{vector_store_id}/files/{file_id}")
    suspend fun deleteVectorStoreFile(
        @Path("vector_store_id") vectorStoreId: String,
        @Header("Authorization") apiKey: String,
        @Path("file_id") fileId: String
    ): Response<Unit>

    // Endpoints for Vector Store management
    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/vector_stores")
    suspend fun createVectorStore(
        @Header("Authorization") apiKey: String,
        @Body request: CreateVectorStoreRequest
    ): Response<VectorStoreResponse>

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @GET("v1/vector_stores")
    suspend fun listVectorStores(
        @Header("Authorization") apiKey: String
    ): Response<VectorStoreListResponse>

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @DELETE("v1/vector_stores/{vector_store_id}")
    suspend fun deleteVectorStore(
        @Path("vector_store_id") vectorStoreId: String,
        @Header("Authorization") apiKey: String
    ): Response<Unit>

    // Endpoints for Assistant management
    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @POST("v1/assistants")
    suspend fun createAssistant(
        @Header("Authorization") apiKey: String,
        @Body request: CreateAssistantRequest
    ): Response<AssistantResponse>

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @GET("v1/assistants")
    suspend fun listAssistants(
        @Header("Authorization") apiKey: String,
        @Query("order") order: String = "desc",
        @Query("limit") limit: Int = 20
    ): Response<AssistantListResponse>

    @Headers("Content-Type: application/json", "OpenAI-Beta: assistants=v2")
    @DELETE("v1/assistants/{assistant_id}")
    suspend fun deleteAssistant(
        @Path("assistant_id") assistantId: String,
        @Header("Authorization") apiKey: String
    ): Response<Unit>
}

// Data classes for endpoints:

data class CreateThreadRequest(val dummy: String? = null)

data class ThreadResponse(
    val id: String,
    val created_at: Long
)

data class MessageRequest(
    val role: String,
    val content: String
)

data class MessageResponse(
    val id: String,
    val role: String,
    val content: List<ContentPart>?,
    val created_at: Long
)

data class ContentPart(
    val type: String,
    val text: ContentText
)

data class ContentText(
    val value: String,
    val annotations: List<Any> = emptyList()
)

data class RunRequest(
    @SerializedName("assistant_id")
    val assistant_id: String
)

data class RunResponse(
    val id: String,
    val status: String,
    val started_at: Long
)

data class RunStatusResponse(
    val id: String,
    val status: String,
    val updated_at: Long
)

data class ThreadMessagesResponse(
    @SerializedName("data")
    val messages: List<MessageResponse>?
)

data class UpdateAssistantRequest(
    @SerializedName("tool_resources")
    val toolResources: Map<String, Any>
)

data class CreateVectorStoreRequest(
    val name: String
)

data class VectorStoreResponse(
    val id: String,
    val name: String,
    val created_at: Long
)

data class VectorStoreListResponse(
    @SerializedName("data")
    val vectorStores: List<VectorStoreResponse>
)

data class CreateAssistantRequest(
    val instructions: String,
    val name: String,
    val tools: List<Tool>,
    val model: String
)

data class Tool(
    val type: String
)

data class AssistantListResponse(
    @SerializedName("data")
    val assistants: List<AssistantResponse>
)

data class VectorStoreFileRequest(
    val file_id: String
)

data class VectorStoreFileResponse(
    val id: String,
    val status: String,
    val uploaded_at: Long
)

data class UploadFileResponse(
    val id: String,
    val filename: String,
    val status: String,
    val created_at: Long
)
