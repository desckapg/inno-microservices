import {useEffect, useState} from 'react'
import {getOrders, createOrder, updateOrder, deleteOrder, getItems} from '../api/services/order-service'
import type {Order, Item, CreateOrderRequest, UpdateOrderRequest} from '../types/Order'
import {jwtUtils} from '../utils/jwt-utils'
import {tokenStore} from '../utils/token-store'
import {OrdersTable} from '../components/OrdersTable'
import {CreateOrderModal} from '../components/CreateOrderModal'
import {EditOrderModal} from '../components/EditOrderModal'
import {useNavigate} from 'react-router'

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  return 'Operation failed.'
}

export function OrdersPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<Order[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const [showCreateModal, setShowCreateModal] = useState(false)
  const [editingOrder, setEditingOrder] = useState<Order | null>(null)

  useEffect(() => { loadData() }, [])

  const handleLogout = () => {
    tokenStore.clearTokens()
    navigate('/login')
  }

  const loadData = async () => {
    setIsLoading(true)
    try {
      const userId = jwtUtils.getCurrentUserId()
      const [ordersData, itemsData] = await Promise.all([
         getOrders(userId ? {userId} : undefined),
         getItems()
      ])
      setOrders(ordersData)
      setItems(itemsData)
    } catch (err) { setError(getErrorMessage(err)) }
    finally { setIsLoading(false) }
  }

  const handleCreate = async (request: CreateOrderRequest) => {
    try {
      setError(null)
      setIsLoading(true)
      await createOrder(request)
      setSuccess('Order created!')
      setShowCreateModal(false)
      loadData()
    } finally { setIsLoading(false) }
  }

  const handleUpdate = async (id: number, request: UpdateOrderRequest) => {
    try {
        setError(null)
        setIsLoading(true)
        await updateOrder(id, request)
        setSuccess('Order updated!')
        setEditingOrder(null)
        loadData()
    } catch (err) { setError(getErrorMessage(err)) }
    finally { setIsLoading(false) }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Delete order?')) return
    try {
        setIsLoading(true)
        await deleteOrder(id)
        setSuccess('Order deleted!')
        loadData()
    } catch (err) { setError(getErrorMessage(err)) }
    finally { setIsLoading(false) }
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 fw-bold">Order Management</h1>
        <div className="d-flex gap-2">
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <i className="bi bi-plus-circle me-2"></i> Create Order
          </button>
          <button className="btn btn-outline-secondary" onClick={handleLogout}>
            <i className="bi bi-box-arrow-right me-2"></i> Logout
          </button>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {isLoading && !orders.length ? (
        <div className="spinner-border text-primary" />
      ) : (
        <OrdersTable
            orders={orders}
            onEdit={setEditingOrder}
            onDelete={handleDelete}
        />
      )}

      <CreateOrderModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSubmit={handleCreate}
        items={items}
        isLoading={isLoading}
      />

      <EditOrderModal
        isOpen={!!editingOrder}
        order={editingOrder}
        onClose={() => setEditingOrder(null)}
        onSubmit={handleUpdate}
        isLoading={isLoading}
      />
    </div>
  )
}
