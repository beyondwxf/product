package com.example.insurance.product.application.publish;

/**
 * 产品事件发布端口，用于隔离消息中间件实现。
 */
public interface ProductEventPublisher {
    /**
     * 发布产品版本激活事件。
     *
     * @param productCode 产品编码
     * @param versionNo 产品版本号
     */
    void publishProductActivated(String productCode, String versionNo);

    /**
     * 发布产品下架事件。
     *
     * @param productCode 产品编码
     * @param versionNo 产品版本号
     */
    void publishProductSuspended(String productCode, String versionNo);
}
