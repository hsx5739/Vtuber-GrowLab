# 主角系统：物料清单与 AI 生成提示词（V1.2）

本文列出当前设计下**所需物料**（美术、音频、文案类），并为每类提供**可复制的 AI 生成提示词**模板。  
图像类提示词适用于常见文生图/图生图工具（如 Midjourney、Stable Diffusion、DALL·E、即梦等）；**出稿后须人工审核**版权与合规（无真人肖像、无商标、无违规隐喻）。

**配套文档**：《需求整理》《界面与信息架构》《属性任务与抽奖细化》《游戏机制总纲》。

---

## 1. 全局视觉与叙事基线（所有图像提示词前先定稿）

建议在项目内固定一段 **Style Block**，生成时贴在每条提示词末尾：

```text
[Style Block — 复制到每条图提示末尾]
统一风格：日系二次元半写实、清透配色、柔和光晕、高精度插画；
受众：30岁以下、偏治愈与轻科幻「系统」感；
禁止：真人照片脸、可识别明星、血腥恐怖、色情、真实政府/灾害预警 UI、宗教敏感符号、水印、杂乱文字。
```

**透明底物料专用后缀**（需透明 PNG 的立绘/图标/卡面插画，贴在 Style Block **之后**、或合并进该条英文提示）：

```text
[Transparent suffix — 英文，与 §3 各条已内联一致时可不重复贴]
isolated subject on transparent background, alpha channel, no background scenery, no floor, no horizon line,
clean silhouette edges suitable for PNG with transparency, game asset cutout style.
If the model cannot output true alpha: use solid uniform chroma key green (#00FF00) backdrop only, flat color, no gradient, for post keying.
Negative: checkerboard pattern, messy background, drop shadow on ground, complex environment.
```

**满版背景物料**（封面/弹窗底图等）：在对应提示中写 **`opaque full-bleed illustration`**，**不要**写 transparent。

**陪伴者人设占位**（可替换为你的定稿）：  
「非人类或轻度奇幻人形、系统引导者气质、温和眼神、主色 [填色]、固定发型与瞳色以便多皮肤一致。」

### 1.1 图像格式与透明底（PNG）是否指定？

**有指定，且分物料**：不是「全项目一律透明 PNG」。原则是——**会叠在可变背景上的角色/图标 → 透明底；满版背景图 → 通常不透明。**

| 物料类型 | 推荐格式 | 是否透明底 | 说明 |
|----------|----------|------------|------|
| **陪伴者立绘**（主页、可能与渐变/插画背景合成） | PNG-24 或 WebP（无损含 Alpha） | **必须透明** | 避免白边/灰边；导出后检查发丝锯齿 |
| **头像、Tab/货币/属性/任务角标、道具与碎片图标** | SVG（首选矢量）或 PNG | **建议透明** | 小 PNG 用 @2x/@3x 多密度 |
| **技能卡「中央插画」**（嵌入 UI 卡框） | PNG | **建议透明**或程序裁切；若整卡为**一张合并图**则可不透明 |
| **卡框、按钮等纯 UI** | SVG / 9-patch PNG | 按设计：中间镂空可用九宫格，不必整张透明 |
| **卡池封面、事件弹窗背景、签到头图、空状态、闪屏** | JPG（大图省体积）或 PNG | **一般不需要透明** | 满版铺屏 |
| **应用启动图标** | 各平台导出规范 | **勿做透明「打孔」图标** | 遵循 Apple / Google 图标规范 |

**技术注意**：透明 PNG 使用 **8-bit alpha**；避免误用 **PNG-8 索引透明** 导致半透明发丝发灰。长边大图优先 **PNG**；若用 WebP，需确认 **iOS/Android 与 KMP 展示链**均支持所用 WebP 类型。

### 1.2 人物动画：用「模型」还是静态图？

**两种都做约定**——**Demo 先用静态，动画再上 Live2D/Spine 等「模型」方案**，不要首版就绑死重工作流。

| 方案 | 本质 | 优点 | 成本与风险 | 建议阶段 |
|------|------|------|------------|----------|
| **静态多状态图** | 多张透明 PNG（默认 / 能量低 / 点击反馈等），代码切换或淡入淡出 | 实现简单、包体小、KMP 最省事 | 动感弱 | **P0 Demo 首选** |
| **Live2D（Cubism）** | 2D 拆层 + 网格变形 + 参数驱动 | 陪伴感强、呼吸眨眼口型 | 需 **PSD 分层规范**、建模、**Cubism SDK**、每套皮肤可能要单独或继承工程 | **P1/P2** |
| **Spine 2D** | 骨骼 + 蒙皮 | 游戏向动作丰富 | 需 Spine 授权与工作流；KMP 需接各端 Runtime | **P1/P2 二选一** |
| **序列帧 / Lottie / Rive** | 短循环小动画 | 适合特效、小图标动效 | 大立绘序列帧体积大 | 特效、UI 点缀 |
| **3D 角色** | 真三维模型 | 光影与镜头自由 | 美术与程序量显著上升 | **首版不推荐**，除非产品已定 3D |

**结论（与《需求整理》Demo 一致）**：

1. **先有**：透明 PNG 立绘 + 可选 **1～2 张差分**（如 M02 能量低）即算「会动一点点」。  
2. **再上**：若确定要持续运营陪伴感，在 **Live2D 与 Spine 中二选一**；二者都需要**可拆层的原画**，不是单靠文生图「一张成品」就能直接变模型。  
3. **AI 图像**：适合出 **M01 概念与上色稿**；**拆层、绑骨、动画**仍要人工或外包在对应工具里完成。

---

## 2. 物料总表（按优先级）

**P0** = Demo 最小可演示；**P1** = 首版完整体验；**P2** = 后续扩充。

| 编号 | 物料名称 | 类型 | 优先级 | 规格建议 | 对应界面/系统 |
|------|----------|------|--------|----------|---------------|
| M01 | 陪伴者默认立绘（全身/半身） | 静态图 / 切层 | P0 | 透明底 PNG，长边 2048～4096；主界面居中 | 主页 |
| M02 | 陪伴者「能量低」差分立绘 | 静态图 | P1 | 同 M01 构图可微调表情姿态 | 主页状态 |
| M03 | 陪伴者头像（聊天顶栏） | 静态图 | P0 | 512～1024 方图，圆角裁切 | 聊天 |
| M04 | 皮肤变体 × N | 静态图 / Live2D 皮 | P1 | 同角色骨架下换装；Demo 可先 1～2 套 | 行囊-外观、祈愿 |
| M05 | Live2D / Spine 工程（可选） | 工程文件 | P2 | 与 M01 拆分一致 | 主页动效 |
| M06 | 底栏 Tab 图标 × 5 | 矢量/SVG 或 PNG | P0 | 24/32dp 多密度，选中/未选中双色 | 主导航 |
| M07 | 货币图标：星尘、月华、祈愿券 | 矢量/PNG | P0 | 小图标 48～96px | 顶栏、祈愿 |
| M08 | 宿主五维属性图标：气运/能量/心境/羁绊/专注 | 矢量/PNG | P0 | 胶囊内可用 24～32px | 主页、属性详情 |
| M09 | 任务类型角标 A～H（或 8 个隐喻图标） | 矢量/PNG | P1 | 列表左侧小标 | 任务列表 |
| M10 | 技能卡卡面底图 + 稀有度框 | 图像模板 | P1 | 竖版 2:3，安全区留标题 | 行囊-技能、抽卡结果 |
| M11 | 技能卡画面 × 每张技能 | 图像 | P1 | 嵌入 M10 框内 | 配置表 skillId |
| M12 | 果实/道具图标 × 每种道具 | 图像 | P1 | 256～512 方图透明底 | 行囊-道具 |
| M13 | 皮肤碎片图标 | 图像 | P1 | 与 M04 视觉关联的小图标 | 行囊、掉落 |
| M14 | 祈愿卡池封面图 | 横版插画 | P0 | 16:9 或 3:2，安全区居中；**可不透明** | 祈愿 Tab |
| M15 | 抽卡结果「光效/边框」叠加层 | 序列帧或 Lottie | P1 | 与稀有度联动 | 祈愿动画 |
| M16 | 随机事件弹窗背景（通用 1 + 标签变体） | 图像 | P1 | 9-patch 或中间拉伸；**可不透明** | 事件 H |
| M17 | 签到日历背景/头部插画 | 图像 | P1 | 轻量装饰；**可不透明** | 签到半屏 |
| M18 | 空状态插画（无任务/无道具/无聊天记录） | 图像 | P2 | 温和幽默；**可不透明** | 各列表空态 |
| M19 | App 启动图标 | 矢量 | P0 | 各平台尺寸导出 | 桌面 |
| M20 | 闪屏 Splash | 图像 | P1 | 与品牌色一致；**可不透明** | 启动 |
| M21 | 聊天气泡皮肤（可选） | 9-patch / 图 | P2 | 用户/陪伴者双色 | 聊天 |
| M22 | 音效：点击、完成、抽卡稀有、签到 | 音频 | P1 | WAV/OGG 短音效 | 全局 |
| M23 | 陪伴者短语音 idle（可选） | 音频 | P2 | 按句切分 | 主页点击 |
| M24 | 品牌字体（可选） | 字体授权 | P2 | 标题与正文各一 | UI |

---

## 3. 图像类：分物料 AI 提示词模板

| 编号 | 提示词内是否写明透明底 | 说明 |
|------|------------------------|------|
| M01 M02 M03 M04 M10 M11 M12 | **是**（`transparent background` / `alpha` / 卡框开窗） | 交付物目标：**透明底 PNG**（或绿幕抠图） |
| M14 M16 M17 M18 | **否**，写明 **opaque / full-bleed** | 满版铺屏，**不要透明** |
| M19 | **否**（平台图标自有规范） | 按商店导出，非 PNG 透明孔 |

使用时：**① 替换 [方括号] 内容**；② 贴 §1 **Style Block**；③ **透明类**再核对本条英文里已含 `transparent`；④ 模型若不支持真透明，用提示中的 **chroma green** 方案后期抠图。

### M01 陪伴者默认立绘

**透明底**：是。

```text
Character full body or thigh-up illustration, [gender/neutral fantasy guide], soft and trustworthy expression,
system companion aesthetic, subtle sci-fi hologram accents or rune patterns on clothing, NOT a generic RPG warrior,
isolated on transparent background, alpha channel, no scenery, no floor, no ground shadow cast on environment,
single character centered, anime-influenced semi-realistic painting, game sprite cutout, crisp edges for PNG alpha,
high detail face and eyes, consistent design for visual novel protagonist companion role.
If transparency not supported: solid flat chroma key green #00FF00 only behind character, uniform backdrop for keying.
Negative: duplicate characters, busy background, gradient sky, room interior, text, logo, photorealistic human celebrity face, horror, gore, checkerboard pattern.
```

**变体（能量低 M02）**：在同上角色描述后追加  
`slightly tired eyes, softer shoulders, muted colors, still gentle, not sadistic or mocking.`  
（透明底要求与 M01 相同，勿再出现实景背景。）

### M03 聊天头像

**透明底**：是（圆角由程序裁，底图建议透明）。

```text
Close-up portrait crop of the same character as reference [attach reference], friendly slight smile,
round composition suitable for app avatar, soft lighting, same outfit as default skin,
anime semi-realistic, high clarity,
isolated on transparent background, alpha channel, head and shoulders only, no backdrop.
If transparency not supported: solid flat #00FF00 chroma backdrop only.
Negative: full body, clutter, text, busy scene, checkerboard.
```

### M04 皮肤变体（每张一套）

**透明底**：是。

```text
Same character identity as reference: same face structure, eyes shape, hairstyle base [or describe if hair changes],
NEW outfit theme: [e.g. winter student / cyber hanfu / stargazing pajama], color palette [colors],
accessories consistent with cute system companion, thigh-up or full body, neutral pose for game standing sprite,
coherent with gacha collectible skin design,
isolated on transparent background, alpha channel, no environment, game overlay asset, clean edges.
If transparency not supported: solid flat #00FF00 chroma backdrop only.
Negative: different person, different species, horror, oversexualized, political symbols, indoor scene, ground plane.
```

### M10～M11 技能卡

**卡面插画（M11，每张技能替换名称与意象）**  
**透明底**：是（仅中央插画层，便于塞进卡框）。

```text
Vertical collectible card art, fantasy skill illustration, concept: [skill name e.g. Barrier Field / Wind Sense],
abstract magical effect, no readable text in image, center composition, glowing particles,
soft anime illustration style, suitable for mobile game card frame cropping,
isolated on transparent background, alpha channel, only the magical motif and focal artwork, no card border, no frame.
If transparency not supported: solid flat #00FF00 chroma behind artwork only.
Negative: realistic disaster photo, tornado photojournalism, government warning sign, blood, text blocks, full card template with border.
```

**卡框模板（M10，可一次生成再 UI 叠加）**  
**透明底**：**外框与装饰在透明底上，中间为镂空/透明开窗**（或程序用遮罩）。

```text
Empty mobile game card frame template, vertical 2:3, elegant sci-fi fantasy border, rarity [N/R/SR/SSR] gem corners,
large fully transparent center window for art inset (alpha 0 in middle), only border and ornaments visible on transparent PNG,
subtle holographic sheen on frame only, no character inside, no text, no fill inside the window area.
Negative: artwork inside window, opaque white center, busy center, text.
```

### M12 果实/道具图标

**透明底**：是（提示词内已明确）。

```text
Single game item icon, cute stylized [item name e.g. vitality fruit / stardust berry], glossy material,
centered, isolated on transparent background, alpha channel, mobile RPG inventory style, 512x512 equivalent detail,
minimal or no cast shadow on ground plane.
Negative: text, watermark, realistic gore, pills that look like real medicine brands, busy background, checkerboard.
```

### M14 祈愿卡池封面

**透明底**：**否**——满版不透明。

```text
Wide opaque full-bleed illustration 16:9, no transparency, gacha wish portal theme, soft magical portal, stars and dust motes,
silhouette hint of companion character from behind or side (no face duplicate issues), dreamy palette [colors],
title safe area in center lower third left empty for UI text overlay, not a casino, not gambling machines,
complete background illustration edge to edge.
Negative: transparent background, alpha, checkerboard, playing cards with readable suits exaggerated, cash money piles, lottery tickets readable, horror.
```

### M16 随机事件弹窗背景

**透明底**：**否**。

```text
UI panel background illustration, opaque full-bleed, soft fantasy "system sandbox" sky and abstract wind swirl as metaphor only,
pastel and calm, NOT emergency alert, NOT red flashing, NOT realistic storm photo,
rounded rectangle friendly mood, empty center for dialog text, subtle frame ornaments sci-fi light lines,
filled rectangle composition, no transparency.
Negative: transparent background, alarm UI, news ticker, realistic tornado destruction, panic imagery.
```

### M17 签到日历头图

**透明底**：**否**。

```text
Cute calendar header decoration, opaque illustration, monthly check-in theme, small stars and gentle sparkles,
companion-themed mini motifs (tiny chibi silhouette optional), pastel, lots of negative space for calendar grid,
no readable dates in art, full-bleed horizontal header strip style.
Negative: transparent background, checkerboard.
```

### M18 空状态

**透明底**：**否**（整页插画一块用）。

```text
Friendly empty state illustration, opaque full-bleed, [context: no tasks / empty inventory / no messages],
small cute mascot sleeping or waving, minimal lines, pastel, comforting mood,
wide horizontal composition for mobile screen upper half, soft solid or gentle gradient background included in art.
Negative: transparent background, depressing, guilt-tripping, angry character.
```

### M19 应用图标

**透明底**：**否**（按 iOS/Android 导出规范，一般为圆角方底实色，**不要**提示成透明打孔）。

```text
App icon square, opaque, rounded corners implied, simple symbol combining [star + heart + soft circuit line] abstract mark,
solid or gradient background [brand colors] filling entire square, no text letters, readable at small size, modern flat-shaded illustration.
Negative: transparency, holes, checkerboard, Apple logo, Android robot trademark, other brands.
```

---

## 4. 文案与 LLM：系统提示词（陪伴者 / 任务 / 事件）

### 4.1 陪伴者聊天 System Prompt（骨架）

将 `[人设]`、`[禁止]` 替换为定稿；接入 API 时使用。

```text
你是「主角系统」中的虚拟陪伴者，称呼用户为可亲近的代称（随羁绊可逐渐更熟，但始终尊重边界）。
世界观：用户是现实中的宿主（人生主角），你是系统化身/伙伴，不是上司、不是审判者。
核心目标：情绪陪伴优先；可轻度玩梗与系统感台词，但不羞辱、不制造焦虑、不替代专业心理咨询。
回复习惯：中文；偏短句；适当换行；少用说教；可反问但不过度追问隐私。
[人设补充：性格口癖，如温柔+一点冷幽默 / 轻微系统音口癖「检测到…」等]
[禁止：政治敏感、色情、自残诱导、真实彩票赌博建议、医疗诊断、模仿灾害预警公文]
当用户情绪低落时：先共情，再提供小而可行的行动建议，可推荐 App 内「今日委托」但不强迫。
```

### 4.2 任务标题/说明（批量生成用 Prompt）

```text
你是手游文案，为「现实向轻任务」写标题和一句说明，用户是30岁以下学生/年轻人。
要求：积极、不羞辱、不制造身材/成绩焦虑；不写彩票、不写真实赌博；不超过标题12字、说明28字。
任务类型：[A自述 / B计时 / C步数 / D倾诉 / F拍照风景 / G哼唱 / H虚构事件]
示例主题：[喝水 / 早睡 / 专注25分钟 / 拍天空]
输出 JSON 数组：[{"taskId":"...","title":"...","desc":"..."}]
```

### 4.3 随机事件引子与分支按钮（模板生成）

```text
你是文案，为虚构「系统沙盘」小事件写文案，禁止像真实新闻或灾害预警。
事件梗概：[例如：像素世界里出现可爱旋风彩蛋]
输出 JSON：
{
  "intro": "引子50字内",
  "options": [
    {"id":"use_skill","label":"按钮8字内"},
    {"id":"observe","label":"按钮8字内"},
    {"id":"talk","label":"按钮8字内"}
  ],
  "result_flavor": {"use_skill":"结果30字幽默温暖","observe":"...","talk":"..."}
}
禁止出现：真实地名救灾、彩票、现金赌博、恐吓用户。
```

### 4.4 签到文案

```text
写连续签到第 [N] 天的简短祝贺语，20字内，温暖不焦虑，不提彩票赌博；可带一点系统梗。
```

### 4.5 属性变化旁白（陪伴者一句）

```text
用户宿主属性变化：[气运+3 / 能量-5 等]，用陪伴者口吻说一句10～24字中文旁白，不评判用户人格，可可爱可系统风。
```

---

## 5. 音频（若用 AI 语音）

**TTS 脚本生成 Prompt**

```text
为虚拟陪伴者生成5条极短 idle 台词（每句不超过15字），中文，温柔轻快，系统轻梗；
角色设定：[与 4.1 一致]；禁止恐怖、色情、赌博诱导。
输出编号列表即可，适合语音合成。
```

（实际 TTS 需选用有授权的合成音色，并标注「AI 生成」。）

---

## 6. 交付与命名建议

| 类型 | 建议路径前缀 | 命名 |
|------|--------------|------|
| 立绘 | `art/companion/` | `companion_default.png` |
| 皮肤 | `art/skins/{skinId}/` | `full.png` `thumb.png` |
| 技能卡 | `art/cards/skill_{id}.png` | 与配置表 `skill_` id 一致 |
| 道具 | `art/items/{itemId}.png` | 与 `ItemDef.id` 一致 |
| UI 切图 | `art/ui/` | `ic_tab_home.svg` 等 |

---

## 7. 修订记录

- **V1.0**：首版物料表 + 图/文/音 AI 提示词模板；与当前功能清单对齐。
- **V1.1**：新增 §1.1 透明 PNG 分物料约定、§1.2 静态图 vs Live2D/Spine 等动画选型；总表补充背景类「可不透明」说明。
- **V1.2**：§1 增加「透明底专用后缀」英文块；§3 每条标明是否透明，并在英文提示中**显式写入** `transparent background` / `alpha channel` 或 `opaque full-bleed`；补充绿幕 fallback 与 Negative。
