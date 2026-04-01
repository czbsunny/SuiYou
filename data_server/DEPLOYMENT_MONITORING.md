# 部署和监控方案

## 1. 部署架构

### 1.1 系统架构

```
┌───────────────────┐
│   基金数据服务    │
│  (FastAPI + Uvicorn) │
├───────────────────┤
│                   │
┌─────────┐  ┌─────────┐  ┌─────────┐
│  MySQL  │  │  Redis  │  │  行情API │
│  数据库  │  │  缓存  │  │  (可选)  │
└─────────┘  └─────────┘  └─────────┘
```

### 1.2 部署方式

#### 1.2.1 本地开发环境

1. **安装依赖**
   ```bash
   pip install -r requirements.txt
   ```

2. **运行服务**
   ```bash
   python main.py
   ```

3. **启动定时任务**
   - 服务启动时会自动启动定时任务调度器
   - 定时任务会每5分钟执行一次基金估值更新

#### 1.2.2 生产环境部署

1. **使用 Supervisor 管理进程**

   - 安装 Supervisor
     ```bash
     pip install supervisor
     ```

   - 创建配置文件 `supervisor.conf`
     ```ini
     [program:fund-data-server]
     command=uvicorn main:app --host 0.0.0.0 --port 7575 --reload
     directory=/path/to/data_server
     user=root
     autostart=true
     autorestart=true
     stderr_logfile=/path/to/logs/fund-data-server.err.log
     stdout_logfile=/path/to/logs/fund-data-server.out.log
     ```

   - 启动 Supervisor
     ```bash
     supervisord -c supervisor.conf
     ```

2. **使用 Docker 部署**

   - 创建 `Dockerfile`
     ```dockerfile
     FROM python:3.9-slim

     WORKDIR /app

     COPY requirements.txt .
     RUN pip install -r requirements.txt

     COPY . .

     EXPOSE 7575

     CMD ["python", "main.py"]
     ```

   - 构建镜像
     ```bash
     docker build -t fund-data-server .
     ```

   - 运行容器
     ```bash
     docker run -d --name fund-data-server -p 7575:7575 fund-data-server
     ```

## 2. 环境配置

### 2.1 数据库配置

修改 `database/init_db.py` 中的数据库连接配置：

```python
# MySQL数据库连接配置
DATABASE_URL = "mysql+pymysql://username:password@host:port/database?charset=utf8mb4"
```

### 2.2 缓存配置

修改 `services/storage_service.py` 中的缓存配置：

```python
# 缓存过期时间（秒）
self.cache_expiry = 300  # 5分钟
```

### 2.3 定时任务配置

修改 `schedulers/valuation_scheduler.py` 中的定时任务配置：

```python
# 每5分钟执行一次
schedule.every(5).minutes.do(update_fund_valuations)
```

## 3. 监控方案

### 3.1 日志监控

1. **配置日志**
   - 系统默认使用 `logging` 模块记录日志
   - 日志级别设置为 `INFO`
   - 日志格式包含时间、模块名、级别和消息

2. **日志文件**
   - 生产环境中建议将日志写入文件
   - 配置日志轮转，避免日志文件过大

### 3.2 健康检查

1. **添加健康检查端点**
   - 在 `main.py` 中添加健康检查路由

   ```python
   @app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "fund-data-server"}
   ```

2. **使用监控工具**
   - 可以使用 Prometheus + Grafana 监控服务状态
   - 配置告警规则，当服务异常时发送通知

### 3.3 性能监控

1. **监控指标**
   - 请求响应时间
   - CPU 和内存使用率
   - 数据库连接数
   - 缓存命中率

2. **监控工具**
   - 可以使用 `psutil` 库监控系统资源
   - 可以使用 `Prometheus` 收集监控指标

## 4. 故障处理

### 4.1 常见故障

| 故障类型 | 可能原因 | 解决方案 |
|---------|---------|--------|
| 数据库连接失败 | 数据库服务未启动或连接配置错误 | 检查数据库服务状态和连接配置 |
| 行情数据获取失败 | 行情API不可用或网络问题 | 检查网络连接，实现行情数据获取的重试机制 |
| 定时任务执行失败 | 任务代码错误或系统资源不足 | 检查任务日志，优化任务代码，确保系统资源充足 |
| 服务响应缓慢 | 请求量过大或代码性能问题 | 优化代码，增加缓存，考虑使用负载均衡 |

### 4.2 应急措施

1. **数据库备份**
   - 定期备份数据库，确保数据安全

2. **服务降级**
   - 当系统负载过高时，实现服务降级策略
   - 优先保证核心功能的正常运行

3. **灾备方案**
   - 配置主从数据库，确保数据冗余
   - 考虑多机部署，提高系统可用性

## 5. 系统维护

### 5.1 定期维护

1. **数据清理**
   - 定期清理过期的缓存数据
   - 定期清理日志文件

2. **代码更新**
   - 制定代码更新流程，确保更新不会影响系统运行
   - 测试环境验证通过后再部署到生产环境

### 5.2 版本管理

1. **代码版本控制**
   - 使用 Git 进行代码版本控制
   - 制定分支管理策略

2. **部署版本管理**
   - 记录每次部署的版本信息
   - 实现版本回滚机制

## 6. 安全性

### 6.1 安全措施

1. **数据库安全**
   - 使用参数化查询，防止 SQL 注入
   - 限制数据库用户权限

2. **API 安全**
   - 实现 API 访问控制
   - 考虑使用 API 密钥或 JWT 认证

3. **系统安全**
   - 定期更新系统和依赖包
   - 配置防火墙，限制访问端口

### 6.2 安全审计

1. **日志审计**
   - 记录所有关键操作的日志
   - 定期审计日志，发现异常操作

2. **漏洞扫描**
   - 定期进行漏洞扫描，发现并修复安全漏洞

## 7. 部署 checklist

- [ ] 安装依赖包
- [ ] 配置数据库连接
- [ ] 配置缓存
- [ ] 配置定时任务
- [ ] 启动服务
- [ ] 验证服务状态
- [ ] 配置监控
- [ ] 配置日志
- [ ] 进行压力测试
- [ ] 制定应急方案

## 8. 监控 checklist

- [ ] 配置日志监控
- [ ] 添加健康检查端点
- [ ] 配置性能监控
- [ ] 配置告警规则
- [ ] 测试监控系统
- [ ] 制定监控响应流程

## 9. 总结

本部署和监控方案提供了基金估值系统的完整部署指南和监控策略，确保系统的稳定运行和可靠维护。通过合理的部署架构、完善的监控机制和有效的故障处理策略，可以大大提高系统的可用性和可靠性，为用户提供更好的服务体验。
