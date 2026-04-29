package com.example.insurance.product.api.product;

import com.example.insurance.product.api.common.ApiResponse;
import com.example.insurance.product.application.product.CreateProductCommand;
import com.example.insurance.product.application.product.PageResult;
import com.example.insurance.product.application.product.ProductListQuery;
import com.example.insurance.product.application.product.ProductManagementApplicationService;
import com.example.insurance.product.application.publish.ProductPublishApplicationService;
import com.example.insurance.product.application.publish.PublishResult;
import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductStatus;
import com.example.insurance.product.domain.product.ProductType;
import com.example.insurance.product.domain.publish.PublishValidationResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 后台产品管理接口。
 */
@RestController
@RequestMapping("/admin/api/v1")
public class ProductAdminController {
    /**
     * 产品管理应用服务。
     */
    private final ProductManagementApplicationService productManagementApplicationService;
    /**
     * 产品发布应用服务。
     */
    private final ProductPublishApplicationService productPublishApplicationService;

    /**
     * 创建后台产品管理接口。
     *
     * @param productManagementApplicationService 产品管理应用服务
     * @param productPublishApplicationService 产品发布应用服务
     */
    public ProductAdminController(ProductManagementApplicationService productManagementApplicationService,
                                  ProductPublishApplicationService productPublishApplicationService) {
        this.productManagementApplicationService = productManagementApplicationService;
        this.productPublishApplicationService = productPublishApplicationService;
    }

    /**
     * 创建产品草稿。
     *
     * @param request 创建产品请求
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/products")
    public ApiResponse<ProductDetailResponse> create(@Valid @RequestBody CreateProductRequest request,
                                                     @RequestHeader(value = "X-Operator", required = false) String operator) {
        ProductDetailResponse response = ProductMapper.toDetailResponse(productManagementApplicationService.create(
                new CreateProductCommand(
                        request.productCode(),
                        request.productName(),
                        request.shortName(),
                        request.productType(),
                        request.productNature(),
                        request.insuranceCompany(),
                        request.salesChannels(),
                        resolveOperator(operator)
                )
        ));
        return ApiResponse.success(response);
    }

    /**
     * 分页查询产品列表。
     *
     * @param productCode 产品编码
     * @param productName 产品名称
     * @param productType 产品类型
     * @param productNature 产品属性
     * @param status 产品状态
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return 产品分页列表
     */
    @GetMapping("/products")
    public ApiResponse<PageResult<ProductListItemResponse>> list(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) ProductNature productNature,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<ProductListItemResponse> result = mapPage(productManagementApplicationService.list(
                new ProductListQuery(productCode, productName, productType, productNature, status, pageNo, pageSize)
        ));
        return ApiResponse.success(result);
    }

    /**
     * 查询产品详情。
     *
     * @param id 产品 ID
     * @return 产品详情
     */
    @GetMapping("/products/{id}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable long id) {
        return ApiResponse.success(ProductMapper.toDetailResponse(productManagementApplicationService.detail(id)));
    }

    /**
     * 提交产品版本审核。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/product-versions/{versionId}/submit-review")
    public ApiResponse<ProductDetailResponse> submitReview(
            @PathVariable long versionId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(
                productManagementApplicationService.submitReview(versionId, resolveOperator(operator))
        ));
    }

    /**
     * 审核通过产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/product-versions/{versionId}/approve")
    public ApiResponse<ProductDetailResponse> approve(
            @PathVariable long versionId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(
                productManagementApplicationService.approveReview(versionId, resolveOperator(operator))
        ));
    }

    /**
     * 发布前校验。
     *
     * @param versionId 产品版本 ID
     * @return 发布校验结果
     */
    @PostMapping("/product-versions/{versionId}/validate")
    public ApiResponse<PublishValidationResponse> validate(@PathVariable long versionId) {
        PublishValidationResult result = productManagementApplicationService.validatePublish(versionId);
        return ApiResponse.success(new PublishValidationResponse(result.passed(), result.issues()));
    }

    /**
     * 发布产品版本。
     *
     * @param versionId 产品版本 ID
     * @param operator 操作人
     * @return 发布结果
     */
    @PostMapping("/product-versions/{versionId}/publish")
    public ApiResponse<PublishResult> publish(
            @PathVariable long versionId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(productPublishApplicationService.publish(versionId, resolveOperator(operator)));
    }

    /**
     * 转换分页模型。
     *
     * @param source 应用层分页结果
     * @return 接口层分页结果
     */
    private static PageResult<ProductListItemResponse> mapPage(
            PageResult<com.example.insurance.product.application.product.ProductListItem> source) {
        return new PageResult<>(
                source.pageNo(),
                source.pageSize(),
                source.total(),
                source.records().stream().map(ProductMapper::toListResponse).toList()
        );
    }

    /**
     * 解析操作人。
     *
     * @param operator 请求头操作人
     * @return 操作人
     */
    private static String resolveOperator(String operator) {
        return Optional.ofNullable(operator).filter(value -> !value.isBlank()).orElse("system");
    }
}
