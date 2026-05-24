# 资产页时间标签格式修复 - 实现计划

## [x] Task 1: 修改月份标签格式为中文
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `getMonthLabel` 函数返回中文月份格式（1月、2月等）
  - 保留月份对应的位置信息
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic`: 月份格式为中文（数字+月）
  - `human-judgement`: 标签清晰可读

## [x] Task 2: 添加标签位置信息
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 为每个标签添加 x 位置坐标
  - 确保与折线图上的圆点对齐
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `human-judgement`: 标签与圆点正确对齐

## [x] Task 3: 更新模板使用新格式
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改模板以支持带位置信息的标签
  - 使用动态样式绑定对齐标签
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `human-judgement`: 页面显示正确