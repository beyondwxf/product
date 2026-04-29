package com.example.insurance.product.application.open;

import java.util.Optional;

/**
 * 发布快照查询仓储端口。
 */
public interface PublishSnapshotQueryRepository {
    /**
     * 查询当前生效快照 JSON。
     *
     * @param productCode 产品编码
     * @return 快照 JSON
     */
    Optional<String> findCurrentSnapshotJson(String productCode);

    /**
     * 查询指定版本快照 JSON。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     * @return 快照 JSON
     */
    Optional<String> findSnapshotJson(String productCode, String versionNo);
}
