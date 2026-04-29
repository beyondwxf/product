-- 表名 product_info：产品主档，保存产品编码、名称、寿险类型、产品属性和主状态。
CREATE TABLE product_info (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码，全局唯一，发布后不可修改',
    product_name VARCHAR(200) NOT NULL COMMENT '产品全称',
    short_name VARCHAR(100) NOT NULL COMMENT '产品简称',
    product_type VARCHAR(32) NOT NULL COMMENT '产品类型：TERM_LIFE 定期寿险、WHOLE_LIFE 终身寿险、ENDOWMENT 两全保险',
    product_nature VARCHAR(32) NOT NULL COMMENT '产品属性：TRADITIONAL_LIFE 传统寿险、DIVIDEND_LIFE 分红寿险',
    status VARCHAR(32) NOT NULL COMMENT '产品主状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_product_status (status, updated_at)
) COMMENT='产品主档';

-- 表名 product_version：产品版本，保存版本号、生效时间、失效时间和版本状态。
CREATE TABLE product_version (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    product_id BIGINT NOT NULL COMMENT '产品主档 ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码，冗余用于查询和快照追溯',
    version_no VARCHAR(32) NOT NULL COMMENT '产品版本号，例如 V1.0',
    status VARCHAR(32) NOT NULL COMMENT '产品版本状态',
    effective_at DATETIME NULL COMMENT '版本生效时间',
    expired_at DATETIME NULL COMMENT '版本失效时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_product_version (product_code, version_no),
    KEY idx_product_status (product_code, status, effective_at)
) COMMENT='产品版本';

-- 表名 risk_item：险种配置，保存产品版本下的主险和附加险。
CREATE TABLE risk_item (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    version_id BIGINT NOT NULL COMMENT '产品版本 ID',
    risk_code VARCHAR(64) NOT NULL COMMENT '险种代码',
    risk_name VARCHAR(200) NOT NULL COMMENT '险种名称',
    risk_type VARCHAR(32) NOT NULL COMMENT '险种类型：MAIN 主险、ATTACHMENT 附加险',
    coverage_amount_type VARCHAR(32) NOT NULL COMMENT '保额类型：固定、可选、按比例等',
    premium_calc_type VARCHAR(32) NOT NULL COMMENT '保费计算方式：单独费率、合并费率、与主险关联等',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_risk_code (version_id, risk_code),
    KEY idx_version (version_id)
) COMMENT='险种';

-- 表名 coverage_liability：保险责任配置，保存责任代码、责任类型、保额规则和理赔条件。
CREATE TABLE coverage_liability (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    version_id BIGINT NOT NULL COMMENT '产品版本 ID',
    risk_id BIGINT NOT NULL COMMENT '险种 ID',
    liability_code VARCHAR(64) NOT NULL COMMENT '责任代码',
    liability_name VARCHAR(200) NOT NULL COMMENT '责任名称',
    liability_type VARCHAR(32) NOT NULL COMMENT '责任类型，例如身故、全残、满期生存',
    claim_type VARCHAR(32) NOT NULL COMMENT '理赔类型，例如给付型',
    amount_rule JSON NULL COMMENT '保额设定规则 JSON',
    waiting_days INT NULL COMMENT '等待期天数',
    exemption_desc VARCHAR(1000) NULL COMMENT '免责说明',
    payment_condition VARCHAR(1000) NULL COMMENT '给付条件',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_liability_code (version_id, liability_code),
    KEY idx_risk (risk_id)
) COMMENT='保险责任';

-- 表名 rate_table：费率表头，保存费率表生命周期、因子结构和产品版本绑定关系。
CREATE TABLE rate_table (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    rate_table_code VARCHAR(64) NOT NULL COMMENT '费率表编码',
    rate_table_name VARCHAR(200) NOT NULL COMMENT '费率表名称',
    product_code VARCHAR(64) NULL COMMENT '绑定产品编码',
    version_no VARCHAR(32) NULL COMMENT '绑定产品版本号',
    status VARCHAR(32) NOT NULL COMMENT '费率表状态',
    factor_schema JSON NOT NULL COMMENT '费率因子结构定义 JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_rate_table_code (rate_table_code),
    KEY idx_status (status),
    KEY idx_product_version (product_code, version_no)
) COMMENT='费率表头';

-- 表名 rate_item：费率明细，保存按年龄、性别、职业等级、期间等维度匹配的费率。
CREATE TABLE rate_item (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    rate_table_id BIGINT NOT NULL COMMENT '费率表 ID',
    age INT NULL COMMENT '投保年龄',
    gender VARCHAR(16) NULL COMMENT '性别',
    occupation_level VARCHAR(16) NULL COMMENT '职业等级',
    insurance_period VARCHAR(32) NULL COMMENT '保险期间',
    payment_period VARCHAR(32) NULL COMMENT '缴费期间',
    amount_band VARCHAR(64) NULL COMMENT '保额档位',
    premium_rate DECIMAL(18, 6) NOT NULL COMMENT '保费费率',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    KEY idx_rate_match (rate_table_id, age, gender, occupation_level, insurance_period, payment_period)
) COMMENT='费率明细';

-- 表名 dividend_config：分红配置，保存分红方式、规则说明和红利演示假设。
CREATE TABLE dividend_config (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    version_id BIGINT NOT NULL COMMENT '产品版本 ID',
    dividend_methods JSON NOT NULL COMMENT '可选分红方式 JSON',
    rule_desc VARCHAR(2000) NULL COMMENT '分红规则说明',
    assumption_json JSON NOT NULL COMMENT '红利演示假设 JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_version (version_id)
) COMMENT='分红配置';

-- 表名 approval_instance：审核实例，保存一次产品或费率审核流程的整体状态。
CREATE TABLE approval_instance (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    object_type VARCHAR(64) NOT NULL COMMENT '审核对象类型',
    object_id BIGINT NOT NULL COMMENT '审核对象 ID',
    status VARCHAR(32) NOT NULL COMMENT '审核实例状态',
    started_by VARCHAR(64) NOT NULL COMMENT '发起人',
    started_at DATETIME NOT NULL COMMENT '发起时间',
    finished_at DATETIME NULL COMMENT '完成时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    KEY idx_object (object_type, object_id)
) COMMENT='审核实例';

-- 表名 approval_task：审核任务，保存具体审核节点、处理人和任务状态。
CREATE TABLE approval_task (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    instance_id BIGINT NOT NULL COMMENT '审核实例 ID',
    node_code VARCHAR(64) NOT NULL COMMENT '审核节点编码',
    node_name VARCHAR(100) NOT NULL COMMENT '审核节点名称',
    assignee_id VARCHAR(64) NOT NULL COMMENT '审核处理人 ID',
    status VARCHAR(32) NOT NULL COMMENT '审核任务状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    KEY idx_assignee (status, assignee_id),
    KEY idx_instance (instance_id)
) COMMENT='审核任务';

-- 表名 approval_record：审核记录，保存审核动作、意见、操作人和操作时间。
CREATE TABLE approval_record (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    instance_id BIGINT NOT NULL COMMENT '审核实例 ID',
    task_id BIGINT NOT NULL COMMENT '审核任务 ID',
    action VARCHAR(32) NOT NULL COMMENT '审核动作：通过、驳回、撤回等',
    opinion VARCHAR(1000) NULL COMMENT '审核意见',
    operator_id VARCHAR(64) NOT NULL COMMENT '操作人 ID',
    operated_at DATETIME NOT NULL COMMENT '操作时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    KEY idx_instance (instance_id)
) COMMENT='审核记录';

-- 表名 supplier_info：供应商档案，保存再保、服务、渠道供应商的基础信息。
CREATE TABLE supplier_info (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    supplier_code VARCHAR(64) NOT NULL COMMENT '供应商编码',
    supplier_name VARCHAR(200) NOT NULL COMMENT '供应商名称',
    supplier_type VARCHAR(32) NOT NULL COMMENT '供应商类型：再保、服务、渠道',
    cooperation_status VARCHAR(32) NOT NULL COMMENT '合作状态',
    effective_at DATETIME NULL COMMENT '合作生效时间',
    expired_at DATETIME NULL COMMENT '合作失效时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_supplier_code (supplier_code)
) COMMENT='供应商档案';

-- 表名 product_publish_snapshot：产品发布快照，保存开放接口读取的不可变产品定义。
CREATE TABLE product_publish_snapshot (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码',
    version_no VARCHAR(32) NOT NULL COMMENT '产品版本号',
    status VARCHAR(32) NOT NULL COMMENT '快照状态',
    snapshot_json JSON NOT NULL COMMENT '不可变产品定义快照 JSON',
    published_at DATETIME NOT NULL COMMENT '发布时间',
    published_by VARCHAR(64) NOT NULL COMMENT '发布人',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    UNIQUE KEY uk_snapshot (product_code, version_no),
    KEY idx_current (product_code, status)
) COMMENT='产品发布快照';

-- 表名 product_operation_log：操作审计日志，保存后台写操作的追踪信息。
CREATE TABLE product_operation_log (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    object_type VARCHAR(64) NOT NULL COMMENT '操作对象类型',
    object_id BIGINT NOT NULL COMMENT '操作对象 ID',
    action VARCHAR(64) NOT NULL COMMENT '操作动作',
    change_summary VARCHAR(2000) NULL COMMENT '变更摘要',
    operator_id VARCHAR(64) NOT NULL COMMENT '操作人 ID',
    operator_ip VARCHAR(64) NULL COMMENT '操作人 IP',
    trace_id VARCHAR(128) NULL COMMENT '链路追踪 ID',
    operated_at DATETIME NOT NULL COMMENT '操作时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    updated_by VARCHAR(64) NOT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0 未删除、1 已删除',
    KEY idx_operator (operator_id, operated_at),
    KEY idx_object (object_type, object_id)
) COMMENT='操作审计日志';
