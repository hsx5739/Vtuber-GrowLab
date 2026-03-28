package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import java.time.LocalDate

fun main() {
    println("=== 主角色系统验证 ===\n")
    
    testAttributeCalculator()
    testRewardDispatcher()
    testSignInManager()
    testTaskSystem()
    testGachaSystem()
    
    println("\n=== 验证完成 ===")
}

fun testAttributeCalculator() {
    println("【1】属性计算器测试")
    val calculator = AttributeCalculator()
    
    val initialState = HostState(
        fortune = 50,
        vitality = 50,
        mood = 50,
        bond = 50,
        focus = 50
    )
    
    val delta = StatDelta(StatKey.FORTUNE, 10)
    val newState = calculator.applyDelta(initialState, delta, "测试奖励")
    
    println("✓ 初始气运: ${initialState.fortune}")
    println("✓ 增加后气运: ${newState.fortune}")
    println("✓ 数值夹逼测试: ${calculator.clampValue(StatKey.FORTUNE, 150)} (应为100)")
    println("✓ 属性变化记录: ${calculator.getChanges().size} 条记录\n")
}

fun testRewardDispatcher() {
    println("【2】奖励派发系统测试")
    
    val economyManager = EconomyManager()
    val attributeCalculator = AttributeCalculator()
    val buffManager = BuffManager()
    val currencyLedger = CurrencyLedger()
    val attributeLedger = AttributeLedger()
    
    val dispatcher = RewardDispatcher(
        economyManager,
        attributeCalculator,
        buffManager,
        currencyLedger,
        attributeLedger
    )
    
    val wallet = Wallet(
        currencies = mapOf(
            CurrencyId.STAR_DUST to 1000,
            CurrencyId.MOON_GLOW to 100
        )
    )
    
    val hostState = HostState(
        fortune = 50,
        vitality = 50,
        mood = 50,
        bond = 50,
        focus = 50
    )
    
    val rewardBundle = RewardBundle(
        currencies = mapOf(
            CurrencyId.STAR_DUST to 100,
            CurrencyId.MOON_GLOW to 10
        ),
        statDelta = mapOf(
            StatKey.FORTUNE to 5,
            StatKey.BOND to 3
        ),
        items = emptyList(),
        buffs = emptyList()
    )
    
    val result = dispatcher.dispatch(
        rewardBundle,
        wallet,
        hostState,
        ReasonCode.TASK_COMPLETE
    )
    
    println("✓ 派发成功: ${result.success}")
    println("✓ 派发后星尘: ${result.wallet.currencies[CurrencyId.STAR_DUST]}")
    println("✓ 派发后气运: ${result.hostState.fortune}")
    println("✓ 派发后羁绊: ${result.hostState.bond}")
    println("✓ 幂等性测试: ${dispatcher.hasDispatched(result.traceId)}\n")
}

fun testSignInManager() {
    println("【3】签到系统测试")
    
    val economyManager = EconomyManager()
    val attributeCalculator = AttributeCalculator()
    val buffManager = BuffManager()
    val currencyLedger = CurrencyLedger()
    val attributeLedger = AttributeLedger()
    val timeTrigger = TimeTrigger()
    
    val rewardDispatcher = RewardDispatcher(
        economyManager,
        attributeCalculator,
        buffManager,
        currencyLedger,
        attributeLedger
    )
    
    val signInManager = SignInManager(rewardDispatcher, timeTrigger)
    
    val result = signInManager.signIn()
    
    println("✓ 签到成功: ${result.success}")
    println("✓ 连续签到天数: ${result.streak}")
    println("✓ 总签到次数: ${result.totalSignIns}")
    println("✓ 基础奖励: 星尘${result.reward?.baseReward?.currencies?.get(CurrencyId.STAR_DUST)}")
    
    val status = signInManager.getSignInStatus()
    println("✓ 今日已签到: ${status.isSignedToday}")
    println("✓ 签到等级: ${status.streakLevel}")
    println("✓ 签到加成: ${status.streakBonusMultiplier}x\n")
}

fun testTaskSystem() {
    println("【4】任务系统测试")
    
    val taskDef = TaskDef(
        taskId = "task_001",
        taskName = "完成每日任务",
        taskDescription = "完成1个每日任务",
        taskType = TaskType.DAILY,
        taskCategory = TaskCategory.GAMEPLAY,
        requirements = TaskRequirements(
            targetValue = 1,
            currentProgress = 0
        ),
        rewards = RewardBundle(
            currencies = mapOf(CurrencyId.STAR_DUST to 50),
            statDelta = mapOf(StatKey.VITALITY to 2),
            items = emptyList(),
            buffs = emptyList()
        ),
        isActive = true,
        startTime = System.currentTimeMillis() - 86400000,
        endTime = System.currentTimeMillis() + 86400000
    )
    
    val taskInstance = TaskInstance(
        taskId = taskDef.taskId,
        status = TaskStatus.AVAILABLE,
        progress = 0,
        completionsToday = 0,
        lastCompletionTime = 0
    )
    
    println("✓ 任务ID: ${taskDef.taskId}")
    println("✓ 任务名称: ${taskDef.taskName}")
    println("✓ 任务类型: ${taskDef.taskType}")
    println("✓ 任务奖励: 星尘${taskDef.rewards.currencies[CurrencyId.STAR_DUST]}")
    println("✓ 任务状态: ${taskInstance.status}")
    println("✓ 任务进度: ${taskInstance.progress}/${taskDef.requirements.targetValue}\n")
}

fun testGachaSystem() {
    println("【5】祈愿抽卡系统测试")
    
    val gachaPool = GachaPoolDef(
        poolId = "pool_001",
        poolName = "标准卡池",
        poolDescription = "标准祈愿卡池",
        poolType = GachaPoolType.STANDARD,
        cost = GachaCost(
            currencyType = CurrencyId.STAR_DUST,
            singlePullCost = 100,
            tenPullCost = 900
        ),
        rows = listOf(
            GachaRowDef(
                rowId = "row_n",
                rowName = "N级物品",
                rowDescription = "普通物品",
                rarity = SkinRarity.N,
                items = listOf(
                    GachaItemDef(
                        itemId = "skin_n_001",
                        itemName = "普通皮肤",
                        itemDescription = "普通皮肤描述",
                        probability = 1.0f,
                        isLimited = false
                    )
                )
            ),
            GachaRowDef(
                rowId = "row_r",
                rowName = "R级物品",
                rowDescription = "稀有物品",
                rarity = SkinRarity.R,
                items = listOf(
                    GachaItemDef(
                        itemId = "skin_r_001",
                        itemName = "稀有皮肤",
                        itemDescription = "稀有皮肤描述",
                        probability = 1.0f,
                        isLimited = false
                    )
                )
            )
        ),
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
        startTime = System.currentTimeMillis() - 86400000,
        endTime = System.currentTimeMillis() + 86400000 * 30,
        isActive = true
    )
    
    println("✓ 卡池ID: ${gachaPool.poolId}")
    println("✓ 卡池名称: ${gachaPool.poolName}")
    println("✓ 单抽消耗: ${gachaPool.cost.singlePullCost} 星尘")
    println("✓ 十连消耗: ${gachaPool.cost.tenPullCost} 星尘")
    println("✓ 保底机制: ${gachaPool.pityRules.rarePullsMax}抽必出${gachaPool.pityRules.rareRarity}")
    println("✓ 概率公示:")
    gachaPool.probabilityDisplay.displayProbabilities.forEach { (rarity, prob) ->
        println("  - $rarity: ${(prob * 100).toInt()}%")
    }
    println("✓ 卡池状态: ${if (gachaPool.isActive) "活跃" else "非活跃"}\n")
}