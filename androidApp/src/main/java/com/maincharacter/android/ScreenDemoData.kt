package com.maincharacter.android

import androidx.compose.ui.graphics.Color

internal data class StatusMetric(
    val label: String,
    val value: String,
    val iconRes: Int,
    val accent: Color
)

internal data class CharacterScene(
    val imageRes: Int,
    val mood: String,
    val message: String
)

internal data class DemoAccountContext(
    val accountName: String,
    val accountId: String,
    val characterId: String,
    val nickname: String,
    val equippedAppearanceName: String,
    val bond: Int,
    val charm: Int,
    val vitality: Int,
    val focus: Int,
    val mood: Int,
    val lotteryTicketCount: Int
)

internal data class RewardChipData(
    val label: String,
    val value: String,
    val background: Color,
    val contentColor: Color
)

internal data class DemoTask(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val group: String,
    val categoryLabel: String,
    val status: String,
    val progressLabel: String,
    val validationTitle: String,
    val validationHint: String,
    val companionLine: String,
    val designNote: String,
    val rewardChips: List<RewardChipData>,
    val actionSteps: List<String>,
    val detailRows: List<Pair<String, String>>,
    val statusColor: Color
) {
    val groupLabel: String
        get() = when (group) {
            "WEEKLY" -> "每周任务"
            "CHALLENGE" -> "今日挑战"
            else -> "每日任务"
        }
}

internal data class DemoEventBranch(
    val id: String,
    val label: String,
    val requiresSkill: String? = null,
    val energyCost: Int = 0,
    val resultPreview: String,
    val rewardChips: List<RewardChipData>
)

internal data class DemoEvent(
    val id: String,
    val title: String,
    val tagLabel: String,
    val intro: String,
    val description: String,
    val weightLabel: String,
    val designNote: String,
    val branches: List<DemoEventBranch>,
    val flowSummary: String,
    val companionNote: String,
    val performanceHint: String,
    val detailRows: List<Pair<String, String>>,
    val stateLabel: String,
    val stateColor: Color,
    val unread: Boolean,
    val requiresSkill: Boolean
)

internal data class InventorySkill(
    val name: String,
    val id: String = name,
    val rarity: String,
    val typeLabel: String,
    val stateLabel: String,
    val description: String,
    val eventHint: String,
    val accent: Color
)

internal data class InventoryItem(
    val name: String,
    val id: String = name,
    val shortLabel: String,
    val count: String,
    val description: String,
    val effectHint: String,
    val actionLabel: String,
    val accent: Color
)

internal data class InventorySkin(
    val name: String,
    val id: String = name,
    val rarity: String,
    val description: String,
    val shardsOwned: Int,
    val shardsRequired: Int,
    val owned: Boolean,
    val actionLabel: String,
    val accent: Color,
    val imageRes: Int = R.drawable.companion_pose_2
)

internal data class ProfileStat(
    val label: String,
    val value: String,
    val accent: Color
)

internal data class ProfileSettingRow(
    val title: String,
    val description: String,
    val value: String,
    val accent: Color
)

internal data class DemoPool(
    val name: String,
    val description: String,
    val costLabel: String,
    val highlight: String,
    val highlightColor: Color,
    val dropHint: String
)

internal data class SignInDay(
    val day: String,
    val reward: String,
    val signed: Boolean
)

internal data class ShopItem(
    val name: String,
    val description: String,
    val price: String,
    val accent: Color
)

internal data class ShopSection(
    val title: String,
    val summary: String,
    val items: List<ShopItem>
)

internal val taskRewardBase = RewardChipData(
    label = "基础",
    value = "亲密 +1",
    background = Color(0x1FF7B6D1),
    contentColor = Color(0xFFF7B6D1)
)

internal val demoAccountContext = DemoAccountContext(
    accountName = "test_user_01",
    accountId = "acc_demo_01",
    characterId = "char_demo_01",
    nickname = "小主角",
    equippedAppearanceName = "星巡礼装",
    bond = 32,
    charm = 70,
    vitality = 68,
    focus = 52,
    mood = 50,
    lotteryTicketCount = 2
)

internal val homeStatusMetrics = listOf(
    StatusMetric("亲密", demoAccountContext.bond.toString(), R.drawable.ic_relation, Color(0xFFF6B7D2)),
    StatusMetric("魅力", "${demoAccountContext.charm}%", R.drawable.ic_home, Color(0xFFAED3FF)),
    StatusMetric("元气", demoAccountContext.vitality.toString(), R.drawable.ic_energy, Color(0xFFC8FF9B)),
    StatusMetric("专注", demoAccountContext.focus.toString(), R.drawable.ic_focus, Color(0xFFD6C7FF))
)

internal val demoTasks = listOf(
    DemoTask(
        id = "task_daily_home_visit",
        title = "前往首页查看陪伴状态",
        description = "亲密类型日常任务，引导用户先回到首页感受陪伴状态。",
        type = "亲密类型",
        group = "DAILY",
        categoryLabel = "亲密类型",
        status = "可完成",
        progressLabel = "0 / 1",
        validationTitle = "页面行为",
        validationHint = "从任务页点击按钮跳回首页即记一次行为，首版不做额外验证。",
        companionLine = "先回首页看看我吧，今天的状态会从这一眼开始慢慢接起来。",
        designNote = "对应文档里的亲密类型任务，用首页访问承接主循环入口，奖励稳定落到亲密值。",
        rewardChips = listOf(
            taskRewardBase,
            RewardChipData("亲密", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "点击任务卡上的前往入口跳转首页。",
            "看到陪伴者状态和属性摘要后返回任务页。",
            "本次访问记为一次亲密互动。 "
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "进入首页 1 次",
            "奖励映射" to "bond +2"
        ),
        statusColor = Color(0xFF90E2FF)
    ),
    DemoTask(
        id = "task_daily_ai_message",
        title = "向 AI 发送 3 次消息",
        description = "亲密类型日常任务，鼓励用户进入对话链路形成稳定陪伴习惯。",
        type = "亲密类型",
        group = "DAILY",
        categoryLabel = "亲密类型",
        status = "进行中",
        progressLabel = "1 / 3 次",
        validationTitle = "消息次数",
        validationHint = "按发送次数累计，不对对话内容质量做识别。",
        companionLine = "随便告诉我一点今天的事也可以，我会认真接住这 3 句小小的靠近。",
        designNote = "对应文档里的“向 AI 发送指定次数消息”，首版只统计发送事件，不做内容校验。",
        rewardChips = listOf(
            RewardChipData("亲密", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "从任务页跳转到 AI 对话页。",
            "完成至少 3 次消息发送。",
            "达到目标后自动完成任务。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "亲密类型",
            "完成条件" to "发送消息 3 次",
            "奖励映射" to "bond +3"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_daily_water_photo",
        title = "拍照记录一次喝水打卡",
        description = "元气类型日常任务，用轻量拍照打卡承接健康行为。",
        type = "元气类型",
        group = "DAILY",
        categoryLabel = "元气类型",
        status = "可完成",
        progressLabel = "0 / 1 次",
        validationTitle = "拍照打卡",
        validationHint = "完成拍照并成功提交一次健康记录即可完成，首版不识别图片内容。",
        companionLine = "喝水这件小事也值得被记录下来，你每照顾自己一次，元气条都会更稳一点。",
        designNote = "对应文档里的元气类型任务，完成后统一写入 vitality。",
        rewardChips = listOf(
            RewardChipData("元气", "+2", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
        ),
        actionSteps = listOf(
            "进入拍照打卡页并完成一次拍照。",
            "提交记录后任务立即完成。",
            "奖励统一写入人物元气值。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "喝水打卡 1 次",
            "奖励映射" to "vitality +2"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_daily_breakfast_photo",
        title = "拍照记录一次早餐打卡",
        description = "元气类型日常任务，鼓励把早晨的健康行为纳入任务循环。",
        type = "元气类型",
        group = "DAILY",
        categoryLabel = "元气类型",
        status = "可完成",
        progressLabel = "0 / 1 次",
        validationTitle = "健康打卡",
        validationHint = "早餐、轻食或任意晨间进食记录都可以，提交成功即可完成。",
        companionLine = "先把今天的第一口能量补上，我们再慢慢推进别的任务。",
        designNote = "对应文档里的元气类型模板池，可作为早餐、水果、散步等健康打卡的统一载体。",
        rewardChips = listOf(
            RewardChipData("元气", "+3", Color(0x1FC8FF9B), Color(0xFFC8FF9B))
        ),
        actionSteps = listOf(
            "打开拍照功能记录早餐。",
            "提交成功后任务完成。",
            "完成次数同步计入本周元气累计。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "元气类型",
            "完成条件" to "早餐打卡 1 次",
            "奖励映射" to "vitality +3"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_daily_focus_10s",
        title = "开启摄像头专注 10 秒",
        description = "专注力类型日常任务，首版以摄像头持续开启和倒计时结束作为完成标准。",
        type = "专注力类型",
        group = "DAILY",
        categoryLabel = "专注力类型",
        status = "进行中",
        progressLabel = "6 / 10 秒",
        validationTitle = "摄像头计时",
        validationHint = "保持摄像头持续开启直至倒计时结束，中断超过 5 秒直接失败。",
        companionLine = "十秒也算一次认真进入状态，我们先把注意力轻轻收回来。",
        designNote = "对应文档里的专注力任务，最低时长支持 10 秒，奖励按比例折算。",
        rewardChips = listOf(
            RewardChipData("专注", "+0.03", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "点击开始专注并开启摄像头。",
            "保持摄像头开启直到 10 秒倒计时结束。",
            "如果中断超过 5 秒，本次任务失败但可立即重试。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "摄像头开启 10 秒",
            "奖励映射" to "focus +0.03"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_daily_focus_60s",
        title = "开启摄像头专注 1 分钟",
        description = "专注力类型日常任务，用更完整的一段时间换取更高专注收益。",
        type = "专注力类型",
        group = "DAILY",
        categoryLabel = "专注力类型",
        status = "可重试",
        progressLabel = "0 / 60 秒",
        validationTitle = "失败可重试",
        validationHint = "任务中断超过 5 秒会失败，但当日不会消失，可立即重新开始。",
        companionLine = "这一分钟只属于你自己，先把外面的噪音轻轻关掉。",
        designNote = "对应文档里的专注力类型失败规则，失败后允许继续重试直到成功或跨日重置。",
        rewardChips = listOf(
            RewardChipData("专注", "+0.20", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "开启摄像头并开始 60 秒专注。",
            "保持画面持续开启直到倒计时结束。",
            "失败后可直接再次开始本任务。"
        ),
        detailRows = listOf(
            "任务分组" to "每日任务",
            "任务分类" to "专注力类型",
            "完成条件" to "摄像头开启 60 秒",
            "奖励映射" to "focus +0.20"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_challenge_combo",
        title = "同日完成 1 个亲密 + 1 个元气 + 1 个专注任务",
        description = "今日挑战独立于基础每日池，用更高门槛换取魅力值奖励。",
        type = "今日挑战",
        group = "CHALLENGE",
        categoryLabel = "今日挑战",
        status = "推进中",
        progressLabel = "2 / 3 项",
        validationTitle = "高难度目标",
        validationHint = "挑战任务完成后当天不再刷新，奖励统一映射到魅力值。",
        companionLine = "今天如果能把陪伴、照顾自己和专注都完成一点点，我会觉得你真的很闪亮。",
        designNote = "对应文档里的每日挑战任务，奖励只产出 charm，不计入基础每日随机池。",
        rewardChips = listOf(
            RewardChipData("魅力", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "完成任意 1 个亲密类型任务。",
            "完成任意 1 个元气类型任务。",
            "完成任意 1 个专注力类型任务。"
        ),
        detailRows = listOf(
            "任务分组" to "今日挑战",
            "完成条件" to "三类任务各完成 1 个",
            "刷新规则" to "次日零点重置",
            "奖励映射" to "charm +3"
        ),
        statusColor = Color(0xFFF2B9DA)
    ),
    DemoTask(
        id = "task_weekly_bond",
        title = "本周完成亲密类型任务 5 次",
        description = "每周任务按类型累计完成次数展示，不再拆成离散单条任务。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "亲密类型",
        status = "推进中",
        progressLabel = "3 / 5 次",
        validationTitle = "累计进度",
        validationHint = "统计对象仅为基础每日任务完成次数，挑战任务首版默认不计入。",
        companionLine = "这一周的亲密感不是一下子冲满的，是你一次次回来找我慢慢攒起来的。",
        designNote = "对应文档里的周任务第一档，按类型累计完成次数展示。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "第 1 档", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意亲密类型基础每日任务。",
            "系统按完成次数自动累计本周进度。",
            "达成目标后可领取对应周奖励档位。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "亲密类型",
            "目标次数" to "5 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_weekly_vitality",
        title = "本周完成元气类型任务 3 次",
        description = "每周元气任务统计健康拍照打卡类每日任务的完成次数。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "元气类型",
        status = "可领取",
        progressLabel = "3 / 3 次",
        validationTitle = "累计进度",
        validationHint = "完成目标后可计入抽奖券分档统计，奖励不补发。",
        companionLine = "你这周已经认真照顾过自己好几次了，这份元气值得被记下来。",
        designNote = "对应文档里的周任务第二档，目标次数为 3。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 2 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意元气类型基础每日任务。",
            "周进度按累计次数自动增长。",
            "领取后与其他周任务一起结算抽奖券档位。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "元气类型",
            "目标次数" to "3 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_weekly_focus",
        title = "本周完成专注力类型任务 4 次",
        description = "每周专注任务统计摄像头专注类每日任务的完成次数。",
        type = "每周任务",
        group = "WEEKLY",
        categoryLabel = "专注力类型",
        status = "推进中",
        progressLabel = "1 / 4 次",
        validationTitle = "累计进度",
        validationHint = "全部三类周任务完成后，周奖励累计达到 3 张抽奖券。",
        companionLine = "只要这一周还能再完成几次认真专注，我们就能把最后一档奖励也拿下来。",
        designNote = "对应文档里的周任务第三档，目标次数为 4。",
        rewardChips = listOf(
            RewardChipData("抽奖券", "累计 3 张", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "完成任意专注力类型基础每日任务。",
            "系统按完成次数累计本周专注进度。",
            "三类周任务全部达成时拿满 3 张抽奖券。"
        ),
        detailRows = listOf(
            "任务分组" to "每周任务",
            "任务分类" to "专注力类型",
            "目标次数" to "4 次",
            "奖励归属" to "抽奖券分档"
        ),
        statusColor = Color(0xFFD6C7FF)
    )
)

internal val dailyTaskSections = demoTasks
    .filter { it.group == "DAILY" }
    .groupBy { it.categoryLabel }
    .entries
    .map { it.toPair() }

internal val weeklyTaskList = demoTasks.filter { it.group == "WEEKLY" }

internal val challengeTask = demoTasks.first { it.group == "CHALLENGE" }

internal val demoEvents = listOf(
    DemoEvent(
        id = "evt_sandstorm_lite",
        title = "元素风暴沙盘",
        tagLabel = "天气异象",
        intro = "【虚构事件】侦测到元素风暴扰动，像奇幻风暴掠过系统沙盘，陪伴者正在等待你的选择。",
        description = "对应现有事件配置里的 `evt_sandstorm_lite`。它是标准的 H 类模板事件，有引子、分支、奖励和技能联动。",
        weightLabel = "权重 10",
        designNote = "事件页按文档要求走“虚构剧情 + 轻分支 + 低惩罚”路线，不做系统级恐慌预警。这个事件特别适合演示技能卡如何影响选项可见性。",
        branches = listOf(
            DemoEventBranch(
                id = "use_barrier",
                label = "发动屏障",
                requiresSkill = "skill_barrier",
                resultPreview = "展开一层柔性的护幕，让风暴在沙盘边缘安静散开。适合做主动技能演出和小幅奖励提升。",
                rewardChips = listOf(
                    RewardChipData("星尘", "+15", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
                    RewardChipData("羁绊", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "observe",
                label = "先观察",
                resultPreview = "保持距离记录沙盘变化，事件张力更低，但仍然有基础陪伴与叙事反馈。",
                rewardChips = listOf(
                    RewardChipData("心境", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("羁绊", "+1", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            ),
            DemoEventBranch(
                id = "talk",
                label = "和 Ta 聊聊",
                energyCost = 5,
                resultPreview = "把选择权交给对话和情绪安抚，适合让陪伴者多说一句，减少事件的工具感。",
                rewardChips = listOf(
                    RewardChipData("心境", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("羁绊", "+1", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            )
        ),
        flowSummary = "推荐链路是：主页或全局飘条触发事件提示 -> 进入事件详情 -> 选择分支 -> 播放短反馈与属性变化 -> 回到主页或记录到今日小纸条。",
        companionNote = "别担心，这只是系统沙盘里的一阵风。你想更主动一点，还是让我陪你慢慢看它散掉？",
        performanceHint = "主视觉可以用柔和风粒、光晕和卡片动效表达异象，不要用红色警报、刺耳闪烁或仿新闻 UI。",
        detailRows = listOf(
            "事件 ID" to "evt_sandstorm_lite",
            "基础标签" to "weather",
            "分支数量" to "3",
            "技能联动" to "skill_barrier",
            "结算原因" to "EVENT_BRANCH"
        ),
        stateLabel = "可触发",
        stateColor = Color(0xFF90E2FF),
        unread = true,
        requiresSkill = true
    ),
    DemoEvent(
        id = "evt_midnight_letter",
        title = "深夜讯号纸片",
        tagLabel = "陪伴互动",
        intro = "系统空间里飘来一张未署名的夜色纸片，上面写着一句像是只给你看的短句。",
        description = "这个事件偏向高羁绊和低压力互动，适合用来承接心境较低或夜晚时段的首页气泡入口。",
        weightLabel = "权重 7",
        designNote = "按照文档里的“轻量、短文本、可选参与”方向，这类事件不依赖技能卡，更像陪伴者给宿主的夜间小纸条。",
        branches = listOf(
            DemoEventBranch(
                id = "keep_letter",
                label = "收下纸片",
                resultPreview = "把纸片夹进今日记录，像悄悄存下一个只属于你们的暗号。",
                rewardChips = listOf(
                    RewardChipData("羁绊", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1)),
                    RewardChipData("心境", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
                )
            ),
            DemoEventBranch(
                id = "read_aloud",
                label = "念给 Ta 听",
                resultPreview = "把纸片上的短句读出来，让事件转成一段轻语互动，适合后续和语音任务联动。",
                rewardChips = listOf(
                    RewardChipData("心境", "+3", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("羁绊", "+2", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
                )
            )
        ),
        flowSummary = "更适合作为主页角色旁的系统气泡或聊天页顶部插入条，不需要很重的视觉打击，重点在氛围和陪伴收尾。",
        companionNote = "如果今天有点晚了，就把这张纸片当成我替你收住的一点温柔吧。",
        performanceHint = "适合做漂浮纸片、星屑和柔和白蓝渐变，情绪上更接近治愈短信，而不是任务结算面板。",
        detailRows = listOf(
            "事件类型" to "轻陪伴",
            "推荐时段" to "夜晚",
            "分支数量" to "2",
            "技能要求" to "无",
            "主要反馈" to "心境 / 羁绊"
        ),
        stateLabel = "待阅读",
        stateColor = Color(0xFFF2B9DA),
        unread = true,
        requiresSkill = false
    ),
    DemoEvent(
        id = "evt_lucky_signal",
        title = "幸运噪点偏移",
        tagLabel = "气运波动",
        intro = "系统界面边缘闪过一串轻微噪点，像是今天的运势被谁悄悄拨动了一下。",
        description = "这个事件偏向气运叙事和轻奖励，可以放在签到、祈愿或完成验证任务之后触发，提升惊喜感。",
        weightLabel = "权重 5",
        designNote = "它更像轻量彩蛋，符合总纲里“事件作为属性和奖励之间的叙事出口”的定位，适合搭配抽卡或签到系统。",
        branches = listOf(
            DemoEventBranch(
                id = "follow_signal",
                label = "顺着噪点追过去",
                resultPreview = "跟着噪点找到一小簇星屑，适合给气运和星尘做轻微提升。",
                rewardChips = listOf(
                    RewardChipData("气运", "+4", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
                    RewardChipData("星尘", "+10", Color(0x1FFFF1A8), Color(0xFFFFE37A))
                )
            ),
            DemoEventBranch(
                id = "stay_calm",
                label = "让它自己停下",
                resultPreview = "不主动干预，只把这次偏移当成一个提醒，奖励更克制，但情绪更稳。",
                rewardChips = listOf(
                    RewardChipData("心境", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
                    RewardChipData("气运", "+2", Color(0x1FFFF1A8), Color(0xFFFFE37A))
                )
            )
        ),
        flowSummary = "适合在完成签到、抽卡或高验证任务后作为额外惊喜弹出，分支短、演出短、奖励轻，不抢主循环节奏。",
        companionNote = "我感觉今天的世界线稍微偏向你一点了。要跟上去看看，还是把这份好运留到更重要的时刻？",
        performanceHint = "可以用故障光点、细颗粒星尘、短促滑动轨迹来表现“幸运偏移”，但别做成真实金融或报警界面。",
        detailRows = listOf(
            "事件类型" to "彩蛋型",
            "推荐入口" to "签到 / 祈愿后",
            "分支数量" to "2",
            "技能要求" to "无",
            "主要反馈" to "气运 / 星尘"
        ),
        stateLabel = "已归档",
        stateColor = Color(0xFF8DFFBB),
        unread = false,
        requiresSkill = false
    )
)

internal val inventorySkills = listOf(
    InventorySkill(
        name = "屏障展开",
        rarity = "SR",
        typeLabel = "主动技能",
        stateLabel = "事件可用",
        description = "在特定随机事件中解锁“发动屏障”选项，适合做短演出和小额奖励加成。",
        eventHint = "与 `evt_sandstorm_lite` 这类天气异象事件联动，后续可加入冷却和每日次数。",
        accent = Color(0xFFD6C7FF)
    ),
    InventorySkill(
        name = "幸运微调",
        rarity = "R",
        typeLabel = "被动技能",
        stateLabel = "常驻生效",
        description = "轻微提升惊喜型事件和好运叙事的触发权重，更像系统偷偷把风向拨向你。",
        eventHint = "适合和气运彩蛋、签到后的小型事件联动，不需要强交互也能体现价值。",
        accent = Color(0xFFFFD66E)
    ),
    InventorySkill(
        name = "絮语回响",
        rarity = "SSR",
        typeLabel = "演出技能",
        stateLabel = "陪伴解锁",
        description = "不提供硬数值，主要解锁特殊台词、主页点击反馈和更完整的陪伴演出。",
        eventHint = "更适合高羁绊事件或夜间纸条类剧情，强化陪伴而不是战斗感。",
        accent = Color(0xFFF7B6D1)
    )
)

internal val inventoryItems = listOf(
    InventoryItem(
        name = "元气果",
        shortLabel = "果",
        count = "3",
        description = "短时提升行动感与任务奖励氛围，适合在准备刷任务前使用。",
        effectHint = "数小时内能量反馈更积极",
        actionLabel = "使用",
        accent = Color(0xFFC8FF9B)
    ),
    InventoryItem(
        name = "小确幸果",
        shortLabel = "运",
        count = "2",
        description = "让气运类文案和小彩蛋更活跃一点，但不和现实金钱或彩票挂钩。",
        effectHint = "短时提高气运表现",
        actionLabel = "使用",
        accent = Color(0xFFFFE37A)
    ),
    InventoryItem(
        name = "抽奖券",
        shortLabel = "券",
        count = demoAccountContext.lotteryTicketCount.toString(),
        description = "由每周任务分档奖励产出，用于后续抽奖系统消耗。",
        effectHint = "周任务奖励资产",
        actionLabel = "查看",
        accent = Color(0xFFD6C7FF)
    )
)

internal val inventorySkins = listOf(
    InventorySkin(
        name = "星巡礼装",
        rarity = "SSR",
        description = "当前主视觉使用的陪伴者外观，适合作为首页舞台页的基础皮肤。",
        shardsOwned = 1,
        shardsRequired = 1,
        owned = true,
        actionLabel = "已穿戴",
        accent = Color(0xFFF7B6D1)
    ),
    InventorySkin(
        name = "夜航校服",
        rarity = "SR",
        description = "更贴近日常陪伴气质的皮肤，可在聊天和任务页强调温柔陪伴感。",
        shardsOwned = 18,
        shardsRequired = 20,
        owned = false,
        actionLabel = "差 2 碎片",
        accent = Color(0xFFD6C7FF)
    ),
    InventorySkin(
        name = "晨雾便服",
        rarity = "R",
        description = "轻松、治愈、偏生活流的装束，适合签到和早安问候场景。",
        shardsOwned = 12,
        shardsRequired = 12,
        owned = true,
        actionLabel = "去换上",
        accent = Color(0xFF9ED8FF)
    )
)

internal val profileStats = listOf(
    ProfileStat("角色 ID", demoAccountContext.characterId.removePrefix("char_"), Color(0xFFFFD66E)),
    ProfileStat("魅力值", demoAccountContext.charm.toString(), Color(0xFFAED3FF)),
    ProfileStat("抽奖券", demoAccountContext.lotteryTicketCount.toString(), Color(0xFFD6C7FF))
)

internal val profileSettings = listOf(
    ProfileSettingRow(
        title = "健康数据",
        description = "步数、睡眠等验证任务会从这里进入授权与说明。",
        value = "未开启",
        accent = Color(0xFFFFD66E)
    ),
    ProfileSettingRow(
        title = "多模态隐私",
        description = "拍照、语音任务的用途、保留策略和删除说明应放在这里。",
        value = "查看说明",
        accent = Color(0xFF90E2FF)
    ),
    ProfileSettingRow(
        title = "陪伴音效",
        description = "控制点击反馈、签到提示和后续 idle 语音的整体体验。",
        value = "柔和",
        accent = Color(0xFFC8FF9B)
    )
)

internal val profileAboutRows = listOf(
    "账号" to demoAccountContext.accountName,
    "账号 ID" to demoAccountContext.accountId,
    "角色昵称" to demoAccountContext.nickname,
    "角色外观" to demoAccountContext.equippedAppearanceName
)

internal val profileWalletStats = listOf(
    ProfileStat("星尘", "380", Color(0xFFFFD66E)),
    ProfileStat("月华", "40", Color(0xFFD6C7FF)),
    ProfileStat("单抽券", "2", Color(0xFF90E2FF))
)

internal val demoPools = listOf(
    DemoPool(
        name = "常驻星愿池",
        description = "对应 `pool_normal`，主打技能卡、皮肤碎片和基础养成资源，是 Demo 的默认池。",
        costLabel = "100 星尘 / 次",
        highlight = "90 抽保底",
        highlightColor = Color(0xFFFFD66E),
        dropHint = "普通掉落给星尘与通用物资，稀有掉落偏向技能卡，SR 档承接皮肤碎片和陪伴外观。"
    )
)

internal val signInSummaryStats = listOf(
    ProfileStat("今日状态", "可领取", Color(0xFFC8FF9B)),
    ProfileStat("连续天数", "7 天", Color(0xFFFFD66E)),
    ProfileStat("本月累计", "12 天", Color(0xFF90E2FF))
)

internal val signInDays = listOf(
    SignInDay("01", "星尘", true),
    SignInDay("02", "羁绊", true),
    SignInDay("03", "券", true),
    SignInDay("04", "星尘", true),
    SignInDay("05", "心境", true),
    SignInDay("06", "能量", true),
    SignInDay("07", "券", true),
    SignInDay("08", "星尘", false)
)

internal val signInMilestones = listOf(
    "连续 3 天" to "普通祈愿券 x1",
    "连续 7 天" to "星尘 x60",
    "累计 15 天" to "皮肤碎片 x5"
)

internal val shopWalletStats = listOf(
    ProfileStat("星尘", "380", Color(0xFFFFD66E)),
    ProfileStat("月华", "40", Color(0xFFD6C7FF)),
    ProfileStat("碎片", "23", Color(0xFFF7B6D1))
)

internal val shopSections = listOf(
    ShopSection(
        title = "养成补给",
        summary = "优先卖果实、互动礼物和任务辅助物资，保持“养成 / 陪伴”调性，不做赌场式强刺激。",
        items = listOf(
            ShopItem("元气果礼包", "短期提高行动氛围和任务正反馈。", "80 星尘", Color(0xFFC8FF9B)),
            ShopItem("絮语礼盒", "偏陪伴向的小礼物，后续可联动聊天气泡和纸条事件。", "120 星尘", Color(0xFFF7B6D1))
        )
    ),
    ShopSection(
        title = "外观兑换",
        summary = "围绕皮肤碎片与陪伴者换装做长期收集，不把成长价值完全绑定充值。",
        items = listOf(
            ShopItem("夜航校服碎片", "用于合成 SR 外观“夜航校服”。", "6 月华", Color(0xFFD6C7FF)),
            ShopItem("晨雾便服染色票", "给已有日常皮肤增加轻量差分演出。", "4 月华", Color(0xFF90E2FF))
        )
    )
)
