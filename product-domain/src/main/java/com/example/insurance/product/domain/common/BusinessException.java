package com.example.insurance.product.domain.common;

/**
 * 业务异常基类，用于在领域层表达可预期的业务规则失败。
 */
public class BusinessException extends RuntimeException {
    /**
     * 稳定错误码，供接口响应、日志检索和前端国际化映射使用。
     */
    private final String code;

    /**
     * 创建业务异常。
     *
     * @param code 稳定错误码
     * @param message 面向业务人员或调用方的错误描述
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 返回稳定错误码。
     *
     * @return 错误码
     */
    public String code() {
        return code;
    }
}
