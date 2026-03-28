package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GachaPoolDefTest {
    
    @Test
    fun testIsAvailable() {
        val now = System.currentTimeMillis()
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Test Pool",
            poolDescription = "Test Description",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = emptyList(),
            pityRules = PityRules(
                rarePullsMax = 10,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 90,
                superRareRarity = SkinRarity.SR,
                ultraRarePullsMax = 200,
                ultraRareRarity = SkinRarity.SSR
            ),
            duplicateRules = DuplicateRules(),
            probabilityDisplay = ProbabilityDisplay(
                displayProbabilities = mapOf(
                    SkinRarity.N to 0.6f,
                    SkinRarity.R to 0.3f,
                    SkinRarity.SR to 0.09f,
                    SkinRarity.SSR to 0.01f
                )
            ),
            startTime = now - 10000,
            endTime = now + 10000,
            isActive = true
        )
        
        assertTrue(pool.isAvailable())
        
        val inactivePool = pool.copy(isActive = false)
        assertFalse(inactivePool.isAvailable())
        
        val expiredPool = pool.copy(endTime = now - 1000)
        assertFalse(expiredPool.isAvailable())
    }
    
    @Test
    fun testGetRowByRarity() {
        val nRow = GachaRowDef(
            rowId = "row_n",
            rarity = SkinRarity.N,
            weight = 600,
            skinIds = listOf("skin_001", "skin_002")
        )
        
        val rRow = GachaRowDef(
            rowId = "row_r",
            rarity = SkinRarity.R,
            weight = 300,
            skinIds = listOf("skin_003")
        )
        
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Test Pool",
            poolDescription = "Test Description",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = listOf(nRow, rRow),
            pityRules = PityRules(
                rarePullsMax = 10,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 90,
                superRareRarity = SkinRarity.SR,
                ultraRarePullsMax = 200,
                ultraRareRarity = SkinRarity.SSR
            ),
            duplicateRules = DuplicateRules(),
            probabilityDisplay = ProbabilityDisplay(
                displayProbabilities = mapOf(
                    SkinRarity.N to 0.6f,
                    SkinRarity.R to 0.3f,
                    SkinRarity.SR to 0.09f,
                    SkinRarity.SSR to 0.01f
                )
            ),
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000,
            isActive = true
        )
        
        val foundNRow = pool.getRowByRarity(SkinRarity.N)
        assertNotNull(foundNRow)
        assertEquals("row_n", foundNRow.rowId)
        
        val foundRRow = pool.getRowByRarity(SkinRarity.R)
        assertNotNull(foundRRow)
        assertEquals("row_r", foundRRow.rowId)
        
        val notFoundRow = pool.getRowByRarity(SkinRarity.SSR)
        assertNull(notFoundRow)
    }
    
    @Test
    fun testGetTotalWeight() {
        val nRow = GachaRowDef(
            rowId = "row_n",
            rarity = SkinRarity.N,
            weight = 600,
            skinIds = listOf("skin_001")
        )
        
        val rRow = GachaRowDef(
            rowId = "row_r",
            rarity = SkinRarity.R,
            weight = 300,
            skinIds = listOf("skin_002")
        )
        
        val srRow = GachaRowDef(
            rowId = "row_sr",
            rarity = SkinRarity.SR,
            weight = 90,
            skinIds = listOf("skin_003")
        )
        
        val ssrRow = GachaRowDef(
            rowId = "row_ssr",
            rarity = SkinRarity.SSR,
            weight = 10,
            skinIds = listOf("skin_004")
        )
        
        val pool = GachaPoolDef(
            poolId = "pool_001",
            poolName = "Test Pool",
            poolDescription = "Test Description",
            poolType = GachaPoolType.STANDARD,
            cost = GachaCost(
                currencyType = CurrencyType.STAR_DUST,
                singlePullCost = 100,
                tenPullCost = 900
            ),
            rows = listOf(nRow, rRow, srRow, ssrRow),
            pityRules = PityRules(
                rarePullsMax = 10,
                rareRarity = SkinRarity.R,
                superRarePullsMax = 90,
                superRareRarity = SkinRarity.SR,
                ultraRarePullsMax = 200,
                ultraRareRarity = SkinRarity.SSR
            ),
            duplicateRules = DuplicateRules(),
            probabilityDisplay = ProbabilityDisplay(
                displayProbabilities = mapOf(
                    SkinRarity.N to 0.6f,
                    SkinRarity.R to 0.3f,
                    SkinRarity.SR to 0.09f,
                    SkinRarity.SSR to 0.01f
                )
            ),
            startTime = System.currentTimeMillis() - 10000,
            endTime = System.currentTimeMillis() + 10000,
            isActive = true
        )
        
        assertEquals(1000, pool.getTotalWeight())
    }
}

class GachaRowDefTest {
    
    @Test
    fun testHasGuaranteedSkin() {
        val rowWithGuarantee = GachaRowDef(
            rowId = "row_001",
            rarity = SkinRarity.SSR,
            weight = 10,
            skinIds = listOf("skin_001"),
            guaranteedSkinId = "skin_001"
        )
        
        assertTrue(rowWithGuarantee.hasGuaranteedSkin())
        
        val rowWithoutGuarantee = GachaRowDef(
            rowId = "row_002",
            rarity = SkinRarity.R,
            weight = 300,
            skinIds = listOf("skin_002", "skin_003")
        )
        
        assertFalse(rowWithoutGuarantee.hasGuaranteedSkin())
    }
    
    @Test
    fun testIsFeatured() {
        val row = GachaRowDef(
            rowId = "row_001",
            rarity = SkinRarity.SSR,
            weight = 10,
            skinIds = listOf("skin_001", "skin_002"),
            featuredSkinIds = listOf("skin_001")
        )
        
        assertTrue(row.isFeatured("skin_001"))
        assertFalse(row.isFeatured("skin_002"))
    }
}

class GachaCostTest {
    
    @Test
    fun testGetSinglePullCost() {
        val cost = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 900
        )
        
        assertEquals(100, cost.getSinglePullCost())
    }
    
    @Test
    fun testGetTenPullCost() {
        val cost = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 900
        )
        
        assertEquals(900, cost.getTenPullCost())
    }
    
    @Test
    fun testGetTenPullCost_NoDiscount() {
        val cost = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 1000,
            discountTenPull = false
        )
        
        assertEquals(1000, cost.getTenPullCost())
    }
    
    @Test
    fun testHasDiscount() {
        val costWithDiscount = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 900
        )
        
        assertTrue(costWithDiscount.hasDiscount())
        
        val costWithoutDiscount = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 1000
        )
        
        assertFalse(costWithoutDiscount.hasDiscount())
    }
    
    @Test
    fun testGetDiscountPercentage() {
        val cost = GachaCost(
            currencyType = CurrencyType.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 900
        )
        
        assertEquals(10f, cost.getDiscountPercentage())
    }
}

class PityRulesTest {
    
    @Test
    fun testGetRarityPity() {
        val pityRules = PityRules(
            rarePullsMax = 10,
            rareRarity = SkinRarity.R,
            superRarePullsMax = 90,
            superRareRarity = SkinRarity.SR,
            ultraRarePullsMax = 200,
            ultraRareRarity = SkinRarity.SSR
        )
        
        assertEquals(10, pityRules.getRarityPity(SkinRarity.R))
        assertEquals(90, pityRules.getRarityPity(SkinRarity.SR))
        assertEquals(200, pityRules.getRarityPity(SkinRarity.SSR))
        assertNull(pityRules.getRarityPity(SkinRarity.N))
    }
    
    @Test
    fun testGetHighestRarity() {
        val pityRules = PityRules(
            rarePullsMax = 10,
            rareRarity = SkinRarity.R,
            superRarePullsMax = 90,
            superRareRarity = SkinRarity.SR,
            ultraRarePullsMax = 200,
            ultraRareRarity = SkinRarity.SSR
        )
        
        assertEquals(SkinRarity.SSR, pityRules.getHighestRarity())
    }
    
    @Test
    fun testShouldResetAfterRarity() {
        val pityRules = PityRules(
            rarePullsMax = 10,
            rareRarity = SkinRarity.R,
            superRarePullsMax = 90,
            superRareRarity = SkinRarity.SR,
            ultraRarePullsMax = 200,
            ultraRareRarity = SkinRarity.SSR,
            resetAfterRare = true,
            resetAfterSuperRare = true,
            resetAfterUltraRare = true
        )
        
        assertTrue(pityRules.shouldResetAfterRarity(SkinRarity.R))
        assertTrue(pityRules.shouldResetAfterRarity(SkinRarity.SR))
        assertTrue(pityRules.shouldResetAfterRarity(SkinRarity.SSR))
        assertFalse(pityRules.shouldResetAfterRarity(SkinRarity.N))
    }
}

class DuplicateRulesTest {
    
    @Test
    fun testGetShardCountForRarity() {
        val duplicateRules = DuplicateRules(
            shardPerDuplicate = mapOf(
                SkinRarity.N to 10,
                SkinRarity.R to 20,
                SkinRarity.SR to 50,
                SkinRarity.SSR to 100
            )
        )
        
        assertEquals(10, duplicateRules.getShardCountForRarity(SkinRarity.N))
        assertEquals(20, duplicateRules.getShardCountForRarity(SkinRarity.R))
        assertEquals(50, duplicateRules.getShardCountForRarity(SkinRarity.SR))
        assertEquals(100, duplicateRules.getShardCountForRarity(SkinRarity.SSR))
    }
    
    @Test
    fun testCanConvertDuplicate() {
        val rules = DuplicateRules(
            duplicateSkinToShard = true,
            allowDuplicateConversion = true
        )
        
        assertTrue(rules.canConvertDuplicate())
        
        val rules2 = DuplicateRules(
            duplicateSkinToShard = false,
            allowDuplicateConversion = true
        )
        
        assertFalse(rules2.canConvertDuplicate())
    }
}

class ProbabilityDisplayTest {
    
    @Test
    fun testGetProbabilityForRarity() {
        val display = ProbabilityDisplay(
            displayProbabilities = mapOf(
                SkinRarity.N to 0.6f,
                SkinRarity.R to 0.3f,
                SkinRarity.SR to 0.09f,
                SkinRarity.SSR to 0.01f
            )
        )
        
        assertEquals(0.6f, display.getProbabilityForRarity(SkinRarity.N))
        assertEquals(0.3f, display.getProbabilityForRarity(SkinRarity.R))
        assertEquals(0.09f, display.getProbabilityForRarity(SkinRarity.SR))
        assertEquals(0.01f, display.getProbabilityForRarity(SkinRarity.SSR))
    }
    
    @Test
    fun testGetFormattedPercentage() {
        val display = ProbabilityDisplay(
            displayProbabilities = mapOf(
                SkinRarity.N to 0.6f,
                SkinRarity.SSR to 0.01f
            ),
            displayFormat = ProbabilityDisplayFormat.PERCENTAGE
        )
        
        assertEquals("60%", display.getFormattedProbability(SkinRarity.N))
        assertEquals("1%", display.getFormattedProbability(SkinRarity.SSR))
    }
    
    @Test
    fun testGetFormattedDecimal() {
        val display = ProbabilityDisplay(
            displayProbabilities = mapOf(
                SkinRarity.N to 0.6f,
                SkinRarity.SSR to 0.01f
            ),
            displayFormat = ProbabilityDisplayFormat.DECIMAL
        )
        
        assertEquals("0.6000", display.getFormattedProbability(SkinRarity.N))
        assertEquals("0.0100", display.getFormattedProbability(SkinRarity.SSR))
    }
    
    @Test
    fun testGetFormattedFraction() {
        val display = ProbabilityDisplay(
            displayProbabilities = mapOf(
                SkinRarity.N to 0.6f,
                SkinRarity.SSR to 0.01f
            ),
            displayFormat = ProbabilityDisplayFormat.FRACTION
        )
        
        assertEquals("6000/10000", display.getFormattedProbability(SkinRarity.N))
        assertEquals("100/10000", display.getFormattedProbability(SkinRarity.SSR))
    }
}

class GachaResultTest {
    
    @Test
    fun testIsHighestRarity() {
        val ssrResult = GachaResult(
            poolId = "pool_001",
            pullId = "pull_001",
            skinId = "skin_001",
            skinName = "SSR Skin",
            rarity = SkinRarity.SSR,
            isNew = true,
            isDuplicate = false,
            isFeatured = false,
            pullCount = 1,
            timestamp = System.currentTimeMillis()
        )
        
        assertTrue(ssrResult.isHighestRarity())
        
        val srResult = ssrResult.copy(rarity = SkinRarity.SR)
        assertFalse(srResult.isHighestRarity())
    }
    
    @Test
    fun testIsRareOrAbove() {
        val rResult = GachaResult(
            poolId = "pool_001",
            pullId = "pull_001",
            skinId = "skin_001",
            skinName = "R Skin",
            rarity = SkinRarity.R,
            isNew = true,
            isDuplicate = false,
            isFeatured = false,
            pullCount = 1,
            timestamp = System.currentTimeMillis()
        )
        
        assertTrue(rResult.isRareOrAbove())
        
        val nResult = rResult.copy(rarity = SkinRarity.N)
        assertFalse(nResult.isRareOrAbove())
    }
    
    @Test
    fun testHasPityTriggered() {
        val resultWithPity = GachaResult(
            poolId = "pool_001",
            pullId = "pull_001",
            skinId = "skin_001",
            skinName = "SSR Skin",
            rarity = SkinRarity.SSR,
            isNew = true,
            isDuplicate = false,
            isFeatured = false,
            pityTriggered = setOf(SkinRarity.SSR),
            pullCount = 1,
            timestamp = System.currentTimeMillis()
        )
        
        assertTrue(resultWithPity.hasPityTriggered())
        
        val resultWithoutPity = resultWithPity.copy(pityTriggered = emptySet())
        assertFalse(resultWithoutPity.hasPityTriggered())
    }
}

class GachaPullSummaryTest {
    
    @Test
    fun testGetResultsByRarity() {
        val results = listOf(
            GachaResult(
                poolId = "pool_001",
                pullId = "pull_001",
                skinId = "skin_001",
                skinName = "N Skin",
                rarity = SkinRarity.N,
                isNew = true,
                isDuplicate = false,
                isFeatured = false,
                pullCount = 10,
                timestamp = System.currentTimeMillis()
            ),
            GachaResult(
                poolId = "pool_001",
                pullId = "pull_002",
                skinId = "skin_002",
                skinName = "R Skin",
                rarity = SkinRarity.R,
                isNew = true,
                isDuplicate = false,
                isFeatured = false,
                pullCount = 10,
                timestamp = System.currentTimeMillis()
            )
        )
        
        val summary = GachaPullSummary(
            poolId = "pool_001",
            pullCount = 2,
            results = results,
            totalCost = 900,
            currencyType = CurrencyType.STAR_DUST,
            newSkins = setOf("skin_001", "skin_002"),
            duplicateSkins = emptySet(),
            totalShards = 0,
            pityCounters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )
        
        val nResults = summary.getResultsByRarity(SkinRarity.N)
        assertEquals(1, nResults.size)
        
        val rResults = summary.getResultsByRarity(SkinRarity.R)
        assertEquals(1, rResults.size)
        
        val srResults = summary.getResultsByRarity(SkinRarity.SR)
        assertEquals(0, srResults.size)
    }
    
    @Test
    fun testHasSSR() {
        val resultsWithSSR = listOf(
            GachaResult(
                poolId = "pool_001",
                pullId = "pull_001",
                skinId = "skin_001",
                skinName = "SSR Skin",
                rarity = SkinRarity.SSR,
                isNew = true,
                isDuplicate = false,
                isFeatured = false,
                pullCount = 10,
                timestamp = System.currentTimeMillis()
            )
        )
        
        val summaryWithSSR = GachaPullSummary(
            poolId = "pool_001",
            pullCount = 1,
            results = resultsWithSSR,
            totalCost = 900,
            currencyType = CurrencyType.STAR_DUST,
            newSkins = setOf("skin_001"),
            duplicateSkins = emptySet(),
            totalShards = 0,
            pityCounters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )
        
        assertTrue(summaryWithSSR.hasSSR())
        
        val resultsWithoutSSR = resultsWithSSR.map { it.copy(rarity = SkinRarity.SR) }
        val summaryWithoutSSR = summaryWithSSR.copy(results = resultsWithoutSSR)
        
        assertFalse(summaryWithoutSSR.hasSSR())
    }
    
    @Test
    fun testGetHighestRarity() {
        val results = listOf(
            GachaResult(
                poolId = "pool_001",
                pullId = "pull_001",
                skinId = "skin_001",
                skinName = "N Skin",
                rarity = SkinRarity.N,
                isNew = true,
                isDuplicate = false,
                isFeatured = false,
                pullCount = 10,
                timestamp = System.currentTimeMillis()
            ),
            GachaResult(
                poolId = "pool_001",
                pullId = "pull_002",
                skinId = "skin_002",
                skinName = "SSR Skin",
                rarity = SkinRarity.SSR,
                isNew = true,
                isDuplicate = false,
                isFeatured = false,
                pullCount = 10,
                timestamp = System.currentTimeMillis()
            )
        )
        
        val summary = GachaPullSummary(
            poolId = "pool_001",
            pullCount = 2,
            results = results,
            totalCost = 900,
            currencyType = CurrencyType.STAR_DUST,
            newSkins = setOf("skin_001", "skin_002"),
            duplicateSkins = emptySet(),
            totalShards = 0,
            pityCounters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals(SkinRarity.SSR, summary.getHighestRarity())
    }
}

class PullStatsTest {
    
    @Test
    fun testGetAverageCostPerPull() {
        val stats = PullStats(
            totalPulls = 100,
            totalCost = 9500
        )
        
        assertEquals(95f, stats.getAverageCostPerPull())
    }
    
    @Test
    fun testGetSSRRate() {
        val stats = PullStats(
            totalPulls = 100,
            ssrCount = 2
        )
        
        assertEquals(2f, stats.getSSRRate())
    }
    
    @Test
    fun testGetSRRate() {
        val stats = PullStats(
            totalPulls = 100,
            srCount = 9
        )
        
        assertEquals(9f, stats.getSRRate())
    }
    
    @Test
    fun testGetNewSkinRate() {
        val stats = PullStats(
            totalSkinsObtained = 100,
            totalNewSkins = 70
        )
        
        assertEquals(70f, stats.getNewSkinRate())
    }
    
    @Test
    fun testGetDuplicateSkinRate() {
        val stats = PullStats(
            totalSkinsObtained = 100,
            totalDuplicateSkins = 30
        )
        
        assertEquals(30f, stats.getDuplicateSkinRate())
    }
}

class GachaEnumsTest {
    
    @Test
    fun testSkinRarityTier() {
        assertEquals(1, SkinRarity.N.tier)
        assertEquals(2, SkinRarity.R.tier)
        assertEquals(3, SkinRarity.SR.tier)
        assertEquals(4, SkinRarity.SSR.tier)
    }
    
    @Test
    fun testGachaPoolTypeValues() {
        val types = GachaPoolType.values()
        assertEquals(5, types.size)
        assertTrue(types.contains(GachaPoolType.STANDARD))
        assertTrue(types.contains(GachaPoolType.FEATURED))
        assertTrue(types.contains(GachaPoolType.LIMITED))
        assertTrue(types.contains(GachaPoolType.EVENT))
        assertTrue(types.contains(GachaPoolType.COLLABORATION))
    }
    
    @Test
    fun testPullTypeValues() {
        val types = PullType.values()
        assertEquals(2, types.size)
        assertTrue(types.contains(PullType.SINGLE))
        assertTrue(types.contains(PullType.TEN))
    }
}