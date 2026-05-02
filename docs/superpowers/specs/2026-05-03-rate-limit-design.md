# 接口限流机制设计

## 目标

为 Spring Boot 后端增加一个轻量接口限流机制，用于保护登录、注册、检测、聊天等接口，避免短时间内被重复调用导致资源浪费或服务压力过大。

## 方案

采用 `@RateLimit` 注解声明接口限流规则，使用 Spring MVC `HandlerInterceptor` 在 Controller 方法执行前完成限流判断。Redis 保存固定窗口内的访问计数。

这种方案保留了“注解声明”的直观性，同时把执行逻辑放在更适合 HTTP 接口处理的 MVC 拦截器中。

## 注解设计

新增方法级注解：

```java
@RateLimit(limit = 10, window = 60)
```

默认值为一分钟 10 次，因此常见用法可以简写为：

```java
@RateLimit
```

字段含义：

- `limit`：窗口内允许的最大请求次数，默认 `10`。
- `window`：固定窗口长度，默认 `60`。
- `timeUnit`：窗口时间单位，默认 `TimeUnit.SECONDS`。
- `message`：超限提示，默认 `请求过于频繁，请稍后再试`。

## 限流粒度

限流 key 自动选择身份维度：

1. 如果请求属性中存在 `userId`，按用户 ID 限流。
2. 如果不存在 `userId`，按客户端 IP 限流。

因此：

- 登录、注册等公开接口按 IP 限流。
- 检测、聊天等已登录接口按用户 ID 限流。

Redis key 格式：

```text
rate_limit:{controllerClass}.{method}:{user:{userId}|ip:{clientIp}}
```

## 固定窗口算法

每次请求进入被 `@RateLimit` 标注的 Controller 方法前：

1. 计算 Redis key。
2. 对 key 执行递增计数。
3. 如果递增后的值为 `1`，设置过期时间为窗口长度。
4. 如果计数大于 `limit`，拒绝请求。
5. 如果计数小于等于 `limit`，继续执行 Controller 方法。

## 超限响应

超限时不进入 Controller 方法，直接返回：

- HTTP 状态码：`429 Too Many Requests`
- 响应体：项目统一 `Result.fail(...)` JSON 格式
- 默认提示：`请求过于频繁，请稍后再试`

## 接入范围

首批建议加在：

- `/user/login`
- `/user/register`
- 图片检测接口
- AI 聊天发送/流式回复接口

这些接口要么容易被刷，要么调用成本较高。

## 测试策略

新增 MVC 拦截器相关测试，覆盖：

1. 未标注 `@RateLimit` 的接口不受影响。
2. 同一 IP 一分钟内前 10 次放行，第 11 次返回 429。
3. 请求存在 `userId` 时按用户 ID 计数。
4. 不同用户或不同 IP 的计数互不影响。

## 非目标

本次不实现滑动窗口、令牌桶、按接口动态配置中心、分布式 Lua 原子脚本或网关层限流。固定窗口 Redis 计数已经满足当前项目需要。
