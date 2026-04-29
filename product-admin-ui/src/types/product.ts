/**
 * 产品属性类型，限定为传统寿险和分红寿险。
 */
export type ProductNature = 'TRADITIONAL_LIFE' | 'DIVIDEND_LIFE'

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
 * 产品列表项前端类型。
 */
export interface ProductListItem {
  /** 产品编码。 */
  productCode: string
  /** 产品名称。 */
  productName: string
  /** 产品类型，限定寿险支持范围。 */
  productType: 'TERM_LIFE' | 'WHOLE_LIFE' | 'ENDOWMENT'
  /** 产品属性，区分传统寿险和分红寿险。 */
  productNature: ProductNature
  /** 当前版本号。 */
  versionNo: string
  /** 产品状态。 */
  status: ProductStatus
}
