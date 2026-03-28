package com.maincharacter.shared.service

import com.maincharacter.shared.model.Buff
import com.maincharacter.shared.model.BuffConfig
import com.maincharacter.shared.model.BuffSource
import com.maincharacter.shared.model.HostState
import com.maincharacter.shared.model.ItemConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class BuffManager(
    private val buffConfig: BuffConfig,
    private val itemConfig: ItemConfig
) {
    private val _activeBuffs = MutableStateFlow<Map<String, Buff>>(emptyMap())
    val activeBuffs: StateFlow<Map<String, Buff>> = _activeBuffs

    fun applyBuff(
        userId: String,
        buffId: String,
        source: BuffSource,
        sourceId: String? = null,
        durationOverride: Long? = null
    ): BuffApplicationResult {
        val durationSec = (durationOverride?.div(1000)?.toInt())
            ?: buffConfig.getBuff(buffId)?.duration?.div(1000)?.toInt()
            ?: 0
        val buff = Buff(buffId = buffId, durationSec = durationSec)
        _activeBuffs.value = _activeBuffs.value + (buffId to buff)
        return BuffApplicationResult(
            success = true,
            buffId = buffId,
            reason = "Buff applied",
            buffState = buff
        )
    }

    fun addBuff(
        state: HostState,
        buffId: String,
        durationSec: Int
    ): HostState {
        val buff = Buff(buffId = buffId, durationSec = durationSec)
        _activeBuffs.value = _activeBuffs.value + (buffId to buff)
        return state.copy(activeBuffs = state.activeBuffs + buff)
    }

    fun cleanExpiredBuffs(state: HostState): HostState {
        val now = System.currentTimeMillis()
        val active = state.activeBuffs.filter { buff ->
            buff.durationSec <= 0 || ((now - buff.startTime) / 1000) < buff.durationSec
        }
        _activeBuffs.value = active.associateBy { it.buffId }
        return state.copy(activeBuffs = active)
    }

    fun cleanExpiredBuffs(): Int {
        val before = _activeBuffs.value.size
        val now = System.currentTimeMillis()
        _activeBuffs.value = _activeBuffs.value.filterValues { buff ->
            buff.durationSec <= 0 || ((now - buff.startTime) / 1000) < buff.durationSec
        }
        return before - _activeBuffs.value.size
    }
}

@Serializable
data class BuffApplicationResult(
    val success: Boolean,
    val buffId: String,
    val reason: String,
    val buffState: Buff?
)
