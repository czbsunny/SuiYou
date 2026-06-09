# 机构资产模块从后端获取 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 修改 loadModules 函数调用真实 API
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `loadModules` 函数，从调用 `loadMockModules()` 改为调用 `getInstitutionModules(instCode)`
  - 处理 API 返回的数据，将其转换为与现有代码兼容的格式
  - 设置 `institutionModules.value` 并调用 `initDefaultModules()`
- **Acceptance Criteria Addressed**: [AC-1, AC-2]
- **Test Requirements**:
  - `programmatic` TR-1.1: 验证调用了 `getInstitutionModules` API，未调用 `loadMockModules`
  - `programmatic` TR-1.2: 验证 API 返回的数据正确转换并赋值给 `institutionModules`
  - `programmatic` TR-1.3: 验证 required、defaultList、optional 模块的 `selectionType` 属性分别设置为 'REQUIRED'、'DEFAULT_SELECTED'、'OPTIONAL'
- **Notes**: 注意 API 返回的 `defaultList` 需要转换为 selectionType='DEFAULT_SELECTED'

## [x] Task 2: 添加 API 错误处理和加载状态
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 try/catch 包裹 API 调用
  - 在 API 失败时显示错误提示（uni.showToast）
  - 错误发生时可考虑回退到 mock 数据或提示用户
- **Acceptance Criteria Addressed**: [AC-3, AC-4]
- **Test Requirements**:
  - `programmatic` TR-2.1: 验证 API 失败时显示错误提示
  - `human-judgement` TR-2.2: 验证用户体验不受错误影响

## [x] Task 3: 验证模块选择功能正常工作
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 测试模块选择/取消功能是否正常
  - 验证 `getModuleChecked` 和 `toggleModule` 函数能正常工作
  - 验证必选模块不能取消
- **Acceptance Criteria Addressed**: [AC-5]
- **Test Requirements**:
  - `programmatic` TR-3.1: 验证 `initDefaultModules` 正确初始化默认选中状态
  - `programmatic` TR-3.2: 验证 `getModuleChecked` 和 `toggleModule` 能正常工作
  - `human-judgement` TR-3.3: 验证模块点击交互符合预期
