package com.example.insurance.product.domain.publish;

/**
 * 发布校验问题，面向前端展示具体模块、错误码和原因。
 *
 * @param module 校验失败模块
 * @param code 稳定错误码
 * @param message 中文错误描述
 */
public record PublishValidationIssue(String module, String code, String message) {
}
