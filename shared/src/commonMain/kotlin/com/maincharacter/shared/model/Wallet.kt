package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Wallet(
    val starDust: Int = 0,
    val moonGlow: Int = 0,
    val gachaTickets: Map<String, Int> = emptyMap()
) {
    fun getBalance(currencyId: CurrencyId): Int {
        return when (currencyId) {
            CurrencyId.STAR_DUST -> starDust
            CurrencyId.MOON_GLOW -> moonGlow
            CurrencyId.GACHA_TICKET_NORMAL -> gachaTickets["NORMAL"] ?: 0
            CurrencyId.GACHA_TICKET_RARE -> gachaTickets["RARE"] ?: 0
            CurrencyId.GACHA_TICKET_SR -> gachaTickets["SR"] ?: 0
            CurrencyId.GACHA_TICKET_SSR -> gachaTickets["SSR"] ?: 0
        }
    }
    
    fun addCurrency(currencyId: CurrencyId, amount: Int): Wallet {
        return when (currencyId) {
            CurrencyId.STAR_DUST -> copy(starDust = starDust + amount)
            CurrencyId.MOON_GLOW -> copy(moonGlow = moonGlow + amount)
            else -> {
                val ticketType = currencyId.name.removePrefix("GACHA_TICKET_")
                val currentTickets = gachaTickets[ticketType] ?: 0
                copy(gachaTickets = gachaTickets + (ticketType to (currentTickets + amount)))
            }
        }
    }
    
    fun deductCurrency(currencyId: CurrencyId, amount: Int): Wallet? {
        val currentBalance = getBalance(currencyId)
        if (currentBalance < amount) {
            return null
        }
        
        return when (currencyId) {
            CurrencyId.STAR_DUST -> copy(starDust = starDust - amount)
            CurrencyId.MOON_GLOW -> copy(moonGlow = moonGlow - amount)
            else -> {
                val ticketType = currencyId.name.removePrefix("GACHA_TICKET_")
                val currentTickets = gachaTickets[ticketType] ?: 0
                copy(gachaTickets = gachaTickets + (ticketType to (currentTickets - amount)))
            }
        }
    }
    
    fun hasEnough(currencyId: CurrencyId, amount: Int): Boolean {
        return getBalance(currencyId) >= amount
    }
    
    fun getTotalValue(): Int {
        val ticketValue = gachaTickets.values.sum()
        return starDust + moonGlow * 10 + ticketValue
    }
}

enum class CurrencyId {
    STAR_DUST,
    MOON_GLOW,
    GACHA_TICKET_NORMAL,
    GACHA_TICKET_RARE,
    GACHA_TICKET_SR,
    GACHA_TICKET_SSR
}

@Serializable
data class Transaction(
    val currencyId: CurrencyId,
    val amount: Int,
    val reasonCode: ReasonCode,
    val timestamp: Long = System.currentTimeMillis(),
    val traceId: String,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isIncome(): Boolean = amount > 0
    fun isExpense(): Boolean = amount < 0
    fun getAbsoluteAmount(): Int = kotlin.math.abs(amount)
}

enum class ReasonCode {
    SIGN_IN,
    TASK_COMPLETE,
    TASK_VERIFY,
    EVENT_BRANCH,
    GACHA,
    SHOP,
    ITEM_USE,
    ADMIN,
    MIGRATION,
    BUFF_EXPIRE,
    DECAY,
    REFUND,
    PROMOTION
}

@Serializable
data class CurrencyBalance(
    val currencyId: CurrencyId,
    val balance: Int,
    val name: String,
    val icon: String,
    val description: String
) {
    companion object {
        fun fromWallet(wallet: Wallet): List<CurrencyBalance> {
            return listOf(
                CurrencyBalance(
                    currencyId = CurrencyId.STAR_DUST,
                    balance = wallet.starDust,
                    name = "星尘",
                    icon = "✨",
                    description = "基础货币，用于日常消费"
                ),
                CurrencyBalance(
                    currencyId = CurrencyId.MOON_GLOW,
                    balance = wallet.moonGlow,
                    name = "月光",
                    icon = "🌙",
                    description = "高级货币，用于稀有物品"
                ),
                CurrencyBalance(
                    currencyId = CurrencyId.GACHA_TICKET_NORMAL,
                    balance = wallet.gachaTickets["NORMAL"] ?: 0,
                    name = "普通抽卡券",
                    icon = "🎫",
                    description = "用于普通抽卡池"
                ),
                CurrencyBalance(
                    currencyId = CurrencyId.GACHA_TICKET_RARE,
                    balance = wallet.gachaTickets["RARE"] ?: 0,
                    name = "稀有抽卡券",
                    icon = "🎫",
                    description = "用于稀有抽卡池"
                ),
                CurrencyBalance(
                    currencyId = CurrencyId.GACHA_TICKET_SR,
                    balance = wallet.gachaTickets["SR"] ?: 0,
                    name = "SR抽卡券",
                    icon = "🎫",
                    description = "用于SR抽卡池"
                ),
                CurrencyBalance(
                    currencyId = CurrencyId.GACHA_TICKET_SSR,
                    balance = wallet.gachaTickets["SSR"] ?: 0,
                    name = "SSR抽卡券",
                    icon = "🎫",
                    description = "用于SSR抽卡池"
                )
            )
        }
    }
}