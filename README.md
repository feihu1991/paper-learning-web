# Paper Learning Assistant

一个基于 AI 的 ArXiv 论文阅读学习助手，帮助你高效地阅读和理解学术论文。

## 功能特性

- 🔍 **ArXiv 搜索与导入** - 直接从 ArXiv 搜索并导入论文
- 🤖 **AI 智能解析** - 使用 LLM 自动生成论文的结构化总结
- 📋 **学习路径生成** - AI 自动生成学习步骤和进度跟踪
- ✅ **进度管理** - 跟踪学习进度，记录完成状态
- ⚙️ **多 API 支持** - 支持 OpenAI、Claude、DeepSeek、通义千问等

## 技术栈

**后端**
- Spring Boot 3.2.3
- Spring Data JPA + MySQL
- RESTful API

**前端**
- React 18 + TypeScript
- Vite
- Ant Design 5

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+ (或使用 Docker)

### 1. 数据库配置

```sql
CREATE DATABASE paper_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 后端启动

```bash
cd backend
./mvnw spring-boot:run
```

或使用 Docker：

```bash
docker-compose up -d
```

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

### 4. LLM 配置

首次使用需要配置 LLM API：
1. 进入「LLM 配置」页面
2. 添加或选择预设配置
3. 激活配置

## 环境变量

**后端**
- `DB_PASSWORD` - 数据库密码
- `SERVER_PORT` - 服务端口（默认 8080）

**前端** (可选)
- `VITE_API_BASE` - API 基础路径（默认 /api）

## 项目结构

```
paper-learning-web/
├── backend/
│   ├── src/main/java/com/paperlearning/
│   │   ├── controller/    # REST 控制器
│   │   ├── service/       # 业务逻辑
│   │   ├── repository/    # 数据访问
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   └── config/        # 配置类
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/    # React 组件
│   │   ├── pages/         # 页面
│   │   ├── services/      # API 服务
│   │   └── types/         # TypeScript 类型
│   └── package.json
├── docker-compose.yml
└── README.md
```

## API 端点

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/papers | 获取所有论文 |
| GET | /api/papers/{id} | 获取论文详情 |
| POST | /api/papers | 保存论文 |
| DELETE | /api/papers/{id} | 删除论文 |
| GET | /api/papers/search | 搜索论文 |
| GET | /api/papers/arxiv/search | 搜索 ArXiv |
| POST | /api/papers/{id}/parse | AI 解析论文 |
| POST | /api/papers/{id}/generate-learning-path | 生成学习路径 |
| GET | /api/learning/papers/{id}/steps | 获取学习步骤 |
| POST | /api/learning/papers/{id}/steps/{stepId}/complete | 标记完成 |
| GET | /api/config/llm | 获取 LLM 配置 |
| POST | /api/config/llm | 创建 LLM 配置 |
| POST | /api/config/llm/{id}/activate | 激活配置 |

## License

MIT
