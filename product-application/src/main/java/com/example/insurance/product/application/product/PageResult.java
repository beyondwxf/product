package com.example.insurance.product.application.product;

import java.util.List;

/**
 * 分页查询结果。
 *
 * @param pageNo 页码
 * @param pageSize 每页条数
 * @param total 总记录数
 * @param records 当前页记录
 * @param <T> 记录类型
 */
public record PageResult<T>(int pageNo, int pageSize, long total, List<T> records) {
}
