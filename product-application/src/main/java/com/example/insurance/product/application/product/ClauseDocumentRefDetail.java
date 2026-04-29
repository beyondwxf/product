package com.example.insurance.product.application.product;

/**
 * 条款文件引用详情。
 *
 * @param id 条款引用 ID
 * @param versionId 产品版本 ID
 * @param documentType 文件类型
 * @param documentId 外部文件 ID
 * @param documentVersion 文件版本
 * @param previewUrl 预览地址
 */
public record ClauseDocumentRefDetail(
        long id,
        long versionId,
        String documentType,
        String documentId,
        String documentVersion,
        String previewUrl
) {
}
