package com.example.sheerhealthinterview.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaseDetails (
    val title: String,
    val caseId: String,
    val timestamp: String,
    val status: CaseStatus,
    val details: MutableList<Detail>
)

@Serializable
data class Detail (
    val detailId: String,
    val timestamp: String,
    val direction: ChatDirection,
    val message: String,
)

@Serializable
data class NewDetail (
    val message: String,
    val direction: ChatDirection
)

@Serializable
enum class ChatDirection() {
    @SerialName("TEAM")
    TEAM,

    @SerialName("USER")
    USER
}



