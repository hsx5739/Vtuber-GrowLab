package com.maincharacter.shared.service

import com.maincharacter.shared.model.CompanionPersona
import com.maincharacter.shared.model.FallbackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FallbackResponseHandler(
    private val defaultPersona: CompanionPersona
) {
    private val _customFallbacks = MutableStateFlow<Map<FallbackType, List<String>>>(emptyMap())
    val customFallbacks: StateFlow<Map<FallbackType, List<String>>> = _customFallbacks

    fun setCustomFallbacks(type: FallbackType, responses: List<String>) {
        _customFallbacks.value = _customFallbacks.value + (type to responses)
    }

    fun getFallbackResponse(
        type: FallbackType,
        persona: CompanionPersona = defaultPersona
    ): String {
        val custom = _customFallbacks.value[type]?.randomOrNull()
        if (custom != null) return custom
        return when (type) {
            FallbackType.GREETING -> "Hello."
            FallbackType.FAREWELL -> "See you later."
            FallbackType.UNKNOWN_INPUT -> "I didn't quite understand that."
            FallbackType.ERROR -> "Something went wrong."
            FallbackType.TIMEOUT -> "The response timed out."
            FallbackType.OFFLINE -> "Offline mode is active."
        }
    }
}
