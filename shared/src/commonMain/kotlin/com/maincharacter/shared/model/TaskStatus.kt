package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    COMPLETED_VERIFIED,
    FAILED,
    CANCELLED
}
