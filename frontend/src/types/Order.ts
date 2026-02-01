export type OrderStatus = 'NEW' | 'PROCESSING' | 'DELIVERING' | 'CANCELLED' | 'SHIPPED'

export interface Item {
  id: number
  name: string
  price: number
}

export interface OrderItem {
  item: Item
  quantity: number
}

export interface User {
  id: number
  name: string
  surname: string
  email: string
}

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
  userId?: number
  ids?: number[]
  statuses?: OrderStatus[]
}
