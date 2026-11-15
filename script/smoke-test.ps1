param(
    [string]$BaseUrl = 'http://127.0.0.1:8081'
)

$ErrorActionPreference = 'Stop'

$baseUrl = $BaseUrl.TrimEnd('/')

# 检查前端
$frontend = Invoke-WebRequest `
    -Uri "$baseUrl/" `
    -TimeoutSec 10

if ($frontend.StatusCode -ne 200) {
    throw '前端首页检查失败'
}

# 检查 Nginx → Spring Boot → MySQL
$health = Invoke-RestMethod `
    -Uri "$baseUrl/healthz" `
    -TimeoutSec 10

if ($health.status -ne 'UP') {
    throw '后端健康检查失败'
}

# 检查 Sa-Token 登录拦截器
# 未携带 Token 访问 /api/user/me，应当返回 401
$authResponse = Invoke-WebRequest `
    -Uri "$baseUrl/api/user/me" `
    -SkipHttpErrorCheck `
    -TimeoutSec 10

if ($authResponse.StatusCode -ne 401) {
    throw "登录拦截器检查失败，状态码：$($authResponse.StatusCode)"
}

Write-Host '冒烟测试全部通过'