# 资产页曲线图修复 - 实现计划

## [x] Task 1: 简化曲线图结构
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 移除现有的 chart-area 渐变区域
  - 移除 line 斜线
  - 保留基础 chart 容器和 point 圆点
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `human-judgement`: 渐变区域和斜线已移除

## [x] Task 2: 使用SVG重绘折线
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 使用 SVG 元素重绘折线图
  - 使用 polyline 或 path 创建平滑折线
  - 调整圆点位置与折线对齐
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `human-judgement`: 折线平滑且清晰
  - `human-judgement`: 圆点在折线上正确显示