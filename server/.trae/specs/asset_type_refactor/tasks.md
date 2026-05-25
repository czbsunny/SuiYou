# AssetType 枚举重构 - 实现计划

## [x] Task 1: 重构 AssetType 枚举
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 参考 sys_asset_category_init.json 重新设计枚举值
  - 储蓄卡改为 CURRENT（活期）
  - 活期理财使用 CURRENT_PLUS
  - 现金独立为 CASH
  - 添加所有资产类别和负债类型
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-1.1: 枚举编译通过
  - `programmatic` TR-1.2: 包含所有预期的枚举值（CURRENT, CURRENT_PLUS, CASH, TIME_DEPOSIT, FUND, STOCK, BANK_PRODUCT, GOLD, PORTFOLIO, PRIVATE_FUND, SAVING_INSURANCE, CRYPTO, REAL_ESTATE, VEHICLE, JEWELRY, HOUSING_FUND, MEDICAL_INSURANCE, PENSION_ACCOUNT, PRIVATE_PENSION, RECEIVABLE, MORTGAGE, CAR_LOAN, CREDIT_CARD, INTERNET_CREDIT, CONSUMER_LOAN, PAYABLE, OTHER）
  - `human-judgment` TR-1.3: 每个枚举值的中文描述准确清晰
- **Notes**: 需要注意与现有代码的兼容性