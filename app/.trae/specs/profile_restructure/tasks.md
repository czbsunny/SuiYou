# 个人中心页重构 - 实现计划

## [x] Task 1: 移除家庭成员管理卡片
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 删除 family-card 相关模板代码
  - 删除相关的样式代码
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement`: 家庭成员管理卡片已移除

## [x] Task 2: 将功能菜单改为独立卡片布局
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 将 menu-card 内的菜单项改为独立卡片
  - 每个菜单项有自己的卡片容器
  - 保持图标、文字、右箭头样式
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement`: 每个菜单项显示为独立卡片
  - `human-judgement`: 卡片间距适当

## [x] Task 3: 调整样式和间距
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 调整菜单项卡片的样式
  - 确保视觉一致性
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement`: 页面整体视觉效果良好