package com.example.insurance.product.api.product;

import com.example.insurance.product.api.common.ApiResponse;
import com.example.insurance.product.application.product.CreateProductCommand;
import com.example.insurance.product.application.product.PageResult;
import com.example.insurance.product.application.product.ProductListQuery;
import com.example.insurance.product.application.product.ProductManagementApplicationService;
import com.example.insurance.product.application.product.SaveClauseCommand;
import com.example.insurance.product.application.product.SaveLiabilityCommand;
import com.example.insurance.product.application.product.SaveRiskCommand;
import com.example.insurance.product.application.product.UpdateProductCommand;
import com.example.insurance.product.application.publish.ProductPublishApplicationService;
import com.example.insurance.product.application.publish.PublishResult;
import com.example.insurance.product.domain.product.ProductNature;
import com.example.insurance.product.domain.product.ProductStatus;
import com.example.insurance.product.domain.product.ProductType;
import com.example.insurance.product.domain.publish.PublishValidationResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
     * 编辑产品草稿。
     *
     * @param id 产品 ID
     * @param request 编辑请求
     * @param operator 操作人
     * @return 产品详情
     */
    @PutMapping("/products/{id}")
    public ApiResponse<ProductDetailResponse> updateDraft(
            @PathVariable long id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(productManagementApplicationService.updateDraft(
                id,
                new UpdateProductCommand(
                        request.productName(),
                        request.shortName(),
                        request.productType(),
                        request.productNature(),
                        request.insuranceCompany(),
                        request.salesChannels(),
                        resolveOperator(operator)
                )
        )));
    }

    /**
     * 基于当前产品创建新草稿版本。
     *
     * @param id 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/products/{id}/new-version")
    public ApiResponse<ProductDetailResponse> createNewVersion(
            @PathVariable long id,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(
                productManagementApplicationService.createNewVersion(id, resolveOperator(operator))
        ));
    }

    /**
     * 删除产品草稿。
     *
     * @param id 产品 ID
     * @param operator 操作人
     * @return 空响应
     */
    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteDraft(
            @PathVariable long id,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        productManagementApplicationService.deleteDraft(id, resolveOperator(operator));
        return ApiResponse.success(null);
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
     * 审核驳回产品版本。
     *
     * @param versionId 产品版本 ID
     * @param request 驳回请求
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/product-versions/{versionId}/reject")
    public ApiResponse<ProductDetailResponse> reject(
            @PathVariable long versionId,
            @Valid @RequestBody RejectReviewRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(
                productManagementApplicationService.rejectReview(versionId, request.reason(), resolveOperator(operator))
        ));
    }

    /**
     * 下架产品。
     *
     * @param id 产品 ID
     * @param operator 操作人
     * @return 产品详情
     */
    @PostMapping("/products/{id}/suspend")
    public ApiResponse<ProductDetailResponse> suspend(
            @PathVariable long id,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toDetailResponse(
                productManagementApplicationService.suspend(id, resolveOperator(operator))
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
     * 创建险种配置。
     *
     * @param versionId 产品版本 ID
     * @param request 险种请求
     * @param operator 操作人
     * @return 险种详情
     */
    @PostMapping("/product-versions/{versionId}/risks")
    public ApiResponse<RiskItemResponse> createRisk(
            @PathVariable long versionId,
            @Valid @RequestBody SaveRiskRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toRiskResponse(productManagementApplicationService.createRisk(
                versionId,
                new SaveRiskCommand(request.riskCode(), request.riskName(), request.riskType(),
                        request.coverageAmountType(), request.premiumCalcType(), request.sortNo(), resolveOperator(operator))
        )));
    }

    /**
     * 查询版本险种列表。
     *
     * @param versionId 产品版本 ID
     * @return 险种列表
     */
    @GetMapping("/product-versions/{versionId}/risks")
    public ApiResponse<List<RiskItemResponse>> listRisks(@PathVariable long versionId) {
        return ApiResponse.success(productManagementApplicationService.listRisks(versionId).stream()
                .map(ProductMapper::toRiskResponse)
                .toList());
    }

    /**
     * 更新险种配置。
     *
     * @param riskId 险种 ID
     * @param request 险种请求
     * @param operator 操作人
     * @return 险种详情
     */
    @PutMapping("/risks/{riskId}")
    public ApiResponse<RiskItemResponse> updateRisk(
            @PathVariable long riskId,
            @Valid @RequestBody SaveRiskRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toRiskResponse(productManagementApplicationService.updateRisk(
                riskId,
                new SaveRiskCommand(request.riskCode(), request.riskName(), request.riskType(),
                        request.coverageAmountType(), request.premiumCalcType(), request.sortNo(), resolveOperator(operator))
        )));
    }

    /**
     * 删除险种配置。
     *
     * @param riskId 险种 ID
     * @param operator 操作人
     * @return 空响应
     */
    @DeleteMapping("/risks/{riskId}")
    public ApiResponse<Void> deleteRisk(
            @PathVariable long riskId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        productManagementApplicationService.deleteRisk(riskId, resolveOperator(operator));
        return ApiResponse.success(null);
    }

    /**
     * 创建责任配置。
     *
     * @param riskId 险种 ID
     * @param request 责任请求
     * @param operator 操作人
     * @return 责任详情
     */
    @PostMapping("/risks/{riskId}/liabilities")
    public ApiResponse<LiabilityResponse> createLiability(
            @PathVariable long riskId,
            @Valid @RequestBody SaveLiabilityRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toLiabilityResponse(productManagementApplicationService.createLiability(
                riskId,
                new SaveLiabilityCommand(request.liabilityCode(), request.liabilityName(), request.liabilityType(),
                        request.claimType(), request.amountRule(), request.waitingDays(), request.exemptionDesc(),
                        request.paymentCondition(), resolveOperator(operator))
        )));
    }

    /**
     * 查询险种责任列表。
     *
     * @param riskId 险种 ID
     * @return 责任列表
     */
    @GetMapping("/risks/{riskId}/liabilities")
    public ApiResponse<List<LiabilityResponse>> listLiabilities(@PathVariable long riskId) {
        return ApiResponse.success(productManagementApplicationService.listLiabilities(riskId).stream()
                .map(ProductMapper::toLiabilityResponse)
                .toList());
    }

    /**
     * 更新责任配置。
     *
     * @param liabilityId 责任 ID
     * @param request 责任请求
     * @param operator 操作人
     * @return 责任详情
     */
    @PutMapping("/liabilities/{liabilityId}")
    public ApiResponse<LiabilityResponse> updateLiability(
            @PathVariable long liabilityId,
            @Valid @RequestBody SaveLiabilityRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toLiabilityResponse(productManagementApplicationService.updateLiability(
                liabilityId,
                new SaveLiabilityCommand(request.liabilityCode(), request.liabilityName(), request.liabilityType(),
                        request.claimType(), request.amountRule(), request.waitingDays(), request.exemptionDesc(),
                        request.paymentCondition(), resolveOperator(operator))
        )));
    }

    /**
     * 删除责任配置。
     *
     * @param liabilityId 责任 ID
     * @param operator 操作人
     * @return 空响应
     */
    @DeleteMapping("/liabilities/{liabilityId}")
    public ApiResponse<Void> deleteLiability(
            @PathVariable long liabilityId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        productManagementApplicationService.deleteLiability(liabilityId, resolveOperator(operator));
        return ApiResponse.success(null);
    }

    /**
     * 创建条款引用。
     *
     * @param versionId 产品版本 ID
     * @param request 条款引用请求
     * @param operator 操作人
     * @return 条款引用详情
     */
    @PostMapping("/product-versions/{versionId}/clauses")
    public ApiResponse<ClauseDocumentRefResponse> createClause(
            @PathVariable long versionId,
            @Valid @RequestBody SaveClauseRequest request,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        return ApiResponse.success(ProductMapper.toClauseResponse(productManagementApplicationService.createClause(
                versionId,
                new SaveClauseCommand(request.documentType(), request.documentId(), request.documentVersion(),
                        request.previewUrl(), resolveOperator(operator))
        )));
    }

    /**
     * 查询版本条款引用列表。
     *
     * @param versionId 产品版本 ID
     * @return 条款引用列表
     */
    @GetMapping("/product-versions/{versionId}/clauses")
    public ApiResponse<List<ClauseDocumentRefResponse>> listClauses(@PathVariable long versionId) {
        return ApiResponse.success(productManagementApplicationService.listClauses(versionId).stream()
                .map(ProductMapper::toClauseResponse)
                .toList());
    }

    /**
     * 删除条款引用。
     *
     * @param clauseId 条款引用 ID
     * @param operator 操作人
     * @return 空响应
     */
    @DeleteMapping("/clauses/{clauseId}")
    public ApiResponse<Void> deleteClause(
            @PathVariable long clauseId,
            @RequestHeader(value = "X-Operator", required = false) String operator) {
        productManagementApplicationService.deleteClause(clauseId, resolveOperator(operator));
        return ApiResponse.success(null);
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
