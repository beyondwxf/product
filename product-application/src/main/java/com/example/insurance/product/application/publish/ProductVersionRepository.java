package com.example.insurance.product.application.publish;

import com.example.insurance.product.domain.publish.ProductPublishCandidate;

/**
 * 产品版本仓储端口，由基础设施层实现。
 */
public interface ProductVersionRepository {
    /**
     * 获取待发布产品版本摘要。
     *
     * @param versionId 产品版本 ID
     * @return 产品版本发布草稿摘要
     */
    ProductVersionDraft getPublishDraft(long versionId);

    /**
     * 组装发布校验候选对象。
     *
     * @param versionId 产品版本 ID
     * @return 产品发布候选对象
     */
    ProductPublishCandidate buildPublishCandidate(long versionId);

    /**
     * 激活产品版本并失效同产品旧版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     */
    void activateVersion(long versionId, String operator);
}
