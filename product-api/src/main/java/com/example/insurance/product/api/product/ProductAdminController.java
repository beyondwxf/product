package com.example.insurance.product.api.product;

import com.example.insurance.product.api.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台产品管理接口。
 */
@RestController
@RequestMapping("/admin/api/v1/products")
public class ProductAdminController {
    /**
     * 分页查询产品列表。
     *
     * @return 产品列表响应
     */
    @GetMapping
    public ApiResponse<List<ProductListItemResponse>> list() {
        return ApiResponse.success(List.of());
    }
}
