
# Asset 金额精度升级 - 实施计划

## [x] Task 1: 修改 Asset.java 中金额字段精度
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 将 totalBalance 字段的 precision 从 18 改为 26，scale 从 2 改为 8
  - 将 frozenBalance 字段的 precision 从 18 改为 26，scale 从 2 改为 8
  - 将 availableBalance 字段的 precision 从 18 改为 26，scale 从 2 改为 8
  - 更新对应的 columnDefinition
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4
- **Test Requirements**:
  - `human-judgment` TR-1.1: 验证 totalBalance 字段定义为 decimal(26,8)
  - `human-judgment` TR-1.2: 验证 frozenBalance 字段定义为 decimal(26,8)
  - `human-judgment` TR-1.3: 验证 availableBalance 字段定义为 decimal(26,8)
  - `programmatic` TR-1.4: 验证 availableBalance = totalBalance - frozenBalance 的计算逻辑
- **Notes**: 需要同步更新 @PrePersist 和 @PreUpdate 中的计算逻辑以保持精度

## [x] Task 2: 验证编译通过
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 编译项目确保修改后代码可以正常编译
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-2.1: 项目编译成功
