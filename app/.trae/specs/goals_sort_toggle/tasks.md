# 目标页排序功能 - 实现计划

## [x] Task 1: 添加排序选项显示/隐藏状态
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 script setup 中添加 isSortOpen 状态变量
  - 添加 toggleSort 函数控制显示/隐藏
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3
- **Test Requirements**:
  - `programmatic`: isSortOpen 初始值为 false
  - `programmatic`: toggleSort 函数正确切换状态

## [x] Task 2: 添加条件渲染控制chips显示
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 使用 v-if 或 v-show 控制排序选项的显示
  - 绑定点击事件到排序按钮
  - 绑定点击事件到排序选项
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4
- **Test Requirements**:
  - `human-judgement`: 点击排序按钮时选项显示/隐藏正常
  - `human-judgement`: 点击选项后选项隐藏并执行排序

## [x] Task 3: 实现排序逻辑
- **Priority**: P1
- **Depends On**: Task 2
- **Description**: 
  - 实现按目标金额、截止时间、完成度的排序逻辑
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgement`: 不同排序选项产生正确的排序结果