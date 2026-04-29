import axios from 'axios'
import type { ProductListItem } from '../types/product'

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
 * 查询产品列表。
 *
 * @returns 产品列表数据
 */
export async function listProducts() {
  const response = await axios.get<ApiResponse<ProductListItem[]>>('/admin/api/v1/products')
  return response.data.data
}
