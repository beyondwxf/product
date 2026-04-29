package com.example.insurance.product.domain.publish;

import java.util.List;

/**
 * 发布校验结果。
 *
 * @param issues 发布校验问题列表，为空表示通过
 */
public record PublishValidationResult(List<PublishValidationIssue> issues) {
    /**
     * 创建发布校验结果，并复制问题列表防止外部修改。
     *
     * @param issues 发布校验问题列表
     */
    public PublishValidationResult {
        issues = List.copyOf(issues);
    }

    /**
     * 判断发布校验是否通过。
     *
     * @return 校验是否通过
     */
    public boolean passed() {
        return issues.isEmpty();
    }
}
