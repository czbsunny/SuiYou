# 机构选择页面增强 - 实施计划

## [x] Task 1: 拷贝机构 logo 图标
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 `src/static/inst` 目录
  - 从 web 端拷贝所有机构 logo 图标
- **Acceptance Criteria Addressed**: [AC-4]
- **Test Requirements**:
  - `programmatic` TR-1.1: 确认图标目录存在且文件数量匹配
  - `human-judgement` TR-1.2: 图标能正常显示在页面上

## [x] Task 2: 更新搜索机构页面添加字母索引
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加字母分组计算逻辑
  - 添加右侧索引条组件
  - 添加滚动监听和点击滚动功能
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3]
- **Test Requirements**:
  - `programmatic` TR-2.1: 页面编译无错误
  - `human-judgement` TR-2.2: 索引条显示正确，点击能滚动

## [x] Task 3: 更新 InstitutionCard 组件显示 logo
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 InstitutionCard 组件显示机构 logo
- **Acceptance Criteria Addressed**: [AC-4]
- **Test Requirements**:
  - `programmatic` TR-3.1: 组件编译无错误
  - `human-judgement` TR-3.2: logo 正确显示在卡片上