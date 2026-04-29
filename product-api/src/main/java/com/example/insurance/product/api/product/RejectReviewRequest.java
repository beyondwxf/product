package com.example.insurance.product.api.product;

import jakarta.validation.constraints.NotBlank;

/**
 * 审核驳回请求。
 *
 * @param reason 驳回原因
 */
public record RejectReviewRequest(
        @NotBlank String reason
) {
}
