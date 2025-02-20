package com.ibsoft.ele.model
import com.google.gson.annotations.SerializedName


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