# iOS App Skeleton

`iosApp/` 现在包含一套独立的 SwiftUI 原生界面原型，用于对齐现有 Android 端的主要功能面：

- 首页
- 签到
- 任务
- 事件
- 背包
- 商店
- 抽卡
- 我的

说明：

- 当前实现只新增 iOS 侧文件，不改 Android。
- 状态管理使用 iOS 本地 `ObservableObject` 示例数据，便于先把界面和交互跑通。
- 后续如需接入 `shared/` 的 KMP 业务层，可从 `AppModel` 开始替换数据源。

建议在 macOS 上新建 Xcode iOS App target 后，将 `iosApp/MainCharacterIOS/` 下源码加入工程。
