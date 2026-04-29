package com.example.insurance.product.api.product;

import com.example.insurance.product.domain.publish.PublishValidationIssue;

import java.util.List;

/**
 * 发布校验响应。
 *
 * @param passed 是否通过
 * @param issues 校验问题列表
 */
public record PublishValidationResponse(boolean passed, List<PublishValidationIssue> issues) {
}
