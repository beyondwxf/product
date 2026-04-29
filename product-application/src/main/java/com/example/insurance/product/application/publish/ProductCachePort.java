package com.example.insurance.product.application.publish;

/**
 * 产品缓存端口，隔离应用层与 Redis 实现细节。
 */
public interface ProductCachePort {
    /**
     * 刷新当前生效产品缓存。
     *
     * @param productCode 产品编码
     * @param versionNo 产品版本号
     */
    void refreshCurrentProduct(String productCode, String versionNo);

    /**
     * 删除当前生效产品缓存。
     *
     * @param productCode 产品编码
     */
    void evictCurrentProduct(String productCode);
}
