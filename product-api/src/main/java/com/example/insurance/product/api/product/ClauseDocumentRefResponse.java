package com.example.insurance.product.api.product;

/**
 * 条款文件引用响应。
 *
 * @param id 条款引用 ID
 * @param versionId 产品版本 ID
 * @param documentType 文件类型
 * @param documentId 外部文件 ID
 * @param documentVersion 文件版本
 * @param previewUrl 预览地址
 */
public record ClauseDocumentRefResponse(
        long id,
        long versionId,
        String documentType,
        String documentId,
        String documentVersion,
        String previewUrl
) {
}
