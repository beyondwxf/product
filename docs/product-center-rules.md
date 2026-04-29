# 保险产品中心后台系统规则

## 0. V1.5 文档基线

当前项目需求和实施以以下文档为准：

- `docs/product-center-srs-v1.5.md`：需求规格说明书，明确寿险与分红险范围、状态机、发布快照、验收标准和风险约束。
- `docs/product-center-technical-implementation.md`：技术实施方案，明确模块化单体架构、数据库、接口、缓存、前端、测试和里程碑。

本规则文件用于约束编码、架构边界和工程质量；当细节存在差异时，以 V1.5 需求规格说明书和技术实施方案中的业务口径为准。

## 1. 系统目标

保险产品中心用于沉淀保险产品主数据和产品配置能力，为运营后台、投保链路、报价服务、核保服务、保单服务提供统一、可版本化、可审计的产品数据。

核心目标：
- 产品数据统一维护。
- 产品配置版本化。
- 产品发布过程可校验、可审计、可回滚。
- 高并发查询优先走缓存或发布快照。
- 管理端操作安全、可追溯。
- V1.5 仅支持传统寿险与分红型寿险，不覆盖健康险、意外险、财险、车险等非寿险产品。

## 2. 业务模块

建议后台模块：
- 产品管理：产品基础信息、险种分类、保险公司、产品标签。
- 版本管理：草稿、待发布、已发布、已失效。
- 责任管理：主险、附加险、责任条款、保额、等待期、免赔额。
- 投保配置：投保要素、健康告知、职业限制、投保方案。
- 费率管理：费率表导入、费率查询、费率校验、费率版本。
- 销售规则：渠道、地区、年龄、职业、保额、缴费期间、保障期间。
- 分红管理：分红方式、红利演示参数、分红关联责任。
- 前端联动：ECA 规则、验证规则、模拟器、前端渲染 JSON。
- 供应商管理：供应商档案、合同、产品关联、供应商费率。
- 发布管理：完整性校验、发布审批、上下架、缓存刷新。
- 审计日志：关键配置变更记录和操作追踪。
- 字典配置：险种、缴费方式、保障期间、职业类别等。

## 3. 推荐状态机

产品状态：
- `DRAFT`：草稿，可编辑。
- `PENDING_REVIEW`：待审核，不允许普通编辑。
- `APPROVED`：已审核，可发布。
- `PUBLISHED`：已发布，只允许创建新版本。
- `SUSPENDED`：已下架，不可销售但历史可查。
- `EXPIRED`：已失效。

版本状态：
- `DRAFT`：草稿。
- `VALIDATING`：校验中。
- `READY`：可发布。
- `ACTIVE`：生效中。
- `INACTIVE`：已失效。

状态规则：
- 已发布版本不可原地修改。
- 新版本发布时，必须明确旧版本失效策略。
- 下架不等于删除，历史投保和保单仍需可追溯查询。

## 4. 后端接口建议

管理端接口：
- `POST /admin/api/v1/products`：创建产品。
- `PUT /admin/api/v1/products/{id}`：编辑草稿产品。
- `GET /admin/api/v1/products`：分页查询产品。
- `GET /admin/api/v1/products/{id}`：查看产品详情。
- `POST /admin/api/v1/products/{id}/versions`：创建新版本。
- `POST /admin/api/v1/product-versions/{versionId}/validate`：发布前校验。
- `POST /admin/api/v1/product-versions/{versionId}/publish`：发布版本。
- `POST /admin/api/v1/products/{id}/suspend`：产品下架。

开放查询接口：
- `GET /open/api/v1/products/{productCode}`：查询当前生效产品。
- `GET /open/api/v1/products/{productCode}/versions/{version}`：查询指定版本产品。
- `POST /open/api/v1/products/{productCode}/premium/calculate`：查询或计算保费。
- `POST /open/api/v1/products/{productCode}/sales-rules/match`：匹配销售规则。

## 5. 数据表建议

基础表：
- `product_info`：产品基础信息。
- `product_version`：产品版本。
- `product_liability`：保险责任。
- `product_rate_table`：费率表头。
- `product_rate_item`：费率明细。
- `product_sales_rule`：销售规则。
- `product_underwriting_rule_ref`：核保规则引用。
- `product_publish_record`：发布记录。
- `product_operation_log`：操作审计日志。

关键索引：
- `product_info(product_code)` 唯一索引。
- `product_version(product_code, version_no)` 唯一索引。
- `product_version(product_code, status, effective_at)` 查询当前生效版本。
- `product_rate_item(rate_table_id, age, gender, pay_period, coverage_period)` 费率匹配索引。
- `product_sales_rule(product_code, version_no, channel_code, region_code)` 销售规则匹配索引。

## 6. 缓存设计

推荐 Key：
- `product:center:current:{productCode}`：当前生效产品摘要。
- `product:center:detail:{productCode}:v:{versionNo}`：指定版本产品详情。
- `product:center:rate:{productCode}:v:{versionNo}:{hash}`：费率查询结果。
- `product:center:sales-rule:{productCode}:v:{versionNo}:{hash}`：销售规则匹配结果。

缓存一致性：
- 发布成功后刷新当前生效产品缓存。
- 下架成功后删除当前产品缓存。
- 费率或销售规则变更只允许在草稿版本发生，发布后生成新缓存。
- 缓存删除失败必须记录告警，必要时进入重试队列。

## 7. 发布校验清单

发布前必须校验：
- 产品基础信息完整。
- 产品编码、版本号唯一。
- 生效时间和失效时间合法。
- 至少配置一个保险责任。
- 费率维度覆盖销售规则要求。
- 销售区域、渠道、年龄范围、职业限制不冲突。
- 核保规则引用存在且版本有效。
- 当前发布不会造成同一产品多个 ACTIVE 版本冲突。

## 8. 权限建议

角色：
- 产品运营：维护草稿产品和规则。
- 产品审核：审核配置完整性和业务合理性。
- 发布管理员：执行发布、下架和回滚。
- 只读人员：查看产品配置和发布记录。
- 系统管理员：用户、角色、权限和字典维护。

权限粒度：
- 产品查看。
- 产品编辑。
- 费率导入。
- 规则配置。
- 发布审批。
- 上架下架。
- 数据导出。
- 审计查看。

## 9. 非功能要求

性能：
- 后台列表查询 P95 小于 500ms。
- 开放产品详情查询 P95 小于 100ms，优先使用缓存。
- 费率查询需根据业务规模决定是否预计算、缓存或拆分维度索引。

可靠性：
- 发布过程必须具备幂等控制。
- 发布失败不得污染当前生效版本。
- 缓存刷新失败不能导致数据库事务回滚，但必须告警和补偿。

可观测性：
- 关键接口记录 traceId。
- 发布、下架、导入、导出必须有业务日志。
- 缓存命中率、接口耗时、错误率需接入监控。

## 10. 开发优先级

第一阶段：
- 产品基础信息。
- 产品版本。
- 责任配置。
- 销售规则。
- 发布校验。
- 管理后台列表和详情。

第二阶段：
- 费率表导入和校验。
- 开放查询接口。
- Redis 缓存。
- 审计日志。

第三阶段：
- 发布审批流。
- 规则服务集成。
- 批量导入导出。
- 监控告警。
