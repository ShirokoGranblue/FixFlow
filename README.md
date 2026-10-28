# FixFlow — 轻量级工单管理系统

FixFlow 是一个基于 Spring Boot 和 Vue 3 开发的前后端分离工单管理系统，面向问题反馈、工单分配、处理跟踪与结果确认等业务场景。

系统采用角色权限控制，支持用户提交问题、工作人员处理工单和管理员进行统一管理，并记录工单操作历史及系统审计日志。

## 技术栈

**后端**
- Java 21、Spring Boot 4
- Spring MVC、MyBatis-Plus
- MySQL 8
- Sa-Token
- Spring AOP、Spring Transaction
- Flyway

**前端**
- Vue 3、TypeScript、Vite
- Vue Router、Pinia、Axios
- Element Plus

**部署**
- Docker、Docker Compose
- Nginx

## 核心功能

### 用户与权限

- 用户注册、登录和退出
- Token 身份认证
- USER、STAFF、ADMIN 角色权限控制
- 管理员维护用户角色

### 工单管理

- 创建和查询工单
- 工作人员接单、管理员分配
- 处理完成、确认关闭、重新打开
- 工单评论与操作历史
- 多条件筛选与分页查询

### 管理与审计

- 工单管理后台
- 用户管理后台
- Dashboard 统计
- 使用 Spring AOP 记录重要操作审计日志

## 工单处理流程

`OPEN → IN_PROGRESS → RESOLVED → CLOSED`

支持符合权限和状态规则的取消、重新打开等操作。

## 系统架构

- Nginx 负责 Vue 静态资源和 API 反向代理
- Spring Boot 提供 REST API
- Sa-Token 负责身份认证与角色校验
- Service 层实现工单状态流转和业务规则
- MyBatis-Plus 负责数据库访问
- MySQL 存储业务数据
- Docker Compose 编排服务

## 本地部署

**环境要求**

- Docker Desktop
- Docker Compose

**启动步骤**

1. 克隆仓库并进入项目目录。
2. 复制 `.env.example` 为 `.env`。
3. 设置数据库管理员密码和业务用户密码。
4. 执行 `docker compose up -d --build`。
5. 执行 `docker compose ps` 检查容器状态。
6. 浏览器访问 `http://127.0.0.1:8081`。

**常用命令**

- 查看日志：`docker compose logs -f backend`
- 停止服务：`docker compose down`
- 重新构建：`docker compose up -d --build`

注意：不要将 `.env` 文件提交到 Git，也不要随意执行 `docker compose down -v`，该命令可能删除数据库数据卷。

## 目录说明

- `backend/`：Spring Boot 后端
- `frontend/`：Vue 3 前端
- `compose.yaml`：服务编排
- `deploy/`：部署相关配置
- `.env.example`：环境变量示例

## 开发说明

本项目采用前后端分离架构，开发环境通过 Vite 代理访问 Spring Boot，容器化部署后由 Nginx 统一提供访问入口。

### 审计日志来源 IP

当前部署链路为浏览器 → Nginx → 后端。Nginx 使用连接来源地址覆盖 `X-Forwarded-For`，后端通过 Tomcat 原生转发处理，使审计日志中的 `request.getRemoteAddr()` 返回用户来源地址。Vite 开发代理同样覆盖此请求头。

本地配置仅信任回环代理；Docker 配置信任私网代理，因此后端必须保持仅在 Compose 内网可访问，不应发布 8080 端口到公网。若增加 CDN 或其他前置代理，需要重新配置可信代理链。本机访问仍可能记录 `::1` 或 `127.0.0.1`；历史日志不会被改写。

更新后执行 `docker compose up -d --build --no-deps frontend backend` 重建这两个服务。本地开发需要重启后端和 Vite。

来源地址回归测试：`mvn '-Dtest=ClientIpTests,UntrustedClientIpTests' test`。

工单状态变化通过后端业务规则校验，前端角色菜单仅负责用户体验，不作为真正的权限安全边界。
