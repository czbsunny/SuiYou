
# Asset 模块迁移计划

## 1. 需求分析

根据用户需求，需要将 `old_server` 中的 Asset 模块迁移到 `server` 项目，并做以下调整：

| 需求点 | 描述 |
| :--- | :--- |
| 关联账户 | Asset 需要同时绑定 `account_id` 和 `account_module_id` |
| 策略模式 | 需要设计策略模板，负责 Asset 的创建和更新（包括基础信息更新及净值更新） |

## 2. 现有代码分析

### 2.1 旧 Asset 模型 (`old_server`)

```java
@Entity
@Table(name = "asset")
public class Asset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_module_id", nullable = false)
    private AccountModule accountModule;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String groupType;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String subCategory;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "total_balance", nullable = false)
    private BigDecimal totalBalance;
    
    @Column(name = "frozen_balance", nullable = false)
    private BigDecimal frozenBalance;
    
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;
    
    @Column(nullable = false)
    private String currency = "CNY";
    
    @Column(nullable = false)
    private Boolean includeInNetWorth = true;
    
    @Column(name = "valuation_mode", nullable = false)
    private String valuationMode = "MANUAL"; // MANUAL, CALCULATED
    
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(columnDefinition = "json")
    private String attributes;
    
    // ... timestamps
}
```

### 2.2 关联模型分析

| 模型 | 字段 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| Account | id | Long | 主键自增 |
| AccountModule | id | String(32) | UUID 格式 |
| AccountModule | accountId | String(32) | 关联 Account.id |

## 3. 迁移方案

### 3.1 调整后的 Asset 模型

```java
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;           // 新增：关联 Account
    
    @Column(name = "account_module_id", nullable = false, length = 32)
    private String accountModuleId;    // 修改：使用字符串 ID
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String groupType;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String subCategory;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "total_balance", nullable = false)
    private BigDecimal totalBalance;
    
    @Column(name = "frozen_balance", nullable = false)
    private BigDecimal frozenBalance;
    
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;
    
    @Column(nullable = false)
    private String currency = "CNY";
    
    @Column(nullable = false)
    private Boolean includeInNetWorth = true;
    
    @Column(name = "valuation_mode", nullable = false)
    private String valuationMode = "MANUAL";
    
    @Column(nullable = false)
    private Integer status = 1;
    
    @Column(columnDefinition = "json")
    private String attributes;
    
    // ... timestamps
}
```

### 3.2 策略模式设计

#### 3.2.1 策略接口

```java
public interface AssetStrategy {
    /**
     * 获取策略类型
     */
    String getStrategyType();
    
    /**
     * 创建 Asset
     */
    Asset createAsset(AssetCreateRequest request);
    
    /**
     * 更新基础信息
     */
    Asset updateBasicInfo(Asset asset, AssetUpdateRequest request);
    
    /**
     * 更新净值
     */
    Asset updateNetWorth(Asset asset, BigDecimal newTotalBalance);
}
```

#### 3.2.2 默认策略实现

```java
public class DefaultAssetStrategy implements AssetStrategy {
    @Override
    public String getStrategyType() {
        return "DEFAULT";
    }
    
    @Override
    public Asset createAsset(AssetCreateRequest request) {
        // 默认创建逻辑
    }
    
    @Override
    public Asset updateBasicInfo(Asset asset, AssetUpdateRequest request) {
        // 默认更新逻辑
    }
    
    @Override
    public Asset updateNetWorth(Asset asset, BigDecimal newTotalBalance) {
        // 默认净值更新逻辑
    }
}
```

#### 3.2.3 策略工厂

```java
@Component
public class AssetStrategyFactory {
    private final Map<String, AssetStrategy> strategyMap;
    
    @Autowired
    public AssetStrategyFactory(List<AssetStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(AssetStrategy::getStrategyType, Function.identity()));
    }
    
    public AssetStrategy getStrategy(String strategyType) {
        return strategyMap.getOrDefault(strategyType, strategyMap.get("DEFAULT"));
    }
}
```

## 4. 文件清单

| 文件路径 | 文件类型 | 说明 |
| :--- | :--- | :--- |
| `model/Asset.java` | 实体类 | 调整后的 Asset 模型 |
| `repository/AssetRepository.java` | 数据访问层 | Asset 的 CRUD 操作 |
| `strategy/AssetStrategy.java` | 策略接口 | 定义策略行为 |
| `strategy/impl/DefaultAssetStrategy.java` | 策略实现 | 默认策略实现 |
| `strategy/AssetStrategyFactory.java` | 策略工厂 | 策略获取器 |
| `service/AssetService.java` | 服务接口 | Asset 业务接口 |
| `service/impl/AssetServiceImpl.java` | 服务实现 | Asset 业务逻辑 |
| `dto/asset/AssetCreateRequest.java` | 请求 DTO | 创建请求 |
| `dto/asset/AssetUpdateRequest.java` | 请求 DTO | 更新请求 |
| `dto/asset/AssetResponse.java` | 响应 DTO | 返回响应 |

## 5. 实施步骤

| 步骤 | 任务 | 负责人 | 依赖 |
| :--- | :--- | :--- | :--- |
| 1 | 创建 Asset 实体类 | 开发 | 无 |
| 2 | 创建 AssetRepository | 开发 | 步骤 1 |
| 3 | 创建策略接口和默认实现 | 开发 | 步骤 1 |
| 4 | 创建策略工厂 | 开发 | 步骤 3 |
| 5 | 创建 DTO 类 | 开发 | 无 |
| 6 | 创建 Service 接口和实现 | 开发 | 步骤 2-5 |
| 7 | 测试验证 | 开发 | 步骤 1-6 |

## 6. 风险评估

| 风险点 | 风险等级 | 缓解措施 |
| :--- | :--- | :--- |
| AccountModule ID 类型差异 | 中 | 使用 String 类型存储，兼容 UUID 格式 |
| 策略模式复杂性 | 低 | 先实现默认策略，后续扩展 |
| 数据迁移 | 中 | 确认数据格式后再进行迁移 |
