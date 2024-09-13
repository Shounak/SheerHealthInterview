package com.example.sheerhealthinterview.network

import kotlinx.serialization.Serializable

@Serializable
data class Case (
    val title: String,
    val caseId: String,
    val timestamp: String,
    val status: String
)