# 资产API优化 - 实现计划

## [x] Task 1: 简化CreateAssetDTO
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 移除accountDTO字段
  - 移除groupType字段（改为自动推断）
  - 移除category字段（改为自动推断）
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: DTO编译通过
  - `human-judgment` TR-1.2: 字段精简，仅保留必要字段
- **Notes**: 保留subCategory用于特定资产类型细分

## [x] Task 2: 更新AssetServiceImpl.createAsset方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 移除自动创建账户的逻辑
  - 根据moduleId获取AccountModule
  - 根据AssetType自动推断groupType和category
  - 添加完善的错误处理
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-2.1: 创建资产成功时自动关联到正确账户
  - `programmatic` TR-2.2: 使用无效moduleId返回错误
  - `programmatic` TR-2.3: 自动填充groupType和category
- **Notes**: 需要查询SysAssetCategory获取分类信息

## [ ] Task 3: 更新AssetRespDTO
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 确保包含完整的模块和账户信息
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-3.1: 返回包含moduleId、moduleName、assetType
- **Notes**: 已有相关字段，检查是否完整

## [ ] Task 4: 更新AssetController
- **Priority**: P1
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 确保Controller正确调用更新后的Service
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-4.1: POST /assets接口正常工作
- **Notes**: 检查Controller是否需要修改