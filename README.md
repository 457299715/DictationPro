# LocalDictationPro

<div align="center">

📚 **English Word Dictation App for Android**

[中文](#中文文档) | [English](#english-documentation)

---

## 中文文档

### 📖 项目简介

LocalDictationPro 是一款基于 Android 的英语单词听写应用，采用 Kotlin 语言和 Jetpack Compose 声明式 UI 框架开发。应用支持本地词库导入、TTS 语音听写、在线词库商店、资源管理与个性化设置等功能，旨在为英语学习者提供便捷高效的移动听写训练工具。

### ✨ 核心功能

| 功能模块 | 说明 |
|---------|------|
| 🎧 **听写训练** | 支持连续/单次播放模式、乱序播放、中英随机朗读、语速调节、播放间隔设置 |
| 📥 **词库导入** | 支持 TXT 格式词库导入，自动识别键值对(word:meaning)和纯英文格式，支持单元标记解析 |
| ☁️ **在线词库** | 内置在线词库商店，支持分类浏览、一键下载、自动导入、版本检测 |
| 📁 **资源管理** | 管理已导入的书籍和词典，支持删除操作，级联删除关联数据 |
| 🔍 **单词选择** | 支持按单元、按单词搜索、按序号范围三种方式选择听写范围 |
| ⚙️ **个性化设置** | 主题切换(浅色/深色)、语速调节、字体大小、导出路径自定义、TTS引擎配置 |
| 💾 **导出记录** | 支持将听写记录导出为 TXT 文件，包含时间戳和语言标记 |

### 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material Design 3)
- **架构模式**: MVVM
- **依赖注入**: Hilt
- **本地数据库**: Room (SQLite)
- **网络请求**: Retrofit + OkHttp
- **语音引擎**: Android TextToSpeech (TTS)
- **构建工具**: Gradle

### 📂 项目结构

```
com.example.localdictationpro/
├── data/                          # 数据层
│   ├── entities/                  # 数据库实体
│   │   ├── Book.kt                # 书籍实体
│   │   ├── Word.kt                # 单词实体
│   │   ├── Dictionary.kt          # 词典实体
│   │   ├── DictionaryEntry.kt     # 词典词条实体
│   │   └── Settings.kt            # 设置实体
│   ├── AppDatabase.kt             # Room数据库配置
│   ├── BookDao.kt                 # 书籍数据访问
│   ├── WordDao.kt                 # 单词数据访问
│   ├── DictionaryDao.kt           # 词典数据访问
│   └── SettingsDao.kt             # 设置数据访问
├── di/                            # 依赖注入模块
│   ├── NetworkModule.kt           # 网络模块
│   └── TTSModule.kt               # TTS模块
├── network/                       # 网络层
│   ├── WordBankApi.kt             # API接口定义
│   ├── FakeWordBankApi.kt         # 模拟API实现
│   └── models/                    # 网络数据模型
│       ├── CategoryResponse.kt
│       ├── WordBankItem.kt
│       └── ...
├── tts/                           # TTS语音引擎
│   └── TTSManager.kt              # TTS管理器封装
├── ui/                            # UI层
│   ├── screens/                   # 页面
│   │   ├── DictationScreen.kt     # 听写主界面
│   │   ├── BookSelectionScreen.kt # 书籍选择
│   │   ├── WordSelectionScreen.kt # 单词范围选择
│   │   ├── ResourceManagerScreen.kt # 资源管理
│   │   ├── OnlineStoreScreen.kt   # 在线词库
│   │   └── SettingsScreen.kt      # 设置
│   ├── components/                # 可复用组件
│   │   ├── CommonComponents.kt    # 通用组件
│   │   └── WordDisplayCard.kt     # 单词显示卡片
│   └── theme/                     # 主题
│       ├── Theme.kt               # 主题配置
│       ├── Color.kt               # 颜色定义
│       └── Type.kt                # 字体定义
├── utils/                         # 工具类
│   ├── FileImporter.kt            # 文件导入工具
│   └── ExportHelper.kt            # 导出工具
├── viewmodel/                     # ViewModel层
│   ├── DictationViewModel.kt      # 听写ViewModel
│   ├── BookViewModel.kt           # 书籍ViewModel
│   ├── SettingsViewModel.kt       # 设置ViewModel
│   ├── OnlineStoreViewModel.kt    # 在线商店ViewModel
│   └── WordSelectionViewModel.kt  # 单词选择ViewModel
├── MainActivity.kt                # 主Activity
└── LocalDictationApplication.kt   # Application类
```

### 🚀 快速开始

#### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 26+ (Android 8.0)
- Gradle 8.0+

#### 构建步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/4572999715/LocalDictationPro.git
   cd LocalDictationPro
   ```

2. **打开项目**
   使用 Android Studio 打开项目根目录，等待 Gradle 同步完成。

3. **配置 TTS 引擎（可选）**
   应用支持系统默认 TTS 引擎。推荐使用 Google TTS 以获得最佳英文朗读效果：
   - 进入应用 **设置 → TTS引擎** 页面
   - 按引导安装 Google TTS
   - 下载英文语音数据包

4. **运行应用**
   连接 Android 设备或启动模拟器，点击 ▶️ Run。

#### 导入词库

1. 准备 TXT 格式词库文件，支持两种格式：
   - **键值对格式**: 每行 `word:meaning` 或 `word：meaning`
   - **纯英文格式**: 每行仅单词（需配合词典使用）
   - **单元标记**: 使用 `[Unit 1]` 或 `# Unit 1` 标记单元

2. 在应用中点击 **📖 书籍选择 → 📁 导入** 或 **📁 资源管理 → 导入**

3. 选择 TXT 文件，系统自动解析并导入数据库

### 📋 数据库设计

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `books` | 书籍表 | id, name, format, wordCount, remoteId, remoteVersion |
| `words` | 单词表 | id, bookId(FK), word, meaning, unit, position |
| `dictionaries` | 词典表 | id, name, filePath, entryCount |
| `dictionary_entries` | 词条表 | id, dictionaryId(FK), word, meaning |
| `settings` | 设置表 | id(固定1), speechRate, theme, currentBookId, ... |

### 🔌 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/wordbanks/categories` | GET | 获取词库分类 |
| `/api/v1/wordbanks/list` | GET | 分页获取词库列表 |
| `/api/v1/wordbanks/{id}` | GET | 获取词库详情 |
| `/api/v1/wordbanks/{id}/check-update` | GET | 检查版本更新 |
| `/api/v1/wordbanks/{id}/download` | POST | 上报下载统计 |

> 开发阶段使用 `FakeWordBankApi` 提供模拟数据，生产环境可切换为真实 Retrofit 接口。

### 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。本项目仅作为学习用途，禁止任何商用行为！

---

## English Documentation

### 📖 Project Overview

LocalDictationPro is an Android English word dictation application developed with Kotlin and Jetpack Compose declarative UI framework. It supports local word bank import, TTS voice dictation, online word bank store, resource management, and personalized settings, aiming to provide English learners with a convenient and efficient mobile dictation training tool.

### ✨ Core Features

| Module | Description |
|--------|-------------|
| 🎧 **Dictation Training** | Continuous/single play mode, shuffle, random EN/CN reading, speech rate control, interval settings |
| 📥 **Word Bank Import** | Import TXT format word banks, auto-detect key-value (word:meaning) and English-only formats, support unit tag parsing |
| ☁️ **Online Store** | Built-in online word bank store with category browsing, one-click download, auto-import, version checking |
| 📁 **Resource Manager** | Manage imported books and dictionaries with delete support and cascade deletion |
| 🔍 **Word Selection** | Select dictation scope by unit, by word search, or by serial number range |
| ⚙️ **Personalized Settings** | Theme switch (Light/Dark), speech rate, font size, export path customization, TTS engine config |
| 💾 **Export Records** | Export dictation records to TXT files with timestamps and language tags |

### 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM
- **DI**: Hilt
- **Local Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Speech Engine**: Android TextToSpeech (TTS)
- **Build Tool**: Gradle

### 📂 Project Structure

```
com.example.localdictationpro/
├── data/                          # Data Layer
│   ├── entities/                  # Database Entities
│   │   ├── Book.kt                # Book entity
│   │   ├── Word.kt                # Word entity
│   │   ├── Dictionary.kt          # Dictionary entity
│   │   ├── DictionaryEntry.kt     # Dictionary entry entity
│   │   └── Settings.kt            # Settings entity
│   ├── AppDatabase.kt             # Room database configuration
│   ├── BookDao.kt                 # Book data access
│   ├── WordDao.kt                 # Word data access
│   ├── DictionaryDao.kt           # Dictionary data access
│   └── SettingsDao.kt             # Settings data access
├── di/                            # DI Modules
│   ├── NetworkModule.kt           # Network module
│   └── TTSModule.kt               # TTS module
├── network/                       # Network Layer
│   ├── WordBankApi.kt             # API interface
│   ├── FakeWordBankApi.kt         # Mock API implementation
│   └── models/                    # Network data models
│       ├── CategoryResponse.kt
│       ├── WordBankItem.kt
│       └── ...
├── tts/                           # TTS Engine
│   └── TTSManager.kt              # TTS manager wrapper
├── ui/                            # UI Layer
│   ├── screens/                   # Screens
│   │   ├── DictationScreen.kt     # Main dictation screen
│   │   ├── BookSelectionScreen.kt # Book selection
│   │   ├── WordSelectionScreen.kt # Word scope selection
│   │   ├── ResourceManagerScreen.kt # Resource management
│   │   ├── OnlineStoreScreen.kt   # Online word bank store
│   │   └── SettingsScreen.kt      # Settings
│   ├── components/                # Reusable components
│   │   ├── CommonComponents.kt    # Common components
│   │   └── WordDisplayCard.kt     # Word display card
│   └── theme/                     # Theme
│       ├── Theme.kt               # Theme configuration
│       ├── Color.kt               # Color definitions
│       └── Type.kt                # Typography
├── utils/                         # Utilities
│   ├── FileImporter.kt            # File import utility
│   └── ExportHelper.kt            # Export utility
├── viewmodel/                     # ViewModel Layer
│   ├── DictationViewModel.kt      # Dictation ViewModel
│   ├── BookViewModel.kt           # Book ViewModel
│   ├── SettingsViewModel.kt       # Settings ViewModel
│   ├── OnlineStoreViewModel.kt    # Online store ViewModel
│   └── WordSelectionViewModel.kt  # Word selection ViewModel
├── MainActivity.kt                # Main Activity
└── LocalDictationApplication.kt   # Application class
```

### 🚀 Quick Start

#### Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 26+ (Android 8.0)
- Gradle 8.0+

#### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/LocalDictationPro.git
   cd LocalDictationPro
   ```

2. **Open the project**
   Open the project root directory in Android Studio and wait for Gradle sync.

3. **Configure TTS engine (optional)**
   The app supports the system default TTS engine. Google TTS is recommended for best English pronunciation:
   - Go to **Settings → TTS Engine** in the app
   - Follow the guide to install Google TTS
   - Download English voice data

4. **Run the app**
   Connect an Android device or start an emulator, then click ▶️ Run.

#### Import Word Bank

1. Prepare a TXT format word bank file. Two formats are supported:
   - **Key-value format**: Each line `word:meaning` or `word：meaning`
   - **English-only format**: Each line contains only the word (requires dictionary)
   - **Unit tags**: Use `[Unit 1]` or `# Unit 1` to mark units

2. In the app, tap **📖 Book Selection → 📁 Import** or **📁 Resource Manager → Import**

3. Select the TXT file, and the system will automatically parse and import it into the database

### 📋 Database Schema

| Table | Description | Key Fields |
|-------|-------------|------------|
| `books` | Books table | id, name, format, wordCount, remoteId, remoteVersion |
| `words` | Words table | id, bookId(FK), word, meaning, unit, position |
| `dictionaries` | Dictionaries table | id, name, filePath, entryCount |
| `dictionary_entries` | Dictionary entries table | id, dictionaryId(FK), word, meaning |
| `settings` | Settings table | id(fixed 1), speechRate, theme, currentBookId, ... |

### 🔌 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/wordbanks/categories` | GET | Get word bank categories |
| `/api/v1/wordbanks/list` | GET | Get paginated word bank list |
| `/api/v1/wordbanks/{id}` | GET | Get word bank details |
| `/api/v1/wordbanks/{id}/check-update` | GET | Check for version update |
| `/api/v1/wordbanks/{id}/download` | POST | Report download statistics |

> During development, `FakeWordBankApi` provides mock data. Switch to real Retrofit interface for production.

### 🤝 Contributing

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 📄 License

This project is licensed under the [MIT License](LICENSE). This project is for educational purposes only. Any commercial use is strictly prohibited!
