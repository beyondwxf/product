package com.example.insurance.product.application.publish;

/**
 * 发布快照仓储端口，由基础设施层实现。
 */
public interface PublishSnapshotRepository {
    /**
     * 为产品版本创建不可变发布快照。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 发布快照摘要
     */
    PublishSnapshot createSnapshot(long versionId, String operator);
}
