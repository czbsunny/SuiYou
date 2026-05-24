# 资产页时间线修复 - 实现计划

## [x] Task 1: 添加动态时间标签计算函数
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加 `getMonthLabel` 函数计算月份标签
  - 添加 `generateTimelineLabels` 函数生成5个时间节点
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic`: 时间标签格式正确 (YY.MM)
  - `programmatic`: 时间范围从12个月前到当前月

## [x] Task 2: 修改模板使用动态时间标签
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 使用 `v-for` 渲染时间标签
  - 绑定计算属性 `timelineLabels`
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement`: 时间标签正确显示