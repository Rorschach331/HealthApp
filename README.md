# 健康助手 (Health Assistant)

一个简洁美观的血压记录与管理系统，包含 Web 端和 Android 客户端，支持数据录入、历史查看、趋势分析及多用户管理。全部使用 `Google Antigravity` 完成开发。

## ✨ 功能特性

### 📱 Android 客户端

- **现代化设计**：基于 Jetpack Compose 和 Material Design 3 构建，界面美观、交互流畅。
- **便捷录入**：
  - 支持收缩压、舒张压、心率及备注录入。
  - **滚轮式时间选择器**：采用 iOS 风格的滚轮选择，操作直观。
  - 录入完成后自动跳转至历史记录。
- **强大的历史记录**：
  - **无限滚动**：支持分页加载，滑动到底部自动加载更多。
  - **下拉刷新**：随时获取最新数据。
  - **高级筛选**：支持按用户、日期范围（快捷选择近 7 天/30 天/90 天）筛选记录。
  - **智能统计**：实时显示筛选范围内的血压均值。
  - **趋势对比**：每条记录显示与上次记录及均值的对比（上升/下降/持平）。
- **可视化趋势图**：
  - 自定义绘制的折线图，直观展示血压变化趋势。
  - 支持与历史记录同步的筛选条件。
- **灵活配置**：
  - 支持在设置页面动态修改后端 API 地址，即时生效，无需重启应用。

### 🌐 Web 端

- **响应式布局**：适配桌面及移动端浏览器。
- **核心功能**：完整的数据录入、列表查看及图表分析功能。
- **极简风格**：使用原生 HTML/CSS/JS 构建，轻量高效。

### 🖥️ 服务端

- **高性能 API**：基于 .NET 8 构建的 RESTful API。
- **数据存储**：使用 SQLite 数据库，轻量且易于迁移。
- **Swagger 文档**：内置 Swagger UI，方便 API 调试。

### 🔐 安全认证

为了保护数据安全，系统引入了简单的授权码机制：

1.  **服务端配置**：在 `appsettings.json` 中设置 `AuthSettings:Code`（默认为 `123456`）。
2.  **客户端授权**：首次使用 App 时，需在“设置”页面输入该授权码。
3.  **自动刷新**：App 会自动管理 Token，并在过期时尝试静默刷新，无需频繁输入。

## 🛠️ 技术栈

- **Android**: Kotlin, Jetpack Compose, Retrofit, Material3, Vico Charts
- **Server**: .NET 8, ASP.NET Core Web API, Entity Framework Core, SQLite
- **Web**: HTML5, CSS3, Vanilla JavaScript
- **DevOps**: GitHub Actions (自动构建 Android APK)

## 🚀 快速开始

### 服务端部署

1. 确保安装了 .NET 8 SDK。
2. 进入 `server-dotnet` 目录：
   ```bash
   cd server-dotnet
   ```
3. 运行服务：
   ```bash
   dotnet run --project Health.Api
   ```
   服务默认监听 `http://localhost:3000`。

### Android 客户端

1. 下载最新发布的 APK 文件（可在 GitHub Releases 中找到）。
2. 安装并打开应用。
3. 在“设置”页面输入服务端地址（例如 `http://192.168.1.100:3000`）。
4. 开始使用！

### Web 端

1. 直接在浏览器中打开 `index.html`。
2. 默认连接本地服务，如需修改 API 地址，请编辑 `script.js` 中的 `API_BASE_URL`。

## 📸 截图展示

_(此处可添加应用截图)_

## 📦 版本发布流程

本项目使用 GitHub Actions 实现自动化构建和发布。发布新版本的步骤如下：

1.  **修改版本号**：
    打开 `android-app/app/build.gradle.kts`，修改 `versionName`：

    ```kotlin
    defaultConfig {
        // ...
        versionName = "1.1.0" // 修改为你想要的版本号
    }
    ```

2.  **提交代码**：
    提交版本号修改：

    ```bash
    git add .
    git commit -m "chore: bump version to 1.1.0"
    git push
    ```

3.  **创建 Release Tag**：
    打上以 `v` 开头的 Tag 并推送到远程仓库：

    ```bash
    git tag v1.1.0
    git push origin v1.1.0
    ```

4.  **自动发布**：
    - GitHub Actions 会自动检测到 Tag 推送。
    - 自动构建 APK，文件名为 `health-v1.1.0.apk`。
    - 自动在 GitHub Releases 页面创建新版本，并附带 APK 下载链接。

## 📄 许可证

MIT License
