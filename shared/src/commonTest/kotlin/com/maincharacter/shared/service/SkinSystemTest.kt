package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SkinSystemTest {
    
    @Test
    fun testSkinEquip() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_001"
        val skinId = "skin_default_001"
        
        val equipResult = skinManager.equipSkin(userId, skinId)
        assertTrue(equipResult.success)
        assertEquals(skinId, equipResult.equippedSkinId)
        
        val currentSkin = skinManager.getEquippedSkin(userId)
        assertNotNull(currentSkin)
        assertEquals(skinId, currentSkin.skinId)
    }
    
    @Test
    fun testSkinUnequip() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_002"
        val skinId = "skin_default_001"
        
        skinManager.equipSkin(userId, skinId)
        
        val unequipResult = skinManager.unequipSkin(userId)
        assertTrue(unequipResult.success)
        
        val currentSkin = skinManager.getEquippedSkin(userId)
        assertEquals(null, currentSkin)
    }
    
    @Test
    fun testBondBonus() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_003"
        
        val skinId = "skin_sr_001"
        val skin = skinManager.getSkin(skinId)
        assertNotNull(skin)
        assertEquals(SkinRarity.SR, skin.rarity)
        assertEquals(10, skin.bondBonus)
        
        val initialBond = 50
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = initialBond,
            focus = 100
        )
        
        val bonus = skinManager.calculateBondBonus(userId, skinId, hostState)
        assertEquals(10, bonus)
    }
    
    @Test
    fun testMultipleSkinsBondBonus() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_004"
        
        val skin1Id = "skin_sr_001"
        val skin2Id = "skin_ssr_001"
        
        val skin1 = skinManager.getSkin(skin1Id)
        val skin2 = skinManager.getSkin(skin2Id)
        assertNotNull(skin1)
        assertNotNull(skin2)
        assertEquals(10, skin1.bondBonus)
        assertEquals(20, skin2.bondBonus)
        
        skinManager.equipSkin(userId, skin1Id)
        val bonus1 = skinManager.calculateBondBonus(userId, skin1Id, HostState())
        assertEquals(10, bonus1)
        
        skinManager.equipSkin(userId, skin2Id)
        val bonus2 = skinManager.calculateBondBonus(userId, skin2Id, HostState())
        assertEquals(20, bonus2)
    }
    
    @Test
    fun testSkinOwnership() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_005"
        val skinId = "skin_default_001"
        
        val ownedSkins = skinManager.getOwnedSkins(userId)
        assertTrue(ownedSkins.isEmpty())
        
        skinManager.ownSkin(userId, skinId)
        
        val updatedOwnedSkins = skinManager.getOwnedSkins(userId)
        assertTrue(updatedOwnedSkins.isNotEmpty())
        assertTrue(updatedOwnedSkins.contains(skinId))
    }
    
    @Test
    fun testSkinRarity() {
        val skinManager = TestSkinManager()
        
        val nSkin = skinManager.getSkin("skin_n_001")
        assertNotNull(nSkin)
        assertEquals(SkinRarity.N, nSkin.rarity)
        assertEquals(0, nSkin.bondBonus)
        
        val rSkin = skinManager.getSkin("skin_r_001")
        assertNotNull(rSkin)
        assertEquals(SkinRarity.R, rSkin.rarity)
        assertEquals(5, rSkin.bondBonus)
        
        val srSkin = skinManager.getSkin("skin_sr_001")
        assertNotNull(srSkin)
        assertEquals(SkinRarity.SR, srSkin.rarity)
        assertEquals(10, srSkin.bondBonus)
        
        val ssrSkin = skinManager.getSkin("skin_ssr_001")
        assertNotNull(ssrSkin)
        assertEquals(SkinRarity.SSR, ssrSkin.rarity)
        assertEquals(20, ssrSkin.bondBonus)
    }
    
    @Test
    fun testEquipNonOwnedSkin() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_006"
        val skinId = "skin_ssr_001"
        
        val equipResult = skinManager.equipSkin(userId, skinId)
        assertFalse(equipResult.success)
        assertEquals("Skin not owned", equipResult.errorMessage)
    }
    
    @Test
    fun testBondLevelBonus() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_007"
        val skinId = "skin_sr_001"
        
        skinManager.ownSkin(userId, skinId)
        
        val baseBonus = 10
        val bondLevel = 5
        skinManager.setBondLevel(userId, skinId, bondLevel)
        
        val bonus = skinManager.calculateBondBonus(userId, skinId, HostState())
        val expectedBonus = baseBonus + (bondLevel * 2)
        assertEquals(expectedBonus, bonus)
    }
    
    @Test
    fun testSkinUnlock() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_008"
        val skinId = "skin_r_001"
        
        val unlockResult = skinManager.unlockSkin(userId, skinId)
        assertTrue(unlockResult.success)
        assertEquals(skinId, unlockResult.unlockedSkinId)
        
        val ownedSkins = skinManager.getOwnedSkins(userId)
        assertTrue(ownedSkins.contains(skinId))
    }
    
    @Test
    fun testSkinEquipHistory() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_009"
        
        val skin1Id = "skin_default_001"
        val skin2Id = "skin_sr_001"
        
        skinManager.ownSkin(userId, skin1Id)
        skinManager.ownSkin(userId, skin2Id)
        
        skinManager.equipSkin(userId, skin1Id)
        val history1 = skinManager.getEquipHistory(userId)
        assertEquals(1, history1.size)
        assertEquals(skin1Id, history1[0].skinId)
        
        skinManager.equipSkin(userId, skin2Id)
        val history2 = skinManager.getEquipHistory(userId)
        assertEquals(2, history2.size)
        assertEquals(skin2Id, history2[1].skinId)
    }
    
    @Test
    fun testDefaultSkin() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_010"
        
        val defaultSkinId = "skin_default_001"
        val defaultSkin = skinManager.getSkin(defaultSkinId)
        assertNotNull(defaultSkin)
        assertEquals(SkinRarity.N, defaultSkin.rarity)
        assertEquals(0, defaultSkin.bondBonus)
        assertTrue(defaultSkin.isDefault)
    }
    
    @Test
    fun testSkinEffectOnAttributes() {
        val skinManager = TestSkinManager()
        val userId = "user_skin_011"
        val skinId = "skin_ssr_001"
        
        skinManager.ownSkin(userId, skinId)
        skinManager.equipSkin(userId, skinId)
        
        val initialBond = 50
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = initialBond,
            focus = 100
        )
        
        val bonus = skinManager.calculateBondBonus(userId, skinId, hostState)
        val finalBond = initialBond + bonus
        
        assertEquals(20, bonus)
        assertEquals(70, finalBond)
    }
}

class TestSkinManager : SkinManager {
    private val _skins = MutableStateFlow<Map<String, Skin>>(emptyMap())
    override val skins: StateFlow<Map<String, Skin>> = _skins
    
    private val _ownedSkins = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    private val _equippedSkins = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _bondLevels = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    private val _equipHistory = MutableStateFlow<Map<String, List<SkinEquipRecord>>>(emptyMap())
    
    init {
        val testSkins = mapOf(
            "skin_default_001" to Skin(
                id = "skin_default_001",
                name = "默认皮肤",
                rarity = SkinRarity.N,
                bondBonus = 0,
                isDefault = true,
                description = "默认外观"
            ),
            "skin_n_001" to Skin(
                id = "skin_n_001",
                name = "普通皮肤1",
                rarity = SkinRarity.N,
                bondBonus = 0,
                isDefault = false,
                description = "普通外观"
            ),
            "skin_r_001" to Skin(
                id = "skin_r_001",
                name = "稀有皮肤1",
                rarity = SkinRarity.R,
                bondBonus = 5,
                isDefault = false,
                description = "稀有外观，增加5点羁绊"
            ),
            "skin_sr_001" to Skin(
                id = "skin_sr_001",
                name = "史诗皮肤1",
                rarity = SkinRarity.SR,
                bondBonus = 10,
                isDefault = false,
                description = "史诗外观，增加10点羁绊"
            ),
            "skin_ssr_001" to Skin(
                id = "skin_ssr_001",
                name = "传说皮肤1",
                rarity = SkinRarity.SSR,
                bondBonus = 20,
                isDefault = false,
                description = "传说外观，增加20点羁绊"
            )
        )
        _skins.value = testSkins
    }
    
    override fun getSkin(skinId: String): Skin? {
        return _skins.value[skinId]
    }
    
    override fun equipSkin(userId: String, skinId: String): SkinEquipResult {
        val ownedSkins = _ownedSkins.value[userId] ?: emptySet()
        if (skinId !in ownedSkins && skinId != "skin_default_001") {
            return SkinEquipResult(
                success = false,
                errorMessage = "Skin not owned"
            )
        }
        
        val updatedEquipped = _equippedSkins.value.toMutableMap()
        updatedEquipped[userId] = skinId
        _equippedSkins.value = updatedEquipped
        
        recordEquipHistory(userId, skinId)
        
        return SkinEquipResult(
            success = true,
            equippedSkinId = skinId
        )
    }
    
    override fun unequipSkin(userId: String): SkinEquipResult {
        val updatedEquipped = _equippedSkins.value.toMutableMap()
        updatedEquipped.remove(userId)
        _equippedSkins.value = updatedEquipped
        
        return SkinEquipResult(success = true)
    }
    
    override fun getEquippedSkin(userId: String): Skin? {
        val skinId = _equippedSkins.value[userId] ?: return null
        return getSkin(skinId)
    }
    
    override fun ownSkin(userId: String, skinId: String) {
        val updatedOwned = _ownedSkins.value.toMutableMap()
        val userSkins = updatedOwned[userId]?.toMutableSet() ?: mutableSetOf()
        userSkins.add(skinId)
        updatedOwned[userId] = userSkins
        _ownedSkins.value = updatedOwned
    }
    
    override fun getOwnedSkins(userId: String): Set<String> {
        return _ownedSkins.value[userId] ?: emptySet()
    }
    
    override fun calculateBondBonus(userId: String, skinId: String, hostState: HostState): Int {
        val skin = getSkin(skinId) ?: return 0
        
        val bondLevel = getBondLevel(userId, skinId)
        val levelBonus = bondLevel * 2
        
        return skin.bondBonus + levelBonus
    }
    
    override fun unlockSkin(userId: String, skinId: String): SkinUnlockResult {
        ownSkin(userId, skinId)
        
        return SkinUnlockResult(
            success = true,
            unlockedSkinId = skinId
        )
    }
    
    override fun setBondLevel(userId: String, skinId: String, level: Int) {
        val updatedLevels = _bondLevels.value.toMutableMap()
        val userLevels = updatedLevels[userId]?.toMutableMap() ?: mutableMapOf()
        userLevels[skinId] = level
        updatedLevels[userId] = userLevels
        _bondLevels.value = updatedLevels
    }
    
    override fun getBondLevel(userId: String, skinId: String): Int {
        val userLevels = _bondLevels.value[userId] ?: return 0
        return userLevels[skinId] ?: 0
    }
    
    override fun getEquipHistory(userId: String): List<SkinEquipRecord> {
        return _equipHistory.value[userId] ?: emptyList()
    }
    
    private fun recordEquipHistory(userId: String, skinId: String) {
        val history = _equipHistory.value[userId]?.toMutableList() ?: mutableListOf()
        history.add(SkinEquipRecord(
            skinId = skinId,
            timestamp = System.currentTimeMillis()
        ))
        _equipHistory.value = _equipHistory.value.toMutableMap().apply {
            this[userId] = history
        }
    }
}

interface SkinManager {
    val skins: StateFlow<Map<String, Skin>>
    
    fun getSkin(skinId: String): Skin?
    fun equipSkin(userId: String, skinId: String): SkinEquipResult
    fun unequipSkin(userId: String): SkinEquipResult
    fun getEquippedSkin(userId: String): Skin?
    fun ownSkin(userId: String, skinId: String)
    fun getOwnedSkins(userId: String): Set<String>
    fun calculateBondBonus(userId: String, skinId: String, hostState: HostState): Int
    fun unlockSkin(userId: String, skinId: String): SkinUnlockResult
    fun setBondLevel(userId: String, skinId: String, level: Int)
    fun getBondLevel(userId: String, skinId: String): Int
    fun getEquipHistory(userId: String): List<SkinEquipRecord>
}

data class Skin(
    val id: String,
    val name: String,
    val rarity: SkinRarity,
    val bondBonus: Int,
    val isDefault: Boolean,
    val description: String
)

data class SkinEquipResult(
    val success: Boolean,
    val equippedSkinId: String? = null,
    val errorMessage: String? = null
)

data class SkinUnlockResult(
    val success: Boolean,
    val unlockedSkinId: String? = null
)

data class SkinEquipRecord(
    val skinId: String,
    val timestamp: Long
)
