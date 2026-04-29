package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 产品状态机单元测试。
 */
class ProductStateMachineTest {
    /**
     * 验证草稿产品允许提交审核。
     */
    @Test
    void shouldAllowDraftSubmitReview() {
        assertTrue(ProductStateMachine.canTransit(ProductStatus.DRAFT, ProductStatus.IN_REVIEW));
        assertDoesNotThrow(() -> ProductStateMachine.assertCanTransit(ProductStatus.DRAFT, ProductStatus.IN_REVIEW));
    }

    /**
     * 验证草稿产品不允许跳过审核直接上架。
     */
    @Test
    void shouldRejectDirectDraftPublish() {
        assertFalse(ProductStateMachine.canTransit(ProductStatus.DRAFT, ProductStatus.PUBLISHED));
        assertThrows(BusinessException.class,
                () -> ProductStateMachine.assertCanTransit(ProductStatus.DRAFT, ProductStatus.PUBLISHED));
    }
}
