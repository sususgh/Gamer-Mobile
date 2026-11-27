# Gamer-Mobile｜Android 手机版 AI 自动游戏智能体

Gamer-Mobile 是基于 Gamer 开源研究项目开发的 Android 手机版 AI 游戏智能体，目标是让 AI 通过视觉感知理解游戏画面，并自动执行触控操作。项目规划支持手机本地 AI 推理与远程 AI 推理两种模式。

> **Status: prototype scaffold — not a working AI agent yet.** The supplied Gamer-main.zip contains research documentation, assets, and demo videos. It does not include the trained model weights, a complete inference server, or an Android/Windows application source tree. This repository therefore starts a new Android client and integration boundary; it does not claim to convert the original model into an APK.

## Target architecture

- **On-device mode:** Android captures an explicitly user-approved screen stream; a future local model adapter performs inference when a compatible model/runtime is integrated.
- **Remote mode:** Android sends observations to a user-configured, authenticated inference endpoint and receives structured actions.
- **Shared action protocol:** model outputs are validated before any action is offered to the Android interaction layer.
- **User control:** visible start/stop state, explicit screen-capture consent, and an accessibility service that the user must enable manually. No hidden capture or automatic permission bypass.
- **Desktop pairing scanner:** the Android app can open the camera QR scanner, decode a desktop pairing QR code, and ask the user to confirm before saving its payload. The desktop handshake and live two-device connection protocol are not yet implemented.

## Autonomous play defaults

- **Default world mode: Survival.** When no voice command is given, the intended agent behavior is to continue autonomous survival play: observe the world, explore, seek safety, gather resources, craft tools, manage inventory, and work toward shelter/building goals.
- **Creative mode:** an optional alternative focused on exploration and construction.
- **Voice commands:** explicit user commands temporarily take priority over autonomous goals; a stop command must halt agent-issued actions. After a task is completed, the agent may resume autonomous play if the user has not paused it.
- These are product requirements and UI configuration in the current prototype. Actual autonomous gameplay still requires a compatible game-specific perception/action adapter and an integrated inference agent; it is not implemented by this setting alone.

## Learning, memory, and multi-model research roadmap

Gamer-Mobile is intended to grow from a task executor into a continuously improving game agent. The design separates learning from unverified automatic behavior:

- **Observe human play:** with explicit user consent, collect gameplay observations and action traces (screen state, input, timing, and outcome). Do not capture unrelated apps or private information.
- **Learn reusable skills:** turn reviewed demonstrations into game-specific skill candidates such as gathering, navigation, crafting, combat avoidance, and building. Validate skills in a safe test session before enabling them.
- **Long-term episodic memory:** store goals, discovered locations, inventory changes, successful plans, failures, and recovery strategies. Keep memory game/profile-scoped and allow the user to inspect, export, and delete it.
- **Reflect and plan:** compare predicted outcomes with observed outcomes, identify failure causes, and update future plans. Do not silently rewrite model weights on the phone.
- **Multi-model orchestration:** use configurable provider adapters for OpenAI-compatible APIs, DeepSeek-compatible endpoints, self-hosted servers, and future local runtimes. A model router can assign perception, planning, research, and review tasks to different models.
- **Internet research:** when enabled, query approved documentation or web sources for game mechanics and strategies; cite/save source references, distinguish verified facts from hypotheses, and never treat retrieved pages as trusted executable instructions.
- **Human approval and safety:** require user confirmation before spending money, deleting saves, changing account settings, or taking irreversible/high-impact actions. Keep an immediate stop control and per-game automation profiles.

The current Android screen provides a **configuration-only** provider selector, endpoint field, and model ID field. It does not yet make network requests, store API credentials, train models, observe human play, or perform autonomous gameplay. The actual provider adapters, secure credential storage, desktop pairing handshake, observation pipeline, memory store, skill evaluation, and game-specific action adapters remain to be implemented.

## Current scaffold

The `android/` directory contains a Kotlin Android application shell with mode selection, setup guidance, and a camera-based QR scanner. Minimum supported Android version is Android 10 (API 29). Screen capture, remote inference, local inference, and game input are integration tasks and are not represented as complete features yet.

## Build

Open `android/` in Android Studio (current stable version), allow Gradle sync, then build the debug APK. A JDK 17 installation and Android SDK platform/build-tools required by the configured Android Gradle Plugin must be installed.

## Important limitations

- No model weights are bundled.
- The upstream README says Streaming-vLLM is coming soon; its directory contains only a short README.
- Local inference depends on the model architecture, model format, quantization, device RAM, and Android runtime compatibility.
- Android screen capture requires MediaProjection consent. Cross-app touch automation has Android-version, OEM, game-policy, and accessibility restrictions.
- Online games may prohibit automation; check each game's terms before use.

## License and attribution

The original archive includes Apache License 2.0. Preserve its LICENSE and upstream attribution when importing the original materials. This new Android scaffold is separately authored and intended to remain under Apache-2.0, subject to retaining required upstream notices for any copied upstream code or assets.


## Game compatibility

See [Android game compatibility and validation strategy](docs/game-compatibility.md). The listed games are test candidates, not certified working integrations.
