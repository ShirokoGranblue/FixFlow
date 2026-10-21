# Docker 构建使用本地 Maven 代理

Docker Desktop、终端中的 BuildKit 客户端令牌请求，以及构建容器中的 Maven
下载请求需要分别确认代理配置。仅设置 Docker Desktop 的代理，不能保证终端
发起的 `auth.docker.io` 请求也走代理。
根目录 Dockerfile 支持可选的 `maven_settings` 构建 secret，代理配置只在 Maven
打包步骤中挂载，不复制进镜像。没有提供 secret 时，Maven 使用默认设置。

在项目根目录创建 `docker-compose.override.yml`：

```yaml
services:
  backend:
    build:
      secrets:
        - maven_settings

secrets:
  maven_settings:
    file: ./docker/maven/settings.local.xml
```

创建 `docker/maven/settings.local.xml`，将端口替换为本机 HTTP 代理的实际端口：

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
    <proxies>
        <proxy>
            <id>local-build-proxy</id>
            <active>true</active>
            <protocol>http</protocol>
            <host>host.docker.internal</host>
            <port>10808</port>
            <nonProxyHosts>localhost|127.*</nonProxyHosts>
        </proxy>
    </proxies>
</settings>
```

此示例用于 Docker Desktop，`host.docker.internal` 指向宿主机；容器内的
`127.0.0.1` 指向容器自身。其他环境需要使用该环境中实际可达的代理地址。
以上两个本地文件均已加入 `.gitignore`。保持本机代理运行，然后在项目根目录执行：

```powershell
$env:HTTP_PROXY = 'http://127.0.0.1:10808'
$env:HTTPS_PROXY = 'http://127.0.0.1:10808'
docker compose config --quiet
docker compose up -d --build
```

这两个环境变量作用于当前 PowerShell 窗口及其子进程；新开终端后需重新设置。
这里的端口也应替换为本机代理的实际端口。不要把这两行放入 Compose 的服务
`environment` 中：服务环境变量只在容器运行时生效，无法修复构建前的令牌请求。

Compose 会自动加载本地覆盖文件。仅使用基础配置、不加载本地代理覆盖时：

```powershell
docker compose -f docker-compose.yml up -d --build
```

Maven 仓库使用 BuildKit 缓存，重复构建可以复用已下载的依赖。
更改 secret 内容不会自动使构建缓存失效；需要验证新的代理设置时执行：

```powershell
docker compose build --no-cache backend
```

Docker 打包使用 `-Dmaven.test.skip=true`，跳过测试编译和执行，以允许工作区中
未完成的测试存在。因此镜像构建成功不代表测试通过；测试应在独立流程中运行。

若错误发生在 `FROM` 或 `load metadata` 阶段，需检查 Docker Desktop 的镜像
拉取代理和当前终端的代理变量；这里的 Maven settings 只作用于 `RUN mvn` 阶段。
