# 修复 Maven 编译错误 - 规范文档

## Overview
- **Summary**: 修复 AssetServiceImpl.java 中缺失的 import 语句
- **Purpose**: 解决 Spring Boot 项目编译失败的问题
- **Target Users**: 后端开发人员

## 问题分析

### 错误信息
```
[ERROR] 找不到符号:   类 SysInstitutionRepository
[ERROR] 找不到符号:   类 AccountService
```

### 问题根源
在 `AssetServiceImpl.java` 中，代码引用了 `SysInstitutionRepository` 和 `AccountService`，但是缺少相应的 import 语句。

## 修改内容

### 修改文件
- `E:/Codes/SuiYou/server/src/main/java/com/suiyou/service/impl/AssetServiceImpl.java`

### 添加的 import
```java
import com.suiyou.repository.SysInstitutionRepository;
import com.suiyou.service.AccountService;
```

## 验证结果
- ✅ `mvn clean compile` 执行成功
- ✅ 191个源文件编译无错误

## 建议后续
建议后续使用代码检查工具（如 IDEA 的自动导入功能）来避免此类问题。