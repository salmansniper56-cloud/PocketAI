# PocketPal AI - Android (Kotlin + Jetpack Compose)

PocketPal AI is a private, on-device AI assistant built with modern Android native architecture, Kotlin, Jetpack Compose, Material 3, and Room Database.

## Features Ported

- **🧠 Chat & Local Intelligence**: Private conversation threads with reasoning blocks, tool/talent triggers (Calculator, Datetime, HTML renderer), and Gemini API integration fallback.
- **🎭 Pals & PalsHub Marketplace**: Create custom Assistant and Roleplay personas with personalized system prompts, roles, avatars, locations, and installed marketplace items.
- **📥 Model Manager & Hugging Face**: Browse and manage GGUF models, search models on Hugging Face Hub, and configure access tokens.
- **🗣️ Text-to-Speech (TTS)**: Speech synthesis options with auto-speak toggles and Android system TTS integration.
- **📊 Device Benchmarking**: Test on-device generation speed (tokens/sec), Time to First Token (TTFT), and RAM memory usage with historical records.
- **⚙️ Settings & Customization**: Dark/Light mode, app language selection, Hugging Face access token management, and feedback submission.

## Tech Stack

- **UI Framework**: Jetpack Compose with Material Design 3
- **Language**: Kotlin 2.0
- **Database**: Room Database with KSP
- **Architecture**: MVVM with Coroutines and Flow
- **Build System**: Gradle (Kotlin DSL)
