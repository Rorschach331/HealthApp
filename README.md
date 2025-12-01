# 健康助手

一个简洁美观的血压记录 Web 应用,支持数据录入、历史查看和趋势分析。

## 功能特性

- ✅ 血压数据录入(收缩压/舒张压/心率)
- ✅ 历史记录查看(带颜色状态标识)
- ✅ 趋势图表分析
- ✅ 后端数据持久化(SQLite 小型数据库)
- ✅ 响应式设计,支持深色模式
- ✅ **单端口部署** - 前后端集成在一个进程中

## 技术栈

**前端**

- React 19 + Vite
- Chart.js (图表)
- Lucide React (图标)
- Vanilla CSS

**后端**

- .NET 8 Minimal API
- SQLite 小型数据库 (`Microsoft.Data.Sqlite`)

## 快速开始

### 生产环境(推荐)

```bash
npm start
```

这个命令会:

1. 自动构建前端 (`npm run build`)
2. 复制构建产物到 `.NET` 项目 `wwwroot/`
3. 启动 .NET 8 后端服务器 (单端口同时提供前端与 API)

访问: **http://localhost:3000**

### 开发环境

**方式一:前后端分离开发**

```bash
# 终端 1 - 后端
npm run server

# 终端 2 - 前端(带热更新)
npm run dev
```

- 前端: http://localhost:5173 (Vite 开发服务器)
- 后端: http://localhost:3000

**方式二:仅启动后端(需先构建前端)**

```bash
npm run build    # 构建前端
npm run server   # 启动服务器
```

## 部署说明

### 单端口架构优势

- ✅ **简化部署**: 只需一个进程,一个端口
- ✅ **无需 CORS**: 前后端同源,避免跨域问题
- ✅ **易于管理**: 统一的日志和监控
- ✅ **资源节省**: 减少端口占用和进程开销

### 部署步骤

1. 克隆项目到服务器
2. 安装依赖: `npm install`
3. 启动应用: `npm start`
4. 访问 `http://服务器IP:3000`

### 环境变量

```bash
PORT=3000              # 可选,默认 3000
DATABASE_PATH=./data/health.db  # 可选,默认使用 data/health.db
```

### 配置文件

位置：`server-dotnet/Health.Api/appsettings.json`（默认）与 `appsettings.Production.json`（生产环境覆盖）。

可配置项：
- `Server:Address` 指定监听地址（默认 `0.0.0.0`）
- `Server:Port` 指定端口（默认 `3000`）
- `Database:Path` 指定数据库文件路径（默认 `data/health.db`）

优先级：环境变量 > `appsettings.Production.json` > `appsettings.json`。

### 单文件发布 (Armbian aarch64)

在开发环境生成 ARM64 单文件(框架依赖):

```bash
npm run publish:arm64
```

生成的产物位于:

```
server-dotnet/Health.Api/bin/Release/net8.0/linux-arm64/publish/
  ├── Health.Api            # 单文件可执行 (linux-arm64, 依赖服务器 .NET 8 运行时)
  └── wwwroot/              # 静态资源(同时已嵌入到单文件，亦保留侧载)
```

部署到服务器(服务器已安装 .NET 8 运行时):

- 上传 `Health.Api` 至服务器目录 (例如 `/opt/health/`)
- 创建数据目录: `mkdir -p /opt/health/data`
- 运行:

```bash
cd /opt/health
PORT=3000 DATABASE_PATH=/opt/health/data/health.db ./Health.Api
```

说明:

- 如果未侧载 `wwwroot/`, 单文件会使用嵌入的静态资源提供前端
- 若系统缺少 SQLite 原生库, 安装: `sudo apt install -y libsqlite3-dev`

## 数据存储

使用 SQLite 小型数据库进行持久化,默认数据库文件位于 `data/health.db`。

.NET 后端使用 `Microsoft.Data.Sqlite` 访问数据库,首次运行会自动初始化数据表。

示例数据表结构(仅用于说明):

```sql
CREATE TABLE IF NOT EXISTS records (
  id INTEGER PRIMARY KEY,
  date TEXT NOT NULL,
  systolic INTEGER NOT NULL,
  diastolic INTEGER NOT NULL,
  pulse INTEGER
);
```

## 项目结构

```
Health/
├── src/              # 前端源码
│   ├── App.jsx       # 主应用组件
│   └── index.css     # 全局样式
├── server/           # 旧版 Node 后端(可忽略)
│   ├── index.js          # 已移除(不再使用)
│   └── db.js             # 已移除(不再使用)
├── server-dotnet/    # .NET 后端源码
│   └── Health.Api/
│       ├── Program.cs
│       ├── Health.Api.csproj
│       └── wwwroot/      # 前端构建产物(由 npm start 自动复制)
├── server-dotnet/    # .NET 后端源码
│   └── Health.Api/
│       ├── Program.cs
│       └── Health.Api.csproj
├── dist/             # 前端构建产物(由 vite build 生成)
├── data/             # 数据存储目录
│   └── health.db     # SQLite 数据库文件
└── package.json      # 项目配置
```

## API 接口

| 方法   | 路径             | 说明         |
| ------ | ---------------- | ------------ |
| GET    | /api/records     | 获取所有记录 |
| POST   | /api/records     | 新增记录     |
| DELETE | /api/records/:id | 删除记录     |

## 作为 PWA 使用

在手机浏览器中打开应用,点击"添加到主屏幕",即可像原生 App 一样使用。

## 常见问题

**Q: 为什么访问 3000 端口就能看到前端页面?**  
A: .NET 后端配置了静态文件服务,会自动提供 `dist/` 目录中的前端构建文件。

**Q: 如何修改前端代码?**  
A: 修改 `src/` 目录下的文件后,运行 `npm run build` 重新构建,然后重启服务器。

**Q: 开发时如何实现热更新?**  
A: 使用 `npm run dev` 启动 Vite 开发服务器(端口 5173),它会自动热更新。
