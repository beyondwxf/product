package com.example.insurance.product.api.product;

import com.example.insurance.product.api.common.ApiResponse;
import com.example.insurance.product.application.open.OpenProductQueryService;
import com.example.insurance.product.domain.common.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放产品查询接口，仅返回已发布快照。
 */
@RestController
@RequestMapping("/open/api/v1/products")
public class OpenProductController {
    /**
     * 开放产品查询服务。
     */
    private final OpenProductQueryService openProductQueryService;

    /**
     * 创建开放产品查询接口。
     *
     * @param openProductQueryService 开放产品查询服务
     */
    public OpenProductController(OpenProductQueryService openProductQueryService) {
        this.openProductQueryService = openProductQueryService;
    }

    /**
     * 查询当前生效产品定义。
     *
     * @param productCode 产品编码
     * @return 产品快照 JSON
     */
    @GetMapping("/{productCode}")
    public ApiResponse<String> current(@PathVariable String productCode) {
        return ApiResponse.success(openProductQueryService.getCurrent(productCode)
                .orElseThrow(() -> new BusinessException("PRODUCT_SNAPSHOT_NOT_FOUND", "产品快照不存在")));
    }

    /**
     * 查询指定版本产品定义。
     *
     * @param productCode 产品编码
     * @param versionNo 版本号
     * @return 产品快照 JSON
     */
    @GetMapping("/{productCode}/versions/{versionNo}")
    public ApiResponse<String> version(@PathVariable String productCode, @PathVariable String versionNo) {
        return ApiResponse.success(openProductQueryService.getVersion(productCode, versionNo)
                .orElseThrow(() -> new BusinessException("PRODUCT_SNAPSHOT_NOT_FOUND", "产品快照不存在")));
    }
}
