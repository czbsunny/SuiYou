# 机构类型重构 - 实施计划

## [x] Task 1: 更新 SysInstitutionService 接口，添加机构类型方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 `SysInstitutionService.java` 中添加 `getAllInstitutionTypes()` 和 `getInstitutionTypeByCode(String typeCode)` 方法
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgment` TR-1.1: 接口文件包含新增的两个方法签名

## [x] Task 2: 更新 SysInstitutionServiceImpl 实现机构类型逻辑
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在 `SysInstitutionServiceImpl.java` 中实现机构类型相关方法
  - 移除对 `SysInstitutionTypeRepository` 的依赖
  - 从枚举 `InstType` 获取机构类型数据
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `human-judgment` TR-2.1: 服务实现类不依赖 SysInstitutionTypeRepository
  - `human-judgment` TR-2.2: 机构类型方法从 InstType 枚举获取数据

## [x] Task 3: 更新 InstitutionController 使用单一服务
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 修改 `InstitutionController.java`，移除对 `SysInstitutionTypeService` 的依赖
  - 所有机构类型接口调用 `SysInstitutionService`
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `programmatic` TR-3.1: 项目编译通过

## [x] Task 4: 删除 SysInstitutionType 实体类
- **Priority**: P1
- **Depends On**: Task 3
- **Description**: 
  - 删除 `SysInstitutionType.java` 实体文件
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgment` TR-4.1: 文件不再存在于项目中

## [x] Task 5: 删除 SysInstitutionTypeRepository
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 删除 `SysInstitutionTypeRepository.java` 文件
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `human-judgment` TR-5.1: 文件不再存在于项目中

## [x] Task 6: 删除 SysInstitutionTypeServiceImpl
- **Priority**: P1
- **Depends On**: Task 5
- **Description**: 
  - 删除 `SysInstitutionTypeServiceImpl.java` 文件
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `human-judgment` TR-6.1: 文件不再存在于项目中

## [x] Task 7: 删除 SysInstitutionTypeService 接口
- **Priority**: P1
- **Depends On**: Task 6
- **Description**: 
  - 删除 `SysInstitutionTypeService.java` 接口文件
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `human-judgment` TR-7.1: 文件不再存在于项目中