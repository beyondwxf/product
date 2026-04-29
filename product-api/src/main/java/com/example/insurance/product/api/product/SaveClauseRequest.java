package com.example.insurance.product.api.product;

import jakarta.validation.constraints.NotBlank;

/**
 * 保存条款文件引用请求。
 *
 * @param documentType 文件类型
 * @param documentId 外部文件 ID
 * @param documentVersion 文件版本
 * @param previewUrl 预览地址
 */
public record SaveClauseRequest(
        @NotBlank String documentType,
        @NotBlank String documentId,
        @NotBlank String documentVersion,
        String previewUrl
) {
}
