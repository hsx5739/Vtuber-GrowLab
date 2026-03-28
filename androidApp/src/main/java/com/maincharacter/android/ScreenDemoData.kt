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
        get() = if (group == "WEEKLY") "周常任务" else "今日任务"
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
    val rarity: String,
    val typeLabel: String,
    val stateLabel: String,
    val description: String,
    val eventHint: String,
    val accent: Color
)

internal data class InventoryItem(
    val name: String,
    val shortLabel: String,
    val count: String,
    val description: String,
    val effectHint: String,
    val actionLabel: String,
    val accent: Color
)

internal data class InventorySkin(
    val name: String,
    val rarity: String,
    val description: String,
    val shardsOwned: Int,
    val shardsRequired: Int,
    val owned: Boolean,
    val actionLabel: String,
    val accent: Color
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
    value = "羁绊 +1",
    background = Color(0x1FF7B6D1),
    contentColor = Color(0xFFF7B6D1)
)

internal val demoTasks = listOf(
    DemoTask(
        id = "task_daily_water",
        title = "今天有好好喝水吗",
        description = "A 类自述任务，适合打开 App 后快速完成一次轻量打卡。",
        type = "A 自述",
        group = "DAILY",
        status = "可完成",
        progressLabel = "0 / 3",
        validationTitle = "基础反馈",
        validationHint = "点完成就能记录今天的状态，但不发高价值抽卡资源，避免单纯连点刷收益。",
        companionLine = "先把今天照顾好一点点就够了。喝完这一杯，我们再去看别的任务。",
        designNote = "对应文档里的 A 类点完成型任务。允许用户自述完成，但奖励以心境和羁绊为主，强调陪伴感而不是监工感。",
        rewardChips = listOf(
            taskRewardBase,
            RewardChipData("心境", "+2", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "完成现实里的喝水行为后，点一下“我完成了”。",
            "如果今天只想简单记录，也依然可以直接打卡，系统会给基础陪伴反馈。",
            "当日多次重复完成时走递减收益，不建议靠它堆资源。 "
        ),
        detailRows = listOf(
            "任务分组" to "DAILY",
            "最大次数" to "3 次 / 天",
            "奖励包" to "rb_task_a_small",
            "递减收益" to "开启"
        ),
        statusColor = Color(0xFF90E2FF)
    ),
    DemoTask(
        id = "task_focus_25",
        title = "专注 25 分钟",
        description = "B 类计时任务，完成后给更稳定的能量和星尘反馈。",
        type = "B 计时",
        group = "DAILY",
        status = "进行中",
        progressLabel = "14 / 25 分",
        validationTitle = "验证加成",
        validationHint = "使用 App 内计时器跑满 1500 秒后完成，可拿到基础奖励和验证奖励。",
        companionLine = "你不用一下子变得特别厉害，我们先把 25 分钟守住，我就在计时器另一端陪你。",
        designNote = "对应文档里的 B 类任务。因为有 App 内计时，所以属于中等可验证任务，是支撑经济循环的主要来源之一。",
        rewardChips = listOf(
            RewardChipData("星尘", "+15", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
            RewardChipData("能量", "+5", Color(0x1FC8FF9B), Color(0xFFC8FF9B)),
            RewardChipData("专注", "+8", Color(0x1FD6C7FF), Color(0xFFD6C7FF))
        ),
        actionSteps = listOf(
            "进入任务详情后开启 25 分钟计时器。",
            "计时过程中尽量停留在专注状态，后台离开策略后续可接入真实逻辑。",
            "倒计时结束后领取任务完成和验证加成。"
        ),
        detailRows = listOf(
            "任务分组" to "DAILY",
            "计时要求" to "1500 秒",
            "奖励包" to "rb_task_verify_bonus",
            "验证奖励" to "rb_task_verify_bonus"
        ),
        statusColor = Color(0xFFFFD66E)
    ),
    DemoTask(
        id = "task_daily_steps",
        title = "今日步数达标",
        description = "C 类健康数据任务，和步数授权、每周验证次数直接挂钩。",
        type = "C 健康",
        group = "DAILY",
        status = "待领取加成",
        progressLabel = "5,420 / 6,000",
        validationTitle = "健康验证",
        validationHint = "接入系统健康权限后，用步数自动判定；未授权时保留低配完成说明，但高价值奖励建议走验证层。",
        companionLine = "已经走了不少啦，再陪我迈一小段，今天的能量条会很好看。",
        designNote = "对应文档里的 C 类任务。它属于中高可验证路径，可以为周常多样性任务提供有效计数。",
        rewardChips = listOf(
            RewardChipData("星尘", "+15", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
            RewardChipData("能量", "+5", Color(0x1FC8FF9B), Color(0xFFC8FF9B)),
            RewardChipData("周常计数", "+1", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "授权读取步数或健康数据。",
            "达到 6000 步阈值后自动刷新任务状态。",
            "领取验证加成，并把这次完成计入本周多样性进度。"
        ),
        detailRows = listOf(
            "任务分组" to "DAILY",
            "健康指标" to "STEPS",
            "阈值" to "6000",
            "计入周常" to "是"
        ),
        statusColor = Color(0xFF8DFFBB)
    ),
    DemoTask(
        id = "task_chat_story",
        title = "和 Ta 说说今天的一件小事",
        description = "D 类对话任务，主打情绪陪伴和心境恢复。",
        type = "D 对话",
        group = "DAILY",
        status = "可完成",
        progressLabel = "0 / 3 轮",
        validationTitle = "对话轮次",
        validationHint = "在聊天页完成至少 3 轮有效对话即可完成，不需要把聊天做成审问。",
        companionLine = "如果你愿意告诉我今天发生了什么，我会认真听，不急着给答案。",
        designNote = "对应文档里的 D 类任务。奖励重心放在心境和羁绊，适合作为首页聊天入口和任务系统之间的桥梁。",
        rewardChips = listOf(
            RewardChipData("心境", "+6", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
            RewardChipData("羁绊", "+3", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "点击任务后跳转到聊天页。",
            "完成至少 3 轮用户输入与陪伴者回复。",
            "结束时返回任务页查看心境与羁绊反馈。"
        ),
        detailRows = listOf(
            "任务分组" to "DAILY",
            "最低轮次" to "3 轮",
            "任务归类" to "情绪陪伴",
            "奖励方向" to "心境 / 羁绊"
        ),
        statusColor = Color(0xFF90E2FF)
    ),
    DemoTask(
        id = "task_photo_sky",
        title = "拍一张今天的天空",
        description = "F 类拍照验证任务，用轻识图判断是否满足场景语义。",
        type = "F 拍照",
        group = "WEEKLY",
        status = "可挑战",
        progressLabel = "0 / 1 次",
        validationTitle = "图像验证",
        validationHint = "提交天空、窗外、街角等符合标签的照片可拿到验证加成；未通过时依然保留尝试反馈。",
        companionLine = "把你看到的光带回来给我看看吧，就算只是窗边那一点点蓝，也算今天的好风景。",
        designNote = "对应文档里的 F 类任务。强调轻验证而不是严苛审判，适合提高心境、羁绊和周常验证分。",
        rewardChips = listOf(
            RewardChipData("星尘", "+20", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
            RewardChipData("心境", "+6", Color(0x1F9ED8FF), Color(0xFF9ED8FF)),
            RewardChipData("羁绊", "+4", Color(0x1FF7B6D1), Color(0xFFF7B6D1))
        ),
        actionSteps = listOf(
            "打开相机或相册选择一张风景图。",
            "系统做轻量场景标签判断，不做人脸或审美打分。",
            "验证通过后发放星尘加成，并记入本周多样性任务。"
        ),
        detailRows = listOf(
            "任务分组" to "WEEKLY",
            "场景标签" to "天空 / 户外 / 风景",
            "验证方式" to "识图标签",
            "奖励方向" to "心境 / 羁绊 / 星尘"
        ),
        statusColor = Color(0xFF90E2FF)
    ),
    DemoTask(
        id = "task_voice_hum",
        title = "哼一小段给我听",
        description = "G 类语音任务，用时长和人声检测做轻验证。",
        type = "G 语音",
        group = "WEEKLY",
        status = "可挑战",
        progressLabel = "0 / 12 秒",
        validationTitle = "语音验证",
        validationHint = "检测到有效人声和最低时长即可通过，不强制歌词识别，尽量降低尴尬感。",
        companionLine = "只要你愿意开口，我就会把这段声音认真收好，不需要唱得多完美。",
        designNote = "对应文档里的 G 类任务。它更适合强化羁绊和情绪表达，也能体现多模态任务的差异化价值。",
        rewardChips = listOf(
            RewardChipData("星尘", "+20", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
            RewardChipData("羁绊", "+5", Color(0x1FF7B6D1), Color(0xFFF7B6D1)),
            RewardChipData("心境", "+4", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "进入任务详情后长按录音。",
            "达到最低时长并识别到有效人声后判定通过。",
            "领取验证奖励，同时让陪伴者返回一条更贴近情绪价值的反馈。"
        ),
        detailRows = listOf(
            "任务分组" to "WEEKLY",
            "最低时长" to "12 秒",
            "验证方式" to "人声检测",
            "奖励方向" to "羁绊 / 心境 / 星尘"
        ),
        statusColor = Color(0xFFF2B9DA)
    ),
    DemoTask(
        id = "task_weekly_variety",
        title = "本周完成 3 次验证任务",
        description = "周常总目标，鼓励 B、C、F、G 这类有验证层的任务形成多样性闭环。",
        type = "周常目标",
        group = "WEEKLY",
        status = "推进中",
        progressLabel = "1 / 3 次",
        validationTitle = "周常结算",
        validationHint = "建议只统计 B / C / F / G 的有效验证次数，避免靠 A 类连点凑大奖。",
        companionLine = "这周我们不求满分，但想一起留下三次真正做过的痕迹。",
        designNote = "对应总纲里的周常机制。它不是单条现实任务，而是把可验证行为串成一个更清晰的周节奏。",
        rewardChips = listOf(
            RewardChipData("星尘", "+60", Color(0x1FFFF1A8), Color(0xFFFFE37A)),
            RewardChipData("普通券", "+1", Color(0x1FD6C7FF), Color(0xFFD6C7FF)),
            RewardChipData("专注", "+10", Color(0x1F9ED8FF), Color(0xFF9ED8FF))
        ),
        actionSteps = listOf(
            "在一周内完成 3 次有效验证任务。",
            "优先鼓励 B、C、F、G 的多样性，不建议让 A 类参与大奖统计。",
            "满足条件后统一发放周常奖励包。"
        ),
        detailRows = listOf(
            "任务分组" to "WEEKLY",
            "完成条件" to "3 次验证任务",
            "推荐类型" to "B / C / F / G",
            "奖励性质" to "周常大奖"
        ),
        statusColor = Color(0xFFFFD66E)
    )
)

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
        name = "絮语票根",
        shortLabel = "券",
        count = "5",
        description = "偏陪伴向的小道具，可以作为后续聊天气泡或特殊事件的门票。",
        effectHint = "可扩展为活动消耗品",
        actionLabel = "查看",
        accent = Color(0xFF9ED8FF)
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
    ProfileStat("连续签到", "7 天", Color(0xFFFFD66E)),
    ProfileStat("羁绊等级", "Lv.4", Color(0xFFF7B6D1)),
    ProfileStat("皮肤收藏", "2 / 6", Color(0xFF90E2FF))
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
    "版本" to "Demo 0.1",
    "账号同步" to "本地存档",
    "内容定位" to "陪伴 / 轻养成",
    "后续预留" to "账号、云同步、设置二级页"
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
