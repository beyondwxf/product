import axios from 'axios'
import type {
  CreateProductPayload,
  PageResult,
  ProductListItem,
  ProductNature,
  ProductStatus,
  ProductType,
  PublishValidationResult
} from '../types/product'

/**
 * 后端统一接口响应结构。
 */
export interface ApiResponse<T> {
  /** 响应码。 */
  code: string
  /** 中文响应消息。 */
  message: string
  /** 链路追踪 ID。 */
  traceId?: string
  /** 响应数据。 */
  data: T
}

/**
 * 产品查询条件。
 */
export interface ProductQuery {
  /** 产品编码。 */
  productCode?: string
  /** 产品名称。 */
  productName?: string
  /** 产品类型。 */
  productType?: ProductType | ''
  /** 产品属性。 */
  productNature?: ProductNature | ''
  /** 产品状态。 */
  status?: ProductStatus | ''
  /** 页码。 */
  pageNo: number
  /** 每页条数。 */
  pageSize: number
}

/**
 * 查询产品列表。
 *
 * @param query 查询条件
 * @returns 产品分页数据
 */
export async function listProducts(query: ProductQuery) {
  const response = await axios.get<ApiResponse<PageResult<ProductListItem>>>('/admin/api/v1/products', {
    params: query
  })
  return response.data.data
}

/**
 * 创建产品草稿。
 *
 * @param payload 创建产品表单
 * @returns 创建后的产品列表项兼容数据
 */
export async function createProduct(payload: CreateProductPayload) {
  const response = await axios.post<ApiResponse<ProductListItem>>('/admin/api/v1/products', payload)
  return response.data.data
}

/**
 * 提交产品版本审核。
 *
 * @param versionId 产品版本 ID
 */
export async function submitReview(versionId: number) {
  await axios.post<ApiResponse<ProductListItem>>(`/admin/api/v1/product-versions/${versionId}/submit-review`)
}

/**
 * 审核通过产品版本。
 *
 * @param versionId 产品版本 ID
 */
export async function approveReview(versionId: number) {
  await axios.post<ApiResponse<ProductListItem>>(`/admin/api/v1/product-versions/${versionId}/approve`)
}

/**
 * 执行发布前校验。
 *
 * @param versionId 产品版本 ID
 * @returns 发布校验结果
 */
export async function validatePublish(versionId: number) {
  const response = await axios.post<ApiResponse<PublishValidationResult>>(
    `/admin/api/v1/product-versions/${versionId}/validate`
  )
  return response.data.data
}

/**
 * 发布产品版本。
 *
 * @param versionId 产品版本 ID
 */
export async function publishVersion(versionId: number) {
  await axios.post<ApiResponse<{ productCode: string; versionNo: string }>>(
    `/admin/api/v1/product-versions/${versionId}/publish`
  )
}
