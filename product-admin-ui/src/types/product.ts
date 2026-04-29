/**
 * 产品属性类型，限定为传统寿险和分红寿险。
 */
export type ProductNature = 'TRADITIONAL_LIFE' | 'DIVIDEND_LIFE'

/**
 * 产品类型，限定 V1.5 支持的寿险范围。
 */
export type ProductType = 'TERM_LIFE' | 'WHOLE_LIFE' | 'ENDOWMENT'

/**
 * 产品主档生命周期状态类型。
 */
export type ProductStatus =
  | 'DRAFT'
  | 'IN_REVIEW'
  | 'REJECTED'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'SUSPENDED'
  | 'EXPIRED'

/**
 * 产品版本生命周期状态类型。
 */
export type ProductVersionStatus = 'DRAFT' | 'IN_REVIEW' | 'APPROVED' | 'ACTIVE' | 'INACTIVE'

/**
 * 分页响应数据。
 */
export interface PageResult<T> {
  /** 当前页码。 */
  pageNo: number
  /** 每页条数。 */
  pageSize: number
  /** 总记录数。 */
  total: number
  /** 当前页记录。 */
  records: T[]
}

/**
 * 产品列表项前端类型。
 */
export interface ProductListItem {
  /** 产品 ID。 */
  id: number
  /** 产品编码。 */
  productCode: string
  /** 产品名称。 */
  productName: string
  /** 产品类型。 */
  productType: ProductType
  /** 产品属性。 */
  productNature: ProductNature
  /** 当前版本 ID。 */
  versionId: number
  /** 当前版本号。 */
  versionNo: string
  /** 产品状态。 */
  status: ProductStatus
  /** 最近更新时间。 */
  updatedAt: string
  /** 最近操作人。 */
  updatedBy: string
}

/**
 * 创建产品表单。
 */
export interface CreateProductPayload {
  /** 产品编码。 */
  productCode: string
  /** 产品名称。 */
  productName: string
  /** 产品简称。 */
  shortName: string
  /** 产品类型。 */
  productType: ProductType
  /** 产品属性。 */
  productNature: ProductNature
  /** 保险公司。 */
  insuranceCompany: string
  /** 销售渠道。 */
  salesChannels: string
}

/**
 * 发布校验问题。
 */
export interface PublishValidationIssue {
  /** 失败模块。 */
  module: string
  /** 错误编码。 */
  code: string
  /** 错误消息。 */
  message: string
}

/**
 * 发布校验结果。
 */
export interface PublishValidationResult {
  /** 是否通过。 */
  passed: boolean
  /** 校验问题列表。 */
  issues: PublishValidationIssue[]
}
