package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GachaPoolConfig {
    
    private val _pools = MutableStateFlow<Map<String, GachaPoolDef>>(emptyMap())
    val pools: StateFlow<Map<String, GachaPoolDef>> = _pools
    
    private val _activePools = MutableStateFlow<List<GachaPoolDef>>(emptyList())
    val activePools: StateFlow<List<GachaPoolDef>> = _activePools
    
    private val _configVersion = MutableStateFlow<String>("")
    val configVersion: StateFlow<String> = _configVersion
    
    private val _lastUpdateTime = MutableStateFlow<Long>(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    fun loadConfig(configJson: String): ConfigLoadResult {
        return try {
            val jsonObject = json.parseToJsonElement(configJson).jsonObject
            
            val version = jsonObject["version"]?.jsonPrimitive?.content ?: "1.0.0"
            val poolsArray = jsonObject["pools"]?.jsonObject ?: JsonObject(emptyMap())
            
            val poolMap = mutableMapOf<String, GachaPoolDef>()
            
            poolsArray.forEach { (poolId, poolElement) ->
                val poolDef = parsePoolDef(poolId, poolElement.jsonObject)
                poolMap[poolId] = poolDef
            }
            
            _pools.value = poolMap
            _activePools.value = poolMap.values.filter { it.isAvailable() }
            _configVersion.value = version
            _lastUpdateTime.value = System.currentTimeMillis()
            
            ConfigLoadResult(
                success = true,
                error = null,
                poolCount = poolMap.size,
                activePoolCount = _activePools.value.size,
                version = version
            )
        } catch (e: Exception) {
            ConfigLoadResult(
                success = false,
                error = "Failed to load config: ${e.message}",
                poolCount = 0,
                activePoolCount = 0,
                version = null
            )
        }
    }
    
    private fun parsePoolDef(poolId: String, poolObject: JsonObject): GachaPoolDef {
        val poolName = poolObject["poolName"]?.jsonPrimitive?.content ?: ""
        val poolDescription = poolObject["poolDescription"]?.jsonPrimitive?.content ?: ""
        val poolType = parsePoolType(poolObject["poolType"]?.jsonPrimitive?.content)
        
        val costObject = poolObject["cost"]?.jsonObject ?: JsonObject(emptyMap())
        val cost = parseCost(costObject)
        
        val rowsArray = poolObject["rows"]?.jsonObject ?: JsonObject(emptyMap())
        val rows = parseRows(rowsArray)
        
        val pityObject = poolObject["pityRules"]?.jsonObject ?: JsonObject(emptyMap())
        val pityRules = parsePityRules(pityObject)
        
        val duplicateObject = poolObject["duplicateRules"]?.jsonObject ?: JsonObject(emptyMap())
        val duplicateRules = parseDuplicateRules(duplicateObject)
        
        val probabilityObject = poolObject["probabilityDisplay"]?.jsonObject ?: JsonObject(emptyMap())
        val probabilityDisplay = parseProbabilityDisplay(probabilityObject)
        
        val startTime = poolObject["startTime"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
        val endTime = poolObject["endTime"]?.jsonPrimitive?.content?.toLongOrNull() ?: Long.MAX_VALUE
        val isActive = poolObject["isActive"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val maxPullsPerUser = poolObject["maxPullsPerUser"]?.jsonPrimitive?.content?.toIntOrNull()
        
        val featuredSkins = parseStringList(poolObject["featuredSkins"]?.jsonArray)
        val metadata = parseMetadata(poolObject["metadata"]?.jsonObject)
        
        return GachaPoolDef(
            poolId = poolId,
            poolName = poolName,
            poolDescription = poolDescription,
            poolType = poolType,
            cost = cost,
            rows = rows,
            pityRules = pityRules,
            duplicateRules = duplicateRules,
            probabilityDisplay = probabilityDisplay,
            startTime = startTime,
            endTime = endTime,
            isActive = isActive,
            maxPullsPerUser = maxPullsPerUser,
            featuredSkins = featuredSkins,
            metadata = metadata
        )
    }
    
    private fun parsePoolType(type: String?): GachaPoolType {
        return when (type?.uppercase()) {
            "STANDARD" -> GachaPoolType.STANDARD
            "FEATURED" -> GachaPoolType.FEATURED
            "LIMITED" -> GachaPoolType.LIMITED
            "EVENT" -> GachaPoolType.EVENT
            "COLLABORATION" -> GachaPoolType.COLLABORATION
            else -> GachaPoolType.STANDARD
        }
    }
    
    private fun parseCost(costObject: JsonObject): GachaCost {
        val currencyType = parseCurrencyType(costObject["currencyType"]?.jsonPrimitive?.content)
        val singlePullCost = costObject["singlePullCost"]?.jsonPrimitive?.content?.toIntOrNull() ?: 100
        val tenPullCost = costObject["tenPullCost"]?.jsonPrimitive?.content?.toIntOrNull() ?: 900
        val discountTenPull = costObject["discountTenPull"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        
        return GachaCost(
            currencyType = currencyType,
            singlePullCost = singlePullCost,
            tenPullCost = tenPullCost,
            discountTenPull = discountTenPull
        )
    }
    
    private fun parseCurrencyType(type: String?): CurrencyType {
        return when (type?.uppercase()) {
            "STAR_DUST" -> CurrencyType.STAR_DUST
            "MOON_GLOW" -> CurrencyType.MOON_GLOW
            "GACHA_TICKET" -> CurrencyType.GACHA_TICKET
            "REAL_MONEY" -> CurrencyType.REAL_MONEY
            else -> CurrencyType.STAR_DUST
        }
    }
    
    private fun parseRows(rowsObject: JsonObject): List<GachaRowDef> {
        val rows = mutableListOf<GachaRowDef>()
        
        rowsObject.forEach { (rowId, rowElement) ->
            val rowDef = parseRowDef(rowId, rowElement.jsonObject)
            rows.add(rowDef)
        }
        
        return rows
    }
    
    private fun parseRowDef(rowId: String, rowObject: JsonObject): GachaRowDef {
        val rarity = parseSkinRarity(rowObject["rarity"]?.jsonPrimitive?.content)
        val weight = rowObject["weight"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
        val skinIds = parseStringList(rowObject["skinIds"]?.jsonArray)
        val guaranteedSkinId = rowObject["guaranteedSkinId"]?.jsonPrimitive?.content
        val featuredSkinIds = parseStringList(rowObject["featuredSkinIds"]?.jsonArray)
        val minGuaranteedCount = rowObject["minGuaranteedCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val maxGuaranteedCount = rowObject["maxGuaranteedCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: Int.MAX_VALUE
        val metadata = parseMetadata(rowObject["metadata"]?.jsonObject)
        
        return GachaRowDef(
            rowId = rowId,
            rarity = rarity,
            weight = weight,
            skinIds = skinIds,
            guaranteedSkinId = guaranteedSkinId,
            featuredSkinIds = featuredSkinIds,
            minGuaranteedCount = minGuaranteedCount,
            maxGuaranteedCount = maxGuaranteedCount,
            metadata = metadata
        )
    }
    
    private fun parseSkinRarity(rarity: String?): SkinRarity {
        return when (rarity?.uppercase()) {
            "N" -> SkinRarity.N
            "R" -> SkinRarity.R
            "SR" -> SkinRarity.SR
            "SSR" -> SkinRarity.SSR
            else -> SkinRarity.N
        }
    }
    
    private fun parsePityRules(pityObject: JsonObject): PityRules {
        val rarePullsMax = pityObject["rarePullsMax"]?.jsonPrimitive?.content?.toIntOrNull() ?: 10
        val rareRarity = parseSkinRarity(pityObject["rareRarity"]?.jsonPrimitive?.content)
        val superRarePullsMax = pityObject["superRarePullsMax"]?.jsonPrimitive?.content?.toIntOrNull() ?: 90
        val superRareRarity = parseSkinRarity(pityObject["superRareRarity"]?.jsonPrimitive?.content)
        val ultraRarePullsMax = pityObject["ultraRarePullsMax"]?.jsonPrimitive?.content?.toIntOrNull() ?: 200
        val ultraRareRarity = parseSkinRarity(pityObject["ultraRareRarity"]?.jsonPrimitive?.content)
        val featuredSkinPity = pityObject["featuredSkinPity"]?.jsonPrimitive?.content?.toIntOrNull()
        val resetAfterRare = pityObject["resetAfterRare"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val resetAfterSuperRare = pityObject["resetAfterSuperRare"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val resetAfterUltraRare = pityObject["resetAfterUltraRare"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        
        return PityRules(
            rarePullsMax = rarePullsMax,
            rareRarity = rareRarity,
            superRarePullsMax = superRarePullsMax,
            superRareRarity = superRareRarity,
            ultraRarePullsMax = ultraRarePullsMax,
            ultraRareRarity = ultraRareRarity,
            featuredSkinPity = featuredSkinPity,
            resetAfterRare = resetAfterRare,
            resetAfterSuperRare = resetAfterSuperRare,
            resetAfterUltraRare = resetAfterUltraRare
        )
    }
    
    private fun parseDuplicateRules(duplicateObject: JsonObject): DuplicateRules {
        val duplicateSkinToShard = duplicateObject["duplicateSkinToShard"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val shardPerDuplicate = parseShardPerDuplicate(duplicateObject["shardPerDuplicate"]?.jsonObject)
        val maxShardPerSkin = duplicateObject["maxShardPerSkin"]?.jsonPrimitive?.content?.toIntOrNull() ?: 999
        val allowDuplicateConversion = duplicateObject["allowDuplicateConversion"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        
        return DuplicateRules(
            duplicateSkinToShard = duplicateSkinToShard,
            shardPerDuplicate = shardPerDuplicate,
            maxShardPerSkin = maxShardPerSkin,
            allowDuplicateConversion = allowDuplicateConversion
        )
    }
    
    private fun parseShardPerDuplicate(shardObject: JsonObject?): Map<SkinRarity, Int> {
        val shardMap = mutableMapOf<SkinRarity, Int>()
        
        shardObject?.forEach { (rarity, count) ->
            val skinRarity = parseSkinRarity(rarity)
            val shardCount = count.jsonPrimitive.content.toIntOrNull() ?: 0
            shardMap[skinRarity] = shardCount
        }
        
        return shardMap
    }
    
    private fun parseProbabilityDisplay(probabilityObject: JsonObject): ProbabilityDisplay {
        val displayProbabilities = parseDisplayProbabilities(probabilityObject["displayProbabilities"]?.jsonObject)
        val featuredProbability = probabilityObject["featuredProbability"]?.jsonPrimitive?.content?.toFloatOrNull()
        val guaranteedProbability = probabilityObject["guaranteedProbability"]?.jsonPrimitive?.content?.toFloatOrNull()
        val displayFormat = parseProbabilityDisplayFormat(probabilityObject["displayFormat"]?.jsonPrimitive?.content)
        val showDetailedProbabilities = probabilityObject["showDetailedProbabilities"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
        
        return ProbabilityDisplay(
            displayProbabilities = displayProbabilities,
            featuredProbability = featuredProbability,
            guaranteedProbability = guaranteedProbability,
            displayFormat = displayFormat,
            showDetailedProbabilities = showDetailedProbabilities
        )
    }
    
    private fun parseDisplayProbabilities(probabilityObject: JsonObject?): Map<SkinRarity, Float> {
        val probabilityMap = mutableMapOf<SkinRarity, Float>()
        
        probabilityObject?.forEach { (rarity, probability) ->
            val skinRarity = parseSkinRarity(rarity)
            val probValue = probability.jsonPrimitive.content.toFloatOrNull() ?: 0f
            probabilityMap[skinRarity] = probValue
        }
        
        return probabilityMap
    }
    
    private fun parseProbabilityDisplayFormat(format: String?): ProbabilityDisplayFormat {
        return when (format?.uppercase()) {
            "PERCENTAGE" -> ProbabilityDisplayFormat.PERCENTAGE
            "DECIMAL" -> ProbabilityDisplayFormat.DECIMAL
            "FRACTION" -> ProbabilityDisplayFormat.FRACTION
            else -> ProbabilityDisplayFormat.PERCENTAGE
        }
    }
    
    private fun parseStringList(array: kotlinx.serialization.json.JsonArray?): List<String> {
        if (array == null) return emptyList()
        
        return array.mapNotNull { element ->
            element.jsonPrimitive.content
        }
    }
    
    private fun parseMetadata(metadataObject: JsonObject?): Map<String, String> {
        if (metadataObject == null) return emptyMap()
        
        val metadata = mutableMapOf<String, String>()
        
        metadataObject.forEach { (key, value) ->
            metadata[key] = value.jsonPrimitive.content
        }
        
        return metadata
    }
    
    fun getPool(poolId: String): GachaPoolDef? {
        return _pools.value[poolId]
    }
    
    fun getAllPools(): Map<String, GachaPoolDef> {
        return _pools.value
    }
    
    fun getActivePools(): List<GachaPoolDef> {
        return _activePools.value
    }
    
    fun getPoolByType(poolType: GachaPoolType): List<GachaPoolDef> {
        return _pools.value.values.filter { it.poolType == poolType }
    }
    
    fun getStandardPool(): GachaPoolDef? {
        return _pools.value.values.find { it.poolType == GachaPoolType.STANDARD }
    }
    
    fun getFeaturedPool(): GachaPoolDef? {
        return _pools.value.values.find { it.poolType == GachaPoolType.FEATURED }
    }
    
    fun getLimitedPools(): List<GachaPoolDef> {
        return _pools.value.values.filter { it.poolType == GachaPoolType.LIMITED }
    }
    
    fun getEventPools(): List<GachaPoolDef> {
        return _pools.value.values.filter { it.poolType == GachaPoolType.EVENT }
    }
    
    fun getCollaborationPools(): List<GachaPoolDef> {
        return _pools.value.values.filter { it.poolType == GachaPoolType.COLLABORATION }
    }
    
    fun getConfigVersion(): String {
        return _configVersion.value
    }
    
    fun getLastUpdateTime(): Long {
        return _lastUpdateTime.value
    }
    
    fun getLoadedPoolCount(): Int {
        return _pools.value.size
    }
    
    fun getLoadedActivePoolCount(): Int {
        return _activePools.value.size
    }
    
    fun hasPool(poolId: String): Boolean {
        return _pools.value.containsKey(poolId)
    }
    
    fun isPoolAvailable(poolId: String): Boolean {
        val pool = getPool(poolId) ?: return false
        return pool.isAvailable()
    }
    
    fun clearConfig() {
        _pools.value = emptyMap()
        _activePools.value = emptyList()
        _configVersion.value = ""
        _lastUpdateTime.value = 0L
    }
}

@Serializable
data class ConfigLoadResult(
    val success: Boolean,
    val error: String?,
    val poolCount: Int,
    val activePoolCount: Int,
    val version: String?
) {
    fun getErrorMessage(): String {
        return error ?: "Unknown error"
    }
    
    fun isSuccessful(): Boolean {
        return success
    }
    
    fun getPoolCountValue(): Int {
        return poolCount
    }
    
    fun getActivePoolCountValue(): Int {
        return activePoolCount
    }
    
    fun getResolvedVersion(): String {
        return version ?: "Unknown"
    }
}
