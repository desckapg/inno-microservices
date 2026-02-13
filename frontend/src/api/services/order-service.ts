import apiClient from '../axios-client'
import type {Order, CreateOrderRequest, UpdateOrderRequest, OrderFilters, Item} from '../../types/Order'
import axios from 'axios'

export async function getOrders(filters?: OrderFilters): Promise<Order[]> {
  try {
    const params = new URLSearchParams()
    if (filters?.userId) params.append('userId', filters.userId.toString())
    if (filters?.ids) filters.ids.forEach(id => params.append('ids', id.toString()))
    if (filters?.statuses) filters.statuses.forEach(status => params.append('statuses', status))

    const {data} = await apiClient.get<Order[]>('/api/v1/orders', {params})
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      throw new Error(err.response?.data?.message ?? 'Failed to fetch orders.')
    }
    throw err
  }
}

export async function getOrderById(id: number): Promise<Order> {
  try {
    const {data} = await apiClient.get<Order>(`/api/v1/orders/${id}`)
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      throw new Error(err.response?.data?.message ?? 'Failed to fetch order.')
    }
    throw err
  }
}

export async function createOrder(order: CreateOrderRequest): Promise<Order> {
  try {
    const {data} = await apiClient.post<Order>('/api/v1/orders', order)
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status
      if (status === 400) {
        throw new Error('Invalid order data.')
      }
      throw new Error(err.response?.data?.message ?? 'Failed to create order.')
    }
    throw err
  }
}

export async function updateOrder(id: number, order: UpdateOrderRequest): Promise<Order> {
  try {
    const {data} = await apiClient.put<Order>(`/api/v1/orders/${id}`, order)
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status
      if (status === 403) {
        throw new Error('You do not have permission to update orders.')
      }
      if (status === 404) {
        throw new Error('Order not found.')
      }
      throw new Error(err.response?.data?.message ?? 'Failed to update order.')
    }
    throw err
  }
}

export async function deleteOrder(id: number): Promise<void> {
  try {
    await apiClient.delete(`/api/v1/orders/${id}`)
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status
      if (status === 403) {
        throw new Error('You do not have permission to delete orders.')
      }
      if (status === 404) {
        throw new Error('Order not found.')
      }
      throw new Error(err.response?.data?.message ?? 'Failed to delete order.')
    }
    throw err
  }
}

export async function getItems(): Promise<Item[]> {
  try {
    const {data} = await apiClient.get<Item[]>('/api/v1/items')
    return data
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      throw new Error(err.response?.data?.message ?? 'Failed to fetch items.')
    }
    throw err
  }
}
