# Gamer Mobile

Android client scaffold for a hybrid AI game agent, based on the Gamer research project's documented screen-observation/action loop.

> **Status: prototype scaffold — not a working AI agent yet.** The supplied Gamer-main.zip contains research documentation, assets, and demo videos. It does not include the trained model weights, a complete inference server, or an Android/Windows application source tree. This repository therefore starts a new Android client and integration boundary; it does not claim to convert the original model into an APK.

## Target architecture

- **On-device mode:** Android captures an explicitly user-approved screen stream; a future local model adapter performs inference when a compatible model/runtime is integrated.
- **Remote mode:** Android sends observations to a user-configured, authenticated inference endpoint and receives structured actions.
- **Shared action protocol:** model outputs are validated before any action is offered to the Android interaction layer.
- **User control:** visible start/stop state, explicit screen-capture consent, and an accessibility service that the user must enable manually. No hidden capture or automatic permission bypass.

## Current scaffold

The `android/` directory contains a minimal Kotlin Android application shell with mode selection and setup guidance. Screen capture, remote inference, local inference, and game input are integration tasks and are not represented as complete features yet.

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
