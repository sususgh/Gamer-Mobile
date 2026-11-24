# Android 游戏兼容性与适配策略

## 目标范围

Gamer-Mobile 面向 **Android 手机上的原生游戏**。它不是 PC 游戏控制器，也不是云游戏串流客户端。电脑端 DSH 负责模型推理；Android 端负责经用户授权采集本机游戏画面/音频、接收动作并执行。

## 兼容性原则

“支持某款游戏”必须以真实设备、真实游戏版本和真实操作测试为依据。仅能启动、识别包名或显示截图，不等于能够自动玩游戏。兼容性按以下阶段标记：

- **发现**：读取前台应用包名/用户手动选择游戏。
- **观察**：获得用户授权的游戏画面，识别分辨率、方向、HUD 与场景。
- **理解**：视觉模型能稳定识别该游戏中的角色、目标、地图和状态。
- **操作**：动作适配器能可靠执行点击、长按、滑动、摇杆及多点触控。
- **闭环验证**：执行后能通过新画面/游戏状态判断动作是否成功，并能处理失败。
- **验证通过**：在明确列出的设备、Android 版本和游戏版本上完成重复测试。

## 通用适配层

1. 游戏发现：包名、应用标签、屏幕方向、分辨率、刷新率（可用时）。
2. 观察采集：Android MediaProjection 用户授权；按需采集画面。音频采集必须遵守 Android 音频播放捕获限制及用户授权。
3. 输入执行：优先使用 Android AccessibilityService 的合法手势能力；设备/系统不支持时明确显示不可用，不尝试绕过系统权限。
4. 坐标归一化：将模型动作坐标映射到当前屏幕尺寸，处理安全区域、刘海、横竖屏和 UI 缩放。
5. 游戏配置：每个游戏独立保存方向、控件布局、动作映射、观察频率和安全边界。
6. 动作反馈：每个动作带唯一 ID、时限和预期结果；等待手机确认及下一帧，不盲目重复点击。

## 初始测试候选（不是已认证支持列表）

- 开放世界/动作：原神、崩坏：星穹铁道、鸣潮、Minecraft。
- 射击/生存：和平精英、PUBG Mobile、使命召唤手游、Free Fire。
- MOBA/策略：王者荣耀、英雄联盟手游、金铲铲之战、部落冲突。
- 赛车/休闲：狂野飙车、Roblox、模拟经营及其他触屏游戏。

以上仅为兼容性测试候选。不得在没有测试报告时标记为“已支持”。在线游戏可能限制自动化行为，使用者应遵守各游戏服务条款。

## 每款游戏的验收测试

- 冷启动与回到前台识别正确。
- 横屏/竖屏、不同分辨率下坐标映射正确。
- 游戏画面帧时间戳与传输延迟可见。
- 单击、长按、滑动、摇杆、多点触控分别验证。
- AI 超时、断网、模型切换时停止危险动作。
- Stop 按钮可立即停止 agent 下发的后续动作。
- 至少进行多轮重复测试并记录设备、Android、游戏版本和结果。

## 当前实现状态

当前项目仍是原型骨架。该文档定义开发和验收标准，不代表上述候选游戏已经实现自动操作。实际支持需要屏幕采集、传输、视觉推理、输入执行和逐游戏闭环测试全部完成。


## Companion autonomy and dual-screen presence

Paimon is a persistent in-game companion persona, not a button-triggered chatbot. The Android client and Windows DSH dashboard render the same companion identity and synchronize her activity state, current goal, short status message, mood/animation cue, and speech events. The two renderings are views of one session, not two independent agents.

### Two operating modes

- **Autonomous adventure is the default whenever the game session is active and no user task is pending.** The agent continuously observes the game, chooses a safe next goal, acts, checks the outcome, and replans. It should behave like a capable human player rather than remain idle waiting for prompts.
- **Player-task mode** starts when the user gives a voice or text task. The task is added to a prioritized queue; the agent acknowledges it, works toward it, and reports progress. A task that conflicts with the current autonomous goal temporarily takes priority.
- On verified completion, emit a completion event and a companion line such as “旅行者，我已经完成你吩咐的任务啦！”. Then return to autonomous adventure if no task remains.
- If a task cannot be completed, report the blocker honestly, preserve progress, and ask for guidance when needed. Never announce completion based only on a model assertion; verify the game state where possible.

### Survival and Creative world behavior

- Respect the selected game/world mode. In Survival, prioritize safety, food/health, resource gathering, tools, shelter, navigation and then exploration/building goals. Avoid knowingly lethal actions when a safer route exists.
- In Creative, prioritize exploration, construction plans, material/layout experimentation and user-defined creative goals; survival resource constraints may not apply.
- The agent must infer the available mechanics from the selected game profile. Do not assume every game has Minecraft-style crafting, hunger, or building.

### Dynamic Paimon presentation

- Both mobile and desktop avatars should animate continuously according to a shared finite state: idle/listening, thinking, exploring, moving, interacting, task-progress, success, warning, model-switching, paused and disconnected.
- Use animation cues and short status text tied to actual runtime events. For example, exploring animation only while an exploration action is active; thinking animation while inference is pending; success animation only after task verification.
- The desktop dashboard may show richer activity details and event history; the phone provides compact animations and optional spoken updates.
- Companion speech must be concise, interruptible, rate-limited and user-configurable. The Paimon persona is a presentation layer; it does not imply the model has human consciousness.

### Pause, safety and recovery

- A visible user stop/pause control overrides autonomous goals and queued tasks immediately.
- On loss of connection, loss of model capability, uncertain game state or unsafe action, stop issuing new game inputs and show a warning. Resume only after recovery checks and user/session policy allow it.
- Preserve the current goal and verified progress across model failover; the replacement model must receive a compact state summary before continuing.
- Never spend currency, make purchases, delete saves, change account/security settings or perform other irreversible actions without explicit user approval.

These are product requirements. The current app remains a prototype and does not yet implement the autonomous gameplay loop, synchronized animated avatar, or task verification.
