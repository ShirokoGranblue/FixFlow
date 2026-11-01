# Windows + Docker Desktop 发布与恢复手册

## 环境和发布约束

- GitHub Actions 的 backend/frontend job 必须先通过，只有 `main` push 才执行本地部署。
- 自托管 Runner 标签为 `self-hosted`, `Windows`, `X64`, `fixflow-local`。
- GitHub `vars.LOCAL_COMPOSE_PROJECT` 与 `vars.LOCAL_ENV_FILE` 必须配置，并在 Runner 主机保持私有。
- `IMAGE_TAG` 默认 `latest`，CI 自动使用 `github.sha` 将前后端镜像绑定到同一版本。
- 发布自动保留运行中的应用镜像并在**新版本健康检查失败时**尝试回滚；首次部署无旧镜像不能回滚。
- **注意：应用镜像回滚不等于数据库迁移回滚。** 每次修改 Flyway 结构时，应提前确认新旧版本共同兼容，必要时使用 expand/contract 迁移策略。

## 常用命令（PowerShell）

```powershell
$env:IMAGE_TAG = 'git提交的完整SHA'
docker compose --env-file .env -f docker-compose.yml config --quiet
docker compose --env-file .env -f docker-compose.yml ps
docker compose --env-file .env -f docker-compose.yml logs --tail 100 backend frontend
```

本地 `docker compose up -d --build` 默认构建 `latest`，不会自动复制 CI 的 SHA 版本；如果已有 CI 管理的服务，请优先通过 CI 发布。回滚时指定旧版本 `IMAGE_TAG`，并使用 `--no-build`，避免从新源码覆盖旧镜像。

## 备份数据库（操作之前先备份）

在 PowerShell 7.4+ 中执行（支持原生 stdout 原始字节重定向）：

```powershell
New-Item -ItemType Directory -Force .\\backups | Out-Null
$backup = ".\\backups\\fixflow-$(Get-Date -Format 'yyyyMMdd-HHmmss').sql"
docker compose --env-file .env -f docker-compose.yml exec -T mysql sh -c 'exec mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --single-transaction --quick --routines --triggers --databases "$MYSQL_DATABASE"' > $backup
if ($LASTEXITCODE -ne 0 -or (Get-Item $backup).Length -eq 0) { throw '备份失败' }
Get-FileHash $backup -Algorithm SHA256
```

将备份存入受控目录，不上传到公开 Git 仓库。该备份含用户信息，必须按敏感数据管理。仅使用 `mysql-data` Docker volume 并不等于备份。

## 恢复演练（仅在测试环境）

1. 建立隔离的 MySQL 测试容器（不能复用正式库）；先确认 `SELECT DATABASE()`。
2. `docker cp .\\backups\\fixflow-XXXX.sql <测试容器名>:/tmp/restore.sql`。
3. `docker exec <测试容器名> sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < /tmp/restore.sql'`，验证表结构、记录数和关键工单状态。
4. 删除临时文件，并记录备份耗时、恢复耗时、校验结果和数据库迁移版本。不要在 CI 发布过程中自动执行生产数据库恢复。

## 故障排查

| 故障 | 第一步 |
|---|---|
| 后端 readiness 失败 | `docker compose logs --tail 200 backend`；检查 Flyway 与数据库连接 |
| 前端静态页面正常但 API 失败 | 查看 Nginx `/api` 反向代理和后端健康状态 |
| 新版容器失败 | 检查 workflow rollback 输出，确认旧镜像 tag 是否存在 |
| Flyway checksum 错误 | 不修改已上线 V1–V7；新增 V8+ 迁移；先核对数据库现状 |
| MySQL volume 损坏 | 停止写入，保留日志并从独立备份演练恢复 |

## 安全提醒

Runner 和 Docker socket 相当于部署权限；不要让不可信 PR 直接执行自托管部署。环境文件禁止提交，数据库和后端不要对公网暴露。镜像和备份应有过期清理策略，但清理前确认仍存在可回退版本。
