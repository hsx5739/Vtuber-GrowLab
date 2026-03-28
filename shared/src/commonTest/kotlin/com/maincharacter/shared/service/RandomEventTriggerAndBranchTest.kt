package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RandomEventTriggerAndBranchTest {

    @Test
    fun testEventTriggerPipeline() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_001"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_001",
            triggerSource = "manual"
        )

        val triggerResult = pipeline.triggerEvent(
            "evt_sandstorm_lite",
            hostState,
            userSkills,
            context
        )

        assertTrue(triggerResult.success)
        assertNotNull(triggerResult.instance)
        assertEquals(EventStage.INTRO, triggerResult.stage)

        val activeEvent = pipeline.getActiveEvent()
        assertNotNull(activeEvent)
        assertEquals("evt_sandstorm_lite", activeEvent.eventId)
        assertEquals(EventState.IN_PROGRESS, activeEvent.state)
    }

    @Test
    fun testEventIntroAndChoices() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_002"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_002",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)

        val introResult = pipeline.getIntro()
        assertTrue(introResult.success)
        assertNotNull(introResult.intro)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)
        assertTrue(choicesResult.choices.isNotEmpty())

        val barrierChoice = choicesResult.choices.find { it.choice.id == "use_barrier" }
        assertNotNull(barrierChoice)
        assertTrue(barrierChoice.isAvailable)
    }

    @Test
    fun testEventChoiceSelection() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_003"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_003",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)

        val selectionResult = pipeline.selectChoice("use_barrier")
        assertTrue(selectionResult.success)
        assertNotNull(selectionResult.outcome)

        val activeEvent = pipeline.getActiveEvent()
        assertNotNull(activeEvent)
        assertEquals(EventStage.OUTCOME, activeEvent.currentStage)
        assertEquals("use_barrier", activeEvent.selectedChoice)
    }

    @Test
    fun testEventOutcomeApplication() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_004"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_004",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
        pipeline.selectChoice("use_barrier")

        val applyResult = pipeline.applyOutcome()
        assertTrue(applyResult.success)
        assertTrue(applyResult.rewards.isNotEmpty())
        assertTrue(applyResult.statChanges.isNotEmpty())

        val activeEvent = pipeline.getActiveEvent()
        assertNotNull(activeEvent)
        assertEquals(EventStage.CONCLUSION, activeEvent.currentStage)
    }

    @Test
    fun testEventConclusion() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_005"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_005",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
        pipeline.selectChoice("use_barrier")
        pipeline.applyOutcome()

        val conclusionResult = pipeline.concludeEvent()
        assertTrue(conclusionResult.success)
        assertNotNull(conclusionResult.summary)

        val activeEvent = pipeline.getActiveEvent()
        assertEquals(null, activeEvent)

        val history = pipeline.getEventHistory()
        assertEquals(1, history.size)
        assertEquals(EventState.COMPLETED, history[0].state)
    }

    @Test
    fun testEventWithSkillRequirement() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_006"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )

        val context = EventContext(
            userId = userId,
            sessionId = "session_006",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, emptyList(), context)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)

        val barrierChoice = choicesResult.choices.find { it.choice.id == "use_barrier" }
        assertNotNull(barrierChoice)
        assertFalse(barrierChoice.isAvailable)
    }

    @Test
    fun testEventWithEnergyCost() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_007"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_007",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)

        val talkChoice = choicesResult.choices.find { it.choice.id == "talk" }
        assertNotNull(talkChoice)
        assertTrue(talkChoice.isAvailable)
        assertEquals(5, talkChoice.cost.energy)
    }

    @Test
    fun testEventCooldown() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_008"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_008",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
        pipeline.selectChoice("use_barrier")
        pipeline.applyOutcome()
        pipeline.concludeEvent()

        val secondTriggerResult = pipeline.triggerEvent(
            "evt_sandstorm_lite",
            hostState,
            userSkills,
            context
        )

        assertFalse(secondTriggerResult.success)
        assertEquals("Event is not available", secondTriggerResult.error)
    }

    @Test
    fun testEventAbandonment() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_009"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_009",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)

        val abandonResult = pipeline.abandonEvent()
        assertTrue(abandonResult.success)

        val activeEvent = pipeline.getActiveEvent()
        assertEquals(null, activeEvent)

        val history = pipeline.getEventHistory()
        assertEquals(1, history.size)
        assertEquals(EventState.ABANDONED, history[0].state)
    }

    @Test
    fun testEventWithMultipleBranches() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_010"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_010",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)
        assertEquals(3, choicesResult.choices.size)

        val choiceIds = choicesResult.choices.map { it.choice.id }
        assertTrue(choiceIds.contains("use_barrier"))
        assertTrue(choiceIds.contains("observe"))
        assertTrue(choiceIds.contains("talk"))
    }

    @Test
    fun testEventStatsTracking() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_011"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_011",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
        pipeline.selectChoice("use_barrier")
        pipeline.applyOutcome()
        pipeline.concludeEvent()

        val stats = pipeline.pipelineState
        assertEquals(PipelineState.IDLE, stats)
    }

    @Test
    fun testEventWithHiddenChoices() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_012"
        val lowBondHostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 30,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_012",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", lowBondHostState, userSkills, context)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)

        val hiddenChoice = choicesResult.choices.find { it.choice.id == "secret_choice" }
        if (hiddenChoice != null) {
            assertFalse(hiddenChoice.isAvailable)
        }
    }

    @Test
    fun testEventWithHighBond() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_013"
        val highBondHostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 80,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_013",
            triggerSource = "manual"
        )

        pipeline.triggerEvent("evt_sandstorm_lite", highBondHostState, userSkills, context)

        val choicesResult = pipeline.getChoices()
        assertTrue(choicesResult.success)

        val secretChoice = choicesResult.choices.find { it.choice.id == "secret_choice" }
        if (secretChoice != null) {
            assertTrue(secretChoice.isAvailable)
        }
    }

    @Test
    fun testEventHistoryRetrieval() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_014"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")

        for (i in 1..3) {
            val context = EventContext(
                userId = userId,
                sessionId = "session_$i",
                triggerSource = "manual"
            )

            pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
            pipeline.selectChoice("observe")
            pipeline.applyOutcome()
            pipeline.concludeEvent()
        }

        val history = pipeline.getEventHistory()
        assertEquals(3, history.size)

        val summary = pipeline.getEventSummary(history[0].id)
        assertNotNull(summary)
        assertEquals("evt_sandstorm_lite", summary.eventId)
        assertEquals("observe", summary.selectedChoice)
    }

    @Test
    fun testEventPipelineStateTransitions() {
        val eventRegistry = TestEventRegistry()
        val rewardDispatcher = TestRewardDispatcher()
        val cooldownManager = TestCooldownManager()
        val taskManager = TestTaskManager()

        val pipeline = EventTriggerPipeline(
            eventRegistry,
            rewardDispatcher,
            cooldownManager,
            taskManager
        )

        val userId = "user_event_015"
        val hostState = HostState(
            fortune = 100,
            vitality = 100,
            mood = 100,
            bond = 50,
            focus = 100
        )
        val userSkills = listOf("skill_barrier")
        val context = EventContext(
            userId = userId,
            sessionId = "session_015",
            triggerSource = "manual"
        )

        assertEquals(PipelineState.IDLE, pipeline.pipelineState.value)

        pipeline.triggerEvent("evt_sandstorm_lite", hostState, userSkills, context)
        assertEquals(PipelineState.INTRO, pipeline.pipelineState.value)

        pipeline.selectChoice("use_barrier")
        assertEquals(PipelineState.OUTCOME, pipeline.pipelineState.value)

        pipeline.applyOutcome()
        assertEquals(PipelineState.CONCLUSION, pipeline.pipelineState.value)

        pipeline.concludeEvent()
        assertEquals(PipelineState.IDLE, pipeline.pipelineState.value)
    }
}

class TestEventRegistry : EventRegistry {
    private val events = mapOf(
        "evt_sandstorm_lite" to createTestEvent()
    )

    override fun getEventById(eventId: String): EventDef? {
        return events[eventId]
    }

    override fun getAllEvents(): List<EventDef> {
        return events.values.toList()
    }

    private fun createTestEvent(): EventDef {
        return EventDef(
            id = "evt_sandstorm_lite",
            name = "小型气旋",
            weightBase = 10,
            tags = listOf("weather"),
            intro = EventIntro(
                title = "气旋扰动",
                text = "侦测到小型气旋扰动——像龙卷风路过像素世界！",
                image = null
            ),
            choices = listOf(
                EventChoice(
                    id = "use_barrier",
                    label = "发动屏障",
                    description = "使用屏障技能保护自己",
                    skillRequired = "skill_barrier",
                    energyCost = 0,
                    isDefault = false
                ),
                EventChoice(
                    id = "observe",
                    label = "先观察",
                    description = "观察气旋的变化",
                    skillRequired = null,
                    energyCost = 0,
                    isDefault = true
                ),
                EventChoice(
                    id = "talk",
                    label = "和Ta聊聊",
                    description = "与陪伴者交流",
                    skillRequired = null,
                    energyCost = 5,
                    isDefault = false
                ),
                EventChoice(
                    id = "secret_choice",
                    label = "秘密选项",
                    description = "只有高羁绊才能看到",
                    skillRequired = null,
                    energyCost = 0,
                    isDefault = false,
                    hiddenUnlessMinBond = 70
                )
            ),
            outcomes = mapOf(
                "use_barrier" to EventOutcome(
                    id = "outcome_barrier",
                    text = "屏障成功挡住了气旋！",
                    statChanges = listOf(
                        StatChange(stat = "fortune", value = 5),
                        StatChange(stat = "mood", value = 3)
                    ),
                    currencyChanges = listOf(
                        CurrencyChange(currency = CurrencyType.STAR_DUST, amount = 10)
                    ),
                    itemRewards = emptyList(),
                    skillRewards = emptyList(),
                    followUpEvent = null
                ),
                "observe" to EventOutcome(
                    id = "outcome_observe",
                    text = "观察到了有趣的现象",
                    statChanges = listOf(
                        StatChange(stat = "focus", value = 2)
                    ),
                    currencyChanges = emptyList(),
                    itemRewards = emptyList(),
                    skillRewards = emptyList(),
                    followUpEvent = null
                ),
                "talk" to EventOutcome(
                    id = "outcome_talk",
                    text = "陪伴者安慰了你",
                    statChanges = listOf(
                        StatChange(stat = "mood", value = 5),
                        StatChange(stat = "bond", value = 2)
                    ),
                    currencyChanges = emptyList(),
                    itemRewards = emptyList(),
                    skillRewards = emptyList(),
                    followUpEvent = null
                )
            ),
            rewards = EventRewards(
                guaranteedRewards = emptyList(),
                rewardBundleId = null
            ),
            skillInteractions = listOf(
                SkillInteraction(
                    skillId = "skill_barrier",
                    modifier = 0.5f
                )
            ),
            cooldownSec = 3600,
            maxTriggersPerDay = 1
        )
    }
}

class TestRewardDispatcher : RewardDispatcher {
    override fun dispatchRewardBundle(bundleId: String): List<RewardItem> {
        return emptyList()
    }
}

class TestCooldownManager : EventCooldownManager {
    private val cooldowns = mutableMapOf<String, Long>()

    override fun getLastTriggerTime(eventId: String): Long {
        return cooldowns[eventId] ?: 0L
    }

    override fun recordTrigger(eventId: String) {
        cooldowns[eventId] = System.currentTimeMillis()
    }

    override fun isOnCooldown(eventId: String, cooldownSec: Int): Boolean {
        val lastTrigger = cooldowns[eventId] ?: return false
        val elapsed = System.currentTimeMillis() - lastTrigger
        return elapsed < cooldownSec * 1000L
    }

    override fun clearCooldown(eventId: String) {
        cooldowns.remove(eventId)
    }

    override fun clearAllCooldowns() {
        cooldowns.clear()
    }
}

class TestTaskManager : TaskManager {
    override fun updateEventStatistics(eventId: String) {
    }
}

interface EventRegistry {
    fun getEventById(eventId: String): EventDef?
    fun getAllEvents(): List<EventDef>
}

interface EventCooldownManager {
    fun getLastTriggerTime(eventId: String): Long
    fun recordTrigger(eventId: String)
    fun isOnCooldown(eventId: String, cooldownSec: Int): Boolean
    fun clearCooldown(eventId: String)
    fun clearAllCooldowns()
}
