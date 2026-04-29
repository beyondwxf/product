package com.example.insurance.product.domain.product;

import com.example.insurance.product.domain.common.BusinessException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 产品版本状态机，集中约束版本从草稿到生效、失效的合法流转。
 */
public final class ProductVersionStateMachine {
    /**
     * 产品版本状态允许流转关系表。
     */
    private static final Map<ProductVersionStatus, Set<ProductVersionStatus>> TRANSITIONS =
            new EnumMap<>(ProductVersionStatus.class);

    /**
     * 初始化产品版本状态流转规则。
     */
    static {
        TRANSITIONS.put(ProductVersionStatus.DRAFT, EnumSet.of(ProductVersionStatus.IN_REVIEW, ProductVersionStatus.INACTIVE));
        TRANSITIONS.put(ProductVersionStatus.IN_REVIEW, EnumSet.of(ProductVersionStatus.DRAFT, ProductVersionStatus.APPROVED));
        TRANSITIONS.put(ProductVersionStatus.APPROVED, EnumSet.of(ProductVersionStatus.ACTIVE, ProductVersionStatus.INACTIVE));
        TRANSITIONS.put(ProductVersionStatus.ACTIVE, EnumSet.of(ProductVersionStatus.INACTIVE));
        TRANSITIONS.put(ProductVersionStatus.INACTIVE, EnumSet.noneOf(ProductVersionStatus.class));
    }

    /**
     * 工具类禁止实例化。
     */
    private ProductVersionStateMachine() {
    }

    /**
     * 判断产品版本状态是否允许从当前状态流转到目标状态。
     *
     * @param current 当前状态
     * @param target 目标状态
     * @return 是否允许流转
     */
    public static boolean canTransit(ProductVersionStatus current, ProductVersionStatus target) {
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }

    /**
     * 校验产品版本状态流转合法性，非法时抛出业务异常。
     *
     * @param current 当前状态
     * @param target 目标状态
     */
    public static void assertCanTransit(ProductVersionStatus current, ProductVersionStatus target) {
        if (!canTransit(current, target)) {
            throw new BusinessException(
                    "PRODUCT_VERSION_STATUS_TRANSITION_DENIED",
                    "产品版本状态不允许从 %s 流转到 %s".formatted(current.desc(), target.desc())
            );
        }
    }
}
