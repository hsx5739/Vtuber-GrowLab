package com.maincharacter.shared.serializer

import com.maincharacter.shared.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class DataSerializer {
    
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        
        fun serializeHostState(state: HostState): String {
            return json.encodeToString(state)
        }
        
        fun deserializeHostState(jsonString: String): HostState {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeWallet(wallet: Wallet): String {
            return json.encodeToString(wallet)
        }
        
        fun deserializeWallet(jsonString: String): Wallet {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeInventory(inventory: Inventory): String {
            return json.encodeToString(inventory)
        }
        
        fun deserializeInventory(jsonString: String): Inventory {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeTaskInstance(instance: TaskInstance): String {
            return json.encodeToString(instance)
        }
        
        fun deserializeTaskInstance(jsonString: String): TaskInstance {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeSignInState(state: SignInState): String {
            return json.encodeToString(state)
        }
        
        fun deserializeSignInState(jsonString: String): SignInState {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeGachaState(state: GachaState): String {
            return json.encodeToString(state)
        }
        
        fun deserializeGachaState(jsonString: String): GachaState {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeCompanionPresentation(presentation: CompanionPresentation): String {
            return json.encodeToString(presentation)
        }
        
        fun deserializeCompanionPresentation(jsonString: String): CompanionPresentation {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeRewardBundle(bundle: RewardBundle): String {
            return json.encodeToString(bundle)
        }
        
        fun deserializeRewardBundle(jsonString: String): RewardBundle {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeTransaction(transaction: Transaction): String {
            return json.encodeToString(transaction)
        }
        
        fun deserializeTransaction(jsonString: String): Transaction {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeBuff(buff: Buff): String {
            return json.encodeToString(buff)
        }
        
        fun deserializeBuff(jsonString: String): Buff {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeItemGrant(item: ItemGrant): String {
            return json.encodeToString(item)
        }
        
        fun deserializeItemGrant(jsonString: String): ItemGrant {
            return json.decodeFromString(jsonString)
        }
        
        fun serializeBuffGrant(buff: BuffGrant): String {
            return json.encodeToString(buff)
        }
        
        fun deserializeBuffGrant(jsonString: String): BuffGrant {
            return json.decodeFromString(jsonString)
        }
    }
}

@Serializable
data class SerializedHostState(
    val fortune: Int,
    val vitality: Int,
    val mood: Int,
    val bond: Int,
    val focus: Int,
    val activeBuffs: List<SerializedBuff>,
    val dailyTaskCompletionsByType: Map<String, Int>,
    val dailyVerifiedTaskCount: Int,
    val verificationScore: Int
)

@Serializable
data class SerializedBuff(
    val buffId: String,
    val durationSec: Int,
    val startTime: Long
)

@Serializable
data class SerializedWallet(
    val starDust: Int,
    val moonGlow: Int,
    val gachaTickets: Map<String, Int>
)

@Serializable
data class SerializedInventory(
    val skills: Map<String, SerializedSkillInstance>,
    val fruits: Map<String, Int>,
    val skins: Map<String, SerializedSkinInstance>,
    val shards: Map<String, Int>,
    val cosmetics: Map<String, Int>
)

@Serializable
data class SerializedSkillInstance(
    val skillId: String,
    val unlocked: Boolean,
    val lastUsedAt: Long?
)

@Serializable
data class SerializedSkinInstance(
    val skinId: String,
    val owned: Boolean,
    val equipped: Boolean
)

@Serializable
data class SerializedTaskInstance(
    val taskId: String,
    val status: String,
    val completedAt: Long?,
    val verifiedAt: Long?
)

@Serializable
data class SerializedSignInState(
    val lastSignDate: String?,
    val streak: Int,
    val monthSignedBits: List<Int>
)

@Serializable
data class SerializedGachaState(
    val pityCounters: Map<String, Int>,
    val history: List<SerializedGachaPullRecord>
)

@Serializable
data class SerializedGachaPullRecord(
    val pullId: String,
    val poolId: String,
    val timestamp: Long,
    val rewards: List<String>
)

@Serializable
data class SerializedCompanionPresentation(
    val equippedSkinId: String,
    val idleVariant: String
)
