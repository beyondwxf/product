package com.example.insurance.product.application.open;

import java.util.Optional;

/**
 * 开放产品查询应用服务，仅读取已发布快照。
 */
public class OpenProductQueryService {
    /**
     * 发布快照查询端口。
     */
    private final PublishSnapshotQueryRepository publishSnapshotQueryRepository;

    /**
     * 创建开放产品查询服务。
     *
     * @param publishSnapshotQueryRepository 发布快照查询仓储
     */
    public OpenProductQueryService(PublishSnapshotQueryRepository publishSnapshotQueryRepository) {
        this.publishSnapshotQueryRepository = publishSnapshotQueryRepository;
    }

    /**
     * 查询当前生效产品快照。
     *
     * @param productCode 产品编码
     * @return 快照 JSON
     */
    public Optional<String> getCurrent(String productCode) {
        return publishSnapshotQueryRepository.findCurrentSnapshotJson(productCode);
    }

    /**
     * 查询指定版本产品快照。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     * @return 快照 JSON
     */
    public Optional<String> getVersion(String productCode, String versionNo) {
        return publishSnapshotQueryRepository.findSnapshotJson(productCode, versionNo);
    }
}
