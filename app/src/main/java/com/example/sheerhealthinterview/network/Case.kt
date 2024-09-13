package com.example.sheerhealthinterview.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Case (
    val title: String,
    val caseId: String,
    val timestamp: String,
    val status: CaseStatus
)

@Serializable
enum class CaseStatus(val value: String) {
    @SerialName("WAITING_ON_USER")
    WAITING_ON_USER("wAITING_ON_USER"),

    @SerialName("WAITING_ON_TEAM")
    WAITING_ON_TEAM("wAITING_ON_TEAM"),

    @SerialName("COMPLETE")
    COMPLETE("cOMPLETE")
}
