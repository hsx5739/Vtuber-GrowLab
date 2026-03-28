package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class RewardBundle(
    val currencies: Map<CurrencyId, Int> = emptyMap(),
    val statDelta: Map<StatKey, Int> = emptyMap(),
    val items: List<ItemGrant> = emptyList(),
    val buffs: List<BuffGrant> = emptyList(),
    val telemetryTags: List<String> = emptyList()
) {
    fun isEmpty(): Boolean {
        return currencies.isEmpty() && 
               statDelta.isEmpty() && 
               items.isEmpty() && 
               buffs.isEmpty()
    }
    
    fun getTotalValue(): Int {
        var total = 0
        currencies.forEach { (currencyId, amount) ->
            total += when (currencyId) {
                CurrencyId.STAR_DUST -> amount
                CurrencyId.MOON_GLOW -> amount * 10
                else -> amount * 5
            }
        }
        return total
    }
    
    fun getCurrencies(): List<CurrencyAmount> {
        return currencies.map { (currencyId, amount) ->
            CurrencyAmount(
                currencyId = currencyId,
                amount = amount,
                name = getCurrencyName(currencyId),
                icon = getCurrencyIcon(currencyId)
            )
        }
    }
    
    fun getStatDeltas(): List<StatDelta> {
        return statDelta.map { (key, value) ->
            StatDelta(key, value)
        }
    }
    
    fun hasCurrency(currencyId: CurrencyId): Boolean {
        return currencies.containsKey(currencyId)
    }
    
    fun hasStatDelta(key: StatKey): Boolean {
        return statDelta.containsKey(key)
    }
    
    fun hasItem(itemId: String): Boolean {
        return items.any { it.itemId == itemId }
    }
    
    fun hasBuff(buffId: String): Boolean {
        return buffs.any { it.buffId == buffId }
    }
    
    fun validate(): RewardValidationResult {
        val errors = mutableListOf<String>()
        
        currencies.forEach { (currencyId, amount) ->
            if (amount < 0) {
                errors.add("Currency amount cannot be negative: $currencyId=$amount")
            }
        }
        
        statDelta.forEach { (key, value) ->
            if (value == 0) {
                errors.add("Stat delta cannot be zero: $key=$value")
            }
        }
        
        items.forEach { item ->
            if (item.count < 1) {
                errors.add("Item count must be >= 1: ${item.itemId}=${item.count}")
            }
        }
        
        buffs.forEach { buff ->
            if (buff.durationSec < 0) {
                errors.add("Buff duration cannot be negative: ${buff.buffId}=${buff.durationSec}")
            }
        }
        
        return RewardValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            totalValue = getTotalValue()
        )
    }
    
    private fun getCurrencyName(currencyId: CurrencyId): String {
        return when (currencyId) {
            CurrencyId.STAR_DUST -> "星尘"
            CurrencyId.MOON_GLOW -> "月光"
            else -> "抽卡券"
        }
    }
    
    private fun getCurrencyIcon(currencyId: CurrencyId): String {
        return when (currencyId) {
            CurrencyId.STAR_DUST -> "✨"
            CurrencyId.MOON_GLOW -> "🌙"
            else -> "🎫"
        }
    }
}

@Serializable
data class ItemGrant(
    val itemId: String,
    val count: Int,
    val grantType: GrantType = GrantType.STACK
) {
    fun validate(): ItemGrantValidationResult {
        val errors = mutableListOf<String>()
        
        if (itemId.isEmpty()) {
            errors.add("Item ID cannot be empty")
        }
        
        if (count < 1) {
            errors.add("Item count must be >= 1: $count")
        }
        
        if (count > 999) {
            errors.add("Item count cannot exceed 999: $count")
        }
        
        return ItemGrantValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

@Serializable
data class BuffGrant(
    val buffId: String,
    val durationSec: Int
) {
    fun validate(): BuffGrantValidationResult {
        val errors = mutableListOf<String>()
        
        if (buffId.isEmpty()) {
            errors.add("Buff ID cannot be empty")
        }
        
        if (durationSec < 0) {
            errors.add("Buff duration cannot be negative: $durationSec")
        }
        
        if (durationSec > 86400 * 7) {
            errors.add("Buff duration cannot exceed 7 days: $durationSec")
        }
        
        return BuffGrantValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

enum class GrantType {
    STACK,
    UNIQUE
}

@Serializable
data class CurrencyAmount(
    val currencyId: CurrencyId,
    val amount: Int,
    val name: String,
    val icon: String
)

@Serializable
data class RewardValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val totalValue: Int
)

@Serializable
data class ItemGrantValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

@Serializable
data class BuffGrantValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)