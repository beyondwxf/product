# 保险产品中心 V1.5 技术实施方案

## 1. 总体架构

### 1.1 技术栈

- 后端：Java 17、Spring Boot 3.x、Spring Validation、Spring Security、MyBatis/MyBatis-Plus。
- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router。
- 数据库：MySQL 8.x。
- 缓存：Redis 7.x。
- 文档：OpenAPI 3、Knife4j。
- 测试：JUnit 5、Mockito、Testcontainers、Vitest、Playwright。
- 监控：Prometheus、ELK、统一 traceId 日志。

### 1.2 模块划分

采用模块化单体优先，后续可按领域拆分微服务。

```text
product-center
├── product-domain
├── product-application
├── product-infrastructure
├── product-api
└── product-admin-ui
```

- `product-domain`：领域模型、值对象、枚举、状态机、领域服务、领域事件。
- `product-application`：用例编排、事务边界、DTO 转换、权限入口、发布校验、审核推进、快照生成。
- `product-infrastructure`：MyBatis Mapper、数据库实体、Redis、消息、外部服务网关、文件导入导出。
- `product-api`：Controller、OpenAPI 注解、请求响应模型、异常处理。
- `product-admin-ui`：Vue3 管理后台。

依赖方向：

- API 依赖 Application。
- Application 依赖 Domain，并通过接口依赖 Infrastructure。
- Infrastructure 实现 Domain/Application 定义的仓储和网关接口。
- Domain 不依赖 Spring、MyBatis、Redis、HTTP、数据库实体。

## 2. 后端领域设计

### 2.1 聚合与领域对象

| 聚合/对象 | 职责 |
| --- | --- |
| `InsuranceProduct` | 产品主档，维护产品编码、名称、类型、分红标识、当前状态。 |
| `ProductVersion` | 产品版本，维护版本号、生效时间、失效时间、版本状态、乐观锁。 |
| `RiskItem` | 险种，区分主险、附加险，维护保额类型、费率计算方式。 |
| `CoverageLiability` | 责任，维护责任代码、责任类型、保额规则、等待期、互斥依赖。 |
| `InsurancePlan` | 投保方案，维护期间、缴费期间、保额档位、合法组合。 |
| `RateTable` | 费率表，维护因子、明细、生命周期、导入批次。 |
| `FieldTemplate` | 投保要素模板，维护字段定义和产品级覆盖。 |
| `HealthQuestionnaire` | 健康告知问卷版本，维护题目、选项、跳转关系、异常标识。 |
| `RuleSet` | 前端联动规则集，维护 ECA 规则 JSON 和模拟结果。 |
| `DividendConfig` | 分红险配置，维护分红方式、红利假设、关联责任。 |
| `ApprovalInstance` | 审核实例，维护节点、任务、审批记录。 |
| `Supplier` | 供应商档案、合同、能力、产品关联。 |
| `PublishSnapshot` | 发布快照，维护不可变产品定义 JSON 和快照元数据。 |

### 2.2 领域服务

- `ProductVersionService`：版本递增、复制历史版本、旧版本失效策略。
- `ProductStateMachine`：产品状态流转校验。
- `RateValidationService`：费率因子组合、缺失值、重复值、异常值校验。
- `PublishValidationService`：发布完整性校验。
- `SnapshotAssembler`：组装发布快照。
- `ApprovalFlowService`：轻量审核流节点推进。
- `RuleSetValidationService`：ECA 结构校验和表达式白名单校验。
- `DividendValidationService`：分红险必填项、红利假设、关联责任校验。

## 3. 数据库设计

### 3.1 通用字段

所有业务表必须包含：

- `id BIGINT PRIMARY KEY`
- `created_at DATETIME NOT NULL`
- `created_by VARCHAR(64) NOT NULL`
- `updated_at DATETIME NOT NULL`
- `updated_by VARCHAR(64) NOT NULL`
- `deleted TINYINT NOT NULL DEFAULT 0`

需要并发编辑的表增加：

- `version INT NOT NULL DEFAULT 0`

### 3.2 核心表

| 表名 | 说明 | 关键索引 |
| --- | --- | --- |
| `product_info` | 产品主档 | `uk_product_code(product_code)` |
| `product_version` | 产品版本 | `uk_product_version(product_code, version_no)`、`idx_product_status(product_code, status, effective_at)` |
| `risk_item` | 险种 | `idx_version(version_id)`、`uk_risk_code(version_id, risk_code)` |
| `coverage_liability` | 责任 | `idx_risk(risk_id)`、`uk_liability_code(version_id, liability_code)` |
| `coverage_relation` | 责任互斥/依赖 | `idx_version(version_id)` |
| `insurance_plan` | 投保方案 | `idx_version(version_id)` |
| `plan_option_matrix` | 方案合法搭配矩阵 | `idx_plan(plan_id)` |
| `rate_factor` | 费率因子 | `uk_factor_code(factor_code)` |
| `rate_table` | 费率表头 | `idx_status(status)`、`idx_product_version(product_code, version_no)` |
| `rate_item` | 费率明细 | `idx_rate_match(rate_table_id, age, gender, occupation_level, insurance_period, payment_period)` |
| `rate_import_batch` | 费率导入批次 | `idx_rate_table(rate_table_id)` |
| `rate_import_error` | 费率导入错误 | `idx_batch(batch_id)` |
| `field_template` | 投保要素模板 | `uk_template_code(template_code, template_version)` |
| `field_definition` | 字段定义 | `idx_template(template_id)` |
| `product_field_override` | 产品字段覆盖 | `idx_version(version_id)` |
| `health_questionnaire` | 健康告知问卷 | `uk_questionnaire(code, version_no)` |
| `health_question` | 健康告知题目 | `idx_questionnaire(questionnaire_id)` |
| `health_question_option` | 题目选项 | `idx_question(question_id)` |
| `occupation_code` | 职业代码 | `idx_parent(parent_code)` |
| `occupation_rule` | 职业限制 | `idx_version(version_id)` |
| `rule_set` | 联动规则集 | `idx_version(version_id)` |
| `dividend_config` | 分红配置 | `uk_version(version_id)` |
| `dividend_assumption` | 红利演示假设 | `idx_config(config_id)` |
| `clause_document_ref` | 条款引用 | `idx_version(version_id)` |
| `product_combo` | 组合产品 | `uk_combo_code(combo_code)` |
| `product_combo_item` | 组合明细 | `idx_combo(combo_id)` |
| `approval_instance` | 审核实例 | `idx_object(object_type, object_id)` |
| `approval_task` | 审核任务 | `idx_assignee(status, assignee_id)` |
| `approval_record` | 审核记录 | `idx_instance(instance_id)` |
| `supplier_info` | 供应商档案 | `uk_supplier_code(supplier_code)` |
| `supplier_contract` | 供应商合同 | `idx_supplier(supplier_id)`、`idx_expire(expire_at)` |
| `supplier_product_relation` | 供应商产品关联 | `idx_product(product_id)` |
| `product_publish_snapshot` | 发布快照 | `uk_snapshot(product_code, version_no)`、`idx_current(product_code, status)` |
| `product_operation_log` | 操作审计日志 | `idx_operator(operator_id, operated_at)`、`idx_object(object_type, object_id)` |

### 3.3 JSON 字段使用

允许使用 JSON 存储低频扩展内容：

- 前端联动规则结构化定义。
- 发布快照完整产品定义。
- 字段扩展属性。
- 规则模拟输入和输出。

禁止把高频查询条件仅放在 JSON 中，例如产品编码、版本号、状态、渠道、年龄、职业等级、费率匹配维度。

## 4. 应用服务与用例

### 4.1 产品用例

- `CreateProductUseCase`：创建产品主档和初始草稿版本。
- `UpdateProductDraftUseCase`：编辑草稿版本。
- `CreateProductVersionUseCase`：从已发布或历史版本复制新草稿版本。
- `SubmitProductReviewUseCase`：执行提交前校验并创建审核实例。
- `ApproveProductUseCase`：推进审核节点。
- `RejectProductUseCase`：记录驳回原因并回到草稿。
- `PublishProductVersionUseCase`：执行发布校验、生成快照、切换版本状态、发送事件。
- `SuspendProductUseCase`：下架产品并清理当前缓存。

### 4.2 费率用例

- `CreateRateTableUseCase`：创建费率表头。
- `ImportRateItemsUseCase`：解析 Excel/CSV，批量校验并入库。
- `SubmitRateReviewUseCase`：提交精算审核。
- `PublishRateTableUseCase`：发布费率表。
- `CalculatePremiumUseCase`：根据产品版本、方案、因子匹配费率并计算保费。

### 4.3 规则与投保配置用例

- `SaveFieldTemplateUseCase`：维护投保要素模板。
- `BindFieldTemplateUseCase`：绑定产品版本字段模板和覆盖配置。
- `SaveQuestionnaireUseCase`：维护健康告知问卷版本。
- `SaveRuleSetUseCase`：保存联动规则并执行结构校验。
- `SimulateRuleSetUseCase`：基于输入数据模拟 ECA 规则执行结果。

### 4.4 分红用例

- `SaveDividendConfigUseCase`：维护分红方式和说明。
- `SaveDividendAssumptionUseCase`：维护低中高档红利演示假设。
- `ValidateDividendConfigUseCase`：校验分红险发布前必填项。

### 4.5 供应商用例

- `CreateSupplierUseCase`：维护供应商档案。
- `SaveSupplierContractUseCase`：维护合同和证照。
- `BindSupplierProductUseCase`：建立产品供应商关系。
- `CheckSupplierEligibilityUseCase`：产品发布时校验供应商状态和有效期。

## 5. 接口设计

### 5.1 管理端接口

统一前缀：`/admin/api/v1`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/products` | 创建产品 |
| `GET` | `/products` | 分页查询产品 |
| `GET` | `/products/{id}` | 查看产品详情 |
| `PUT` | `/products/{id}` | 编辑草稿产品 |
| `POST` | `/products/{id}/versions` | 创建新版本 |
| `GET` | `/product-versions/{id}` | 查看版本详情 |
| `POST` | `/product-versions/{id}/submit-review` | 提交审核 |
| `POST` | `/product-versions/{id}/validate` | 发布前校验 |
| `POST` | `/product-versions/{id}/publish` | 发布版本 |
| `POST` | `/products/{id}/suspend` | 下架产品 |
| `POST` | `/rate-tables/{id}/import` | 导入费率表 |
| `POST` | `/rate-tables/{id}/submit-review` | 费率提交精算审核 |
| `POST` | `/rate-tables/{id}/publish` | 发布费率表 |
| `POST` | `/rule-sets/{id}/simulate` | 联动规则模拟 |
| `GET` | `/approval-tasks/my` | 待我审核 |
| `POST` | `/approval-tasks/{id}/approve` | 审核通过 |
| `POST` | `/approval-tasks/{id}/reject` | 审核驳回 |
| `GET` | `/reports/product-lineage/{versionId}` | 产品血缘图 |

### 5.2 开放接口

统一前缀：`/open/api/v1`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/products/{productCode}` | 获取最新生效产品定义 |
| `GET` | `/products/{productCode}/versions/{versionNo}` | 获取指定版本产品定义 |
| `POST` | `/products/{productCode}/premium/calculate` | 费率试算 |
| `POST` | `/products/sync` | 批量同步产品定义 |
| `POST` | `/products/{productCode}/sales-rules/match` | 匹配销售规则 |

### 5.3 统一响应

```json
{
  "code": "SUCCESS",
  "message": "成功",
  "traceId": "trace-id",
  "data": {}
}
```

分页响应：

```json
{
  "code": "SUCCESS",
  "message": "成功",
  "traceId": "trace-id",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "total": 100,
    "records": []
  }
}
```

## 6. 发布与缓存设计

### 6.1 发布流程

1. 校验产品版本处于 `APPROVED` 状态。
2. 执行发布完整性校验。
3. 生成发布快照 JSON。
4. 在同一数据库事务内写入快照，切换新版本为 `ACTIVE`，失效旧版本。
5. 事务提交后发送产品发布事件。
6. 刷新 Redis 当前生效产品缓存。
7. 缓存刷新失败时记录告警并进入补偿队列。

### 6.2 发布校验项

- 产品基础信息完整。
- 产品编码和版本号唯一。
- 生效时间、失效时间合法。
- 至少一个主险、一个有效责任。
- 至少一个有效投保方案。
- 费率策略已发布且覆盖方案维度。
- 条款文件引用有效。
- 投保要素模板已绑定。
- 健康告知问卷版本有效。
- 职业限制和投保规则不冲突。
- 联动规则结构校验通过。
- 分红险必须有分红方式、红利演示参数、分红说明。
- 供应商关联有效且合同未过期。
- 同一产品不会出现多个 `ACTIVE` 版本。

### 6.3 Redis Key

- `product:center:current:{productCode}`：当前生效产品摘要。
- `product:center:detail:{productCode}:v:{versionNo}`：指定版本产品详情。
- `product:center:rate:{productCode}:v:{versionNo}:{hash}`：费率试算结果。
- `product:center:sales-rule:{productCode}:v:{versionNo}:{hash}`：销售规则匹配结果。
- `product:center:dict:{dictType}`：低频字典。

### 6.4 缓存策略

- 业务缓存必须设置 TTL，不允许永久缓存。
- 当前产品详情发布后主动刷新。
- 下架后删除当前产品缓存。
- 空结果短 TTL 缓存，防止穿透。
- 热点产品支持预热。
- 费率结果缓存 key 使用规范化请求参数 hash。

## 7. 前端实施方案

### 7.1 目录结构

```text
product-admin-ui/src
├── api
├── components
├── router
├── stores
├── types
├── utils
└── views
    ├── product
    ├── rate
    ├── rule
    ├── audit
    ├── supplier
    └── report
```

### 7.2 页面模块

- 仪表盘：指标卡、状态分布、待办、最近操作。
- 产品列表：查询、分页、重置、刷新、状态操作。
- 产品编辑向导：基础信息、险种责任、投保方案、费率绑定、投保要素、健康告知、联动规则、分红配置、条款、供应商、发布校验。
- 费率中心：因子库、费率表、导入批次、错误明细、审核。
- 规则中心：ECA 规则编辑、规则模拟器、渲染 JSON 预览。
- 审核中心：待我审核、已办、审核详情。
- 供应商管理：档案、合同、产品关联、到期提醒。
- 报表中心：目录导出、血缘关系、审计日志。

### 7.3 前端规则渲染

- 后端返回字段定义、模块定义、规则集和字典。
- 前端渲染引擎根据字段类型生成表单组件。
- ECA 引擎监听字段变化，计算条件，执行显隐、必填、只读、选项过滤和试算触发。
- 表达式只执行受控 DSL，不使用 `eval` 或动态 Function。
- 前端校验只负责交互体验，后端保存和发布时必须再次强校验。

## 8. 安全与审计

- 所有后台接口接入 JWT 或统一认证网关。
- 按菜单、按钮、数据范围执行权限校验。
- 发布、下架、费率发布、规则变更、导出、供应商合同变更记录审计日志。
- 审计日志字段包括操作人、操作时间、IP、traceId、对象类型、对象 ID、动作、变更摘要。
- 敏感字段脱敏后记录，禁止记录身份证号、手机号、银行卡号明文。
- 导出任务限制最大数据量，并记录导出条件。
- 动态表达式执行白名单校验，禁止文件、网络、系统命令、SQL 访问。

## 9. 测试方案

### 9.1 单元测试

- 产品状态机合法和非法流转。
- 产品版本递增和复制。
- 发布完整性校验。
- 分红险强制校验。
- 费率匹配边界。
- 投保方案合法组合。
- ECA 规则结构校验。

### 9.2 集成测试

- 产品创建、编辑、提交审核、驳回、审批、发布、下架。
- 费率导入、错误明细、精算审核、发布。
- 发布快照生成和开放接口读取。
- Redis 缓存刷新、删除、空缓存。
- 数据库唯一索引冲突。

### 9.3 前端测试

- 产品编辑向导分步保存。
- 费率导入错误展示。
- 健康告知问卷配置。
- 联动规则模拟。
- 审核列表和审核动作。
- 发布校验结果展示。

### 9.4 性能测试

- 10000 行费率导入含基础校验不超过 5 秒。
- 开放产品详情接口 200 TPS。
- 产品列表分页查询 2000 条以内不超过 2 秒。
- 发布快照生成耗时可监控。

### 9.5 安全测试

- 无权限用户不能访问后台接口。
- 按钮权限生效。
- 导出权限和数据量限制生效。
- 敏感日志脱敏。
- 表达式 DSL 无法执行任意脚本。

## 10. 实施里程碑

### 10.1 第一批：主数据与发布闭环

- 产品基础信息、版本、险种责任。
- 审核流轻量实现。
- 发布校验、发布快照、开放产品详情。
- 审计日志、基础权限。

### 10.2 第二批：费率与投保配置

- 费率因子、费率表导入、精算审核、费率试算。
- 投保要素、健康告知、职业限制、投保方案。
- 条款关联和基础字典。

### 10.3 第三批：分红、联动与供应商

- 分红方式、红利演示参数、分红关联责任。
- 前端联动规则、规则模拟器、前端动态渲染。
- 供应商档案、合同、产品关联、到期提醒。

### 10.4 第四批：组合、报表与优化

- 产品组合。
- 产品目录导出、费率摘要、责任摘要。
- 产品血缘关系图。
- 性能优化、缓存预热、监控告警。

## 11. 兼容与迁移策略

- 所有数据库结构变更通过迁移脚本追加，不修改已发布迁移。
- 新增字段优先允许空值或提供默认值，避免上线时阻塞历史数据。
- 已发布数据不物理删除，通过状态和逻辑删除控制。
- 旧版本产品继续通过发布快照查询。
- 上线前准备基础字典初始化脚本。
- 灰度发布开放接口，先接入只读查询，再接入试算和同步。

## 12. 主要风险与应对

| 风险 | 应对 |
| --- | --- |
| 联动规则过度复杂 | 首版限定结构化 ECA 和受控 DSL，复杂规则外置。 |
| 费率表维度膨胀 | 建立组合索引，按产品规模评估预计算、缓存或分表。 |
| 审批流复杂化 | 首版轻量内置，后续按复杂度评估 BPMN 或 OA 对接。 |
| 发布与缓存不一致 | 数据库事务保证快照一致，缓存失败走告警和补偿。 |
| 外部系统不可用 | 保存引用快照和校验结果，发布前校验外部引用有效性。 |

