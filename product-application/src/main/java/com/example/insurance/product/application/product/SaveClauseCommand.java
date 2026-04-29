package com.example.insurance.product.application.product;

/**
 * 保存条款引用命令。
 *
 * @param documentType 文件类型
 * @param documentId 外部文件 ID
 * @param documentVersion 文件版本
 * @param previewUrl 预览地址
 * @param operator 操作人
 */
public record SaveClauseCommand(
        String documentType,
        String documentId,
        String documentVersion,
        String previewUrl,
        String operator
) {
}
