package com.maincharacter.shared.config.legacy

import com.maincharacter.shared.model.CurrencyId
import com.maincharacter.shared.model.RewardBundle
import com.maincharacter.shared.model.StatKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class LegacyPityRule(
    val rarePullsMax: Int,
    val rareTier: String
)

@Serializable
data class LegacyGachaRowDef(
    val id: String,
    val poolId: String,
    val weight: Int,
    val rewardBundleId: String
)

@Serializable
data class LegacyGachaPoolDef(
    val id: String,
    val cost: Map<String, Int>,
    val pityRule: LegacyPityRule,
    val rows: List<LegacyGachaRowDef>,
    val schemaVersion: Int = 1
)

class ConfigLoader {
    fun <T> loadConfig(content: String, deserializer: DeserializationStrategy<T>): Result<T> {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        return runCatching { json.decodeFromString(deserializer, content) }
    }

    fun loadRewardBundle(bundleId: String): RewardBundle =
        RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 10),
            statDelta = mapOf(StatKey.MOOD to 2)
        )

    fun loadGachaPool(poolId: String): LegacyGachaPoolDef =
        LegacyGachaPoolDef(
            id = poolId,
            cost = mapOf("STAR_DUST" to 100),
            pityRule = LegacyPityRule(
                rarePullsMax = 90,
                rareTier = "RARE"
            ),
            rows = listOf(
                LegacyGachaRowDef(
                    id = "common",
                    poolId = poolId,
                    weight = 600,
                    rewardBundleId = "rb_gacha_common"
                )
            ),
            schemaVersion = 1
        )

    fun getSignInRewardBundleId(streak: Int): String = "rb_sign_day$streak"
}
