package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.BusinessException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 产品主档状态机，集中约束产品生命周期的合法流转。
 */
public final class ProductStateMachine {
    /**
     * 产品状态允许流转关系表。
     */
    private static final Map<ProductStatus, Set<ProductStatus>> TRANSITIONS = new EnumMap<>(ProductStatus.class);

    /**
     * 初始化产品状态流转规则。
     */
    static {
        TRANSITIONS.put(ProductStatus.DRAFT, EnumSet.of(ProductStatus.IN_REVIEW, ProductStatus.EXPIRED));
        TRANSITIONS.put(ProductStatus.IN_REVIEW, EnumSet.of(ProductStatus.REJECTED, ProductStatus.APPROVED, ProductStatus.DRAFT));
        TRANSITIONS.put(ProductStatus.REJECTED, EnumSet.of(ProductStatus.DRAFT, ProductStatus.IN_REVIEW));
        TRANSITIONS.put(ProductStatus.APPROVED, EnumSet.of(ProductStatus.PUBLISHED, ProductStatus.EXPIRED));
        TRANSITIONS.put(ProductStatus.PUBLISHED, EnumSet.of(ProductStatus.SUSPENDED, ProductStatus.EXPIRED));
        TRANSITIONS.put(ProductStatus.SUSPENDED, EnumSet.of(ProductStatus.PUBLISHED, ProductStatus.EXPIRED));
        TRANSITIONS.put(ProductStatus.EXPIRED, EnumSet.noneOf(ProductStatus.class));
    }

    /**
     * 工具类禁止实例化。
     */
    private ProductStateMachine() {
    }

    /**
     * 判断产品状态是否允许从当前状态流转到目标状态。
     *
     * @param current 当前状态
     * @param target 目标状态
     * @return 是否允许流转
     */
    public static boolean canTransit(ProductStatus current, ProductStatus target) {
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }

    /**
     * 校验产品状态流转合法性，非法时抛出业务异常。
     *
     * @param current 当前状态
     * @param target 目标状态
     */
    public static void assertCanTransit(ProductStatus current, ProductStatus target) {
        if (!canTransit(current, target)) {
            throw new BusinessException(
                    "PRODUCT_STATUS_TRANSITION_DENIED",
                    "产品状态不允许从 %s 流转到 %s".formatted(current.desc(), target.desc())
            );
        }
    }
}
