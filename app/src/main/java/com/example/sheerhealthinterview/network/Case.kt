package com.example.sheerhealthinterview.network

import androidx.annotation.DrawableRes
import com.example.sheerhealthinterview.R
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
data class NewCase (
    val title: String,
    val status: CaseStatus
)

@Serializable
enum class CaseStatus(@DrawableRes val icon: Int, val title: String) {
    @SerialName("WAITING_ON_USER")
    WAITING_ON_USER(R.drawable.round_person_24, "Waiting on user"),

    @SerialName("WAITING_ON_TEAM")
    WAITING_ON_TEAM(R.drawable.baseline_hourglass_bottom_24, "Waiting on team"),

    @SerialName("COMPLETE")
    COMPLETE(R.drawable.baseline_check_circle_24, "Complete")
}
