# 基金数据服务

一个基于Python FastAPI的服务，专注于基金数据获取和处理功能

## 功能特性

- **基金数据服务**
  - 基金数据获取与处理
  - 基金净值数据管理
  - 基金信息查询API

## 快速开始

### 安装依赖

```bash
pip install -r requirements.txt
```

### 运行服务

```bash
python main.py
```

服务将在 http://localhost:7575 启动。

## API文档

启动服务后，可通过以下地址访问API文档：
- Swagger UI: http://localhost:7575/docs
- ReDoc: http://localhost:7575/redoc

## 环境变量

- 可以根据需要添加基金数据服务相关的环境变量