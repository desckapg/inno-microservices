import type { OrderStatus } from './OrderStatus'
import type { User } from './User'
import type { OrderItem } from './OrderItem'

export interface Order {
  id?: number
  user?: User
  status?: OrderStatus
  orderItems: OrderItem[]
}

export interface CreateOrderRequest {
  orderItems: Array<{
    item: {
      id: number
    }
    quantity: number
  }>
}

export interface UpdateOrderRequest {
  status: OrderStatus
  orderItems?: OrderItem[]
}

export interface OrderFilters {
  userId?: string
  ids?: number[]
  statuses?: OrderStatus[]
}

