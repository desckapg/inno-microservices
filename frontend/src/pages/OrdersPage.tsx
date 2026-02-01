import {useEffect, useState} from 'react'
import {getOrders, createOrder, updateOrder, deleteOrder, getItems} from '../api/services/order-service'
import type {Order, Item, OrderStatus, CreateOrderRequest, UpdateOrderRequest} from '../types/Order'
import {jwtUtils} from '../utils/jwt-utils'

type OrderFormData = {
  orderItems: Array<{
    itemId: number
    quantity: number
    key: string
  }>
}

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) return err.message
  return 'Operation failed. Please try again.'
}

function generateUniqueKey(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 11)}`
}

export function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editingOrder, setEditingOrder] = useState<Order | null>(null)
  const [formData, setFormData] = useState<OrderFormData>({
    orderItems: [{itemId: 0, quantity: 1, key: generateUniqueKey()}]
  })
  const [editStatus, setEditStatus] = useState<OrderStatus>('NEW')

  useEffect(() => {
    loadOrders()
    loadItems()
  }, [])

  const loadOrders = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const userId = jwtUtils.getCurrentUserId()
      const filters = userId ? {userId} : undefined
      const data = await getOrders(filters)
      setOrders(data)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  const loadItems = async () => {
    try {
      const data = await getItems()
      setItems(data)
    } catch (err) {
      console.error('Failed to load items:', err)
    }
  }

  const handleCreateOrder = async () => {
    setError(null)
    setSuccess(null)

    if (formData.orderItems.some(item => item.itemId === 0 || item.quantity < 1)) {
      setError('Please select items and valid quantities.')
      return
    }

    setIsLoading(true)
    try {
      const createRequest: CreateOrderRequest = {
        orderItems: formData.orderItems.map(item => ({
          item: {id: item.itemId},
          quantity: item.quantity
        }))
      }
      await createOrder(createRequest)
      setSuccess('Order created successfully!')
      setShowCreateModal(false)
      setFormData({orderItems: [{itemId: 0, quantity: 1, key: generateUniqueKey()}]})
      await loadOrders()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  const handleUpdateOrder = async () => {
    if (!editingOrder?.id) return

    setError(null)
    setSuccess(null)
    setIsLoading(true)

    try {
      const updateRequest: UpdateOrderRequest = {
        status: editStatus
      }
      await updateOrder(editingOrder.id, updateRequest)
      setSuccess('Order updated successfully!')
      setShowEditModal(false)
      setEditingOrder(null)
      await loadOrders()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  const handleDeleteOrder = async (id: number) => {
    if (!confirm('Are you sure you want to delete this order?')) return

    setError(null)
    setSuccess(null)
    setIsLoading(true)

    try {
      await deleteOrder(id)
      setSuccess('Order deleted successfully!')
      await loadOrders()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  const openEditModal = (order: Order) => {
    setEditingOrder(order)
    setEditStatus(order.status || 'NEW')
    setShowEditModal(true)
  }

  const addOrderItem = () => {
    setFormData({
      orderItems: [...formData.orderItems, {itemId: 0, quantity: 1, key: generateUniqueKey()}]
    })
  }

  const removeOrderItem = (index: number) => {
    const newItems = formData.orderItems.filter((_, i) => i !== index)
    setFormData({orderItems: newItems.length > 0 ? newItems : [{itemId: 0, quantity: 1, key: generateUniqueKey()}]})
  }

  const updateOrderItem = (index: number, field: 'itemId' | 'quantity', value: number) => {
    const newItems = [...formData.orderItems]
    newItems[index][field] = value
    setFormData({orderItems: newItems})
  }


  const getItemPrice = (itemId: number) => {
    return items.find(item => item.id === itemId)?.price || 0
  }

  const calculateTotal = (orderItems: Array<{itemId: number; quantity: number}>) => {
    return orderItems.reduce((sum, item) => {
      return sum + (getItemPrice(item.itemId) * item.quantity)
    }, 0).toFixed(2)
  }

  const getStatusBadgeClass = (status?: OrderStatus) => {
    switch (status) {
      case 'NEW':
        return 'bg-primary'
      case 'PROCESSING':
        return 'bg-info'
      case 'DELIVERING':
        return 'bg-warning'
      case 'SHIPPED':
        return 'bg-success'
      case 'CANCELLED':
        return 'bg-danger'
      default:
        return 'bg-secondary'
    }
  }

  const renderOrdersContent = () => {
    if (isLoading && orders.length === 0) {
      return (
          <div className="text-center py-5">
            <div className="spinner-border text-primary" aria-live="polite" aria-label="Loading orders">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
      )
    }

    if (orders.length === 0) {
      return (
          <div className="alert alert-info">
            No orders found. Create your first order!
          </div>
      )
    }

    return (
        <div className="table-responsive">
          <table className="table table-hover">
            <thead className="table-light">
            <tr>
              <th>Order ID</th>
              <th>User</th>
              <th>Status</th>
              <th>Items</th>
              <th>Total</th>
              <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{order.user ? `${order.user.name} ${order.user.surname}` : 'N/A'}</td>
                  <td>
                        <span className={`badge ${getStatusBadgeClass(order.status)}`}>
                          {order.status}
                        </span>
                  </td>
                  <td>
                    <ul className="list-unstyled mb-0">
                      {order.orderItems.map((item) => (
                          <li key={`${order.id}-${item.item.id}`} className="small">
                            {item.item.name} × {item.quantity}
                          </li>
                      ))}
                    </ul>
                  </td>
                  <td>
                    $
                    {order.orderItems
                        .reduce((sum, item) => sum + (item.item.price * item.quantity), 0)
                        .toFixed(2)}
                  </td>
                  <td>
                    <button
                        className="btn btn-sm btn-outline-primary me-2"
                        onClick={() => openEditModal(order)}
                        disabled={isLoading}
                    >
                      Edit
                    </button>
                    <button
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => order.id && handleDeleteOrder(order.id)}
                        disabled={isLoading}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
    )
  }

  return (
      <div className="container py-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h1 className="h3 fw-bold">Order Management</h1>
          <button
              className="btn btn-primary"
              onClick={() => setShowCreateModal(true)}
              disabled={isLoading}
          >
            <i className="bi bi-plus-circle me-2"></i>{' '}
            Create Order
          </button>
        </div>

        {error && (
            <div className="alert alert-danger alert-dismissible fade show" role="alert">
              {error}
              <button type="button" className="btn-close" onClick={() => setError(null)}></button>
            </div>
        )}

        {success && (
            <div className="alert alert-success alert-dismissible fade show" role="alert">
              {success}
              <button type="button" className="btn-close" onClick={() => setSuccess(null)}></button>
            </div>
        )}

        {renderOrdersContent()}

        {/* Create Order Modal */}
        {showCreateModal && (
            <div className="modal show d-block" tabIndex={-1} style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
              <div className="modal-dialog modal-lg">
                <div className="modal-content">
                  <div className="modal-header">
                    <h5 className="modal-title">Create New Order</h5>
                    <button
                        type="button"
                        className="btn-close"
                        onClick={() => {
                          setShowCreateModal(false)
                          setFormData({orderItems: [{itemId: 0, quantity: 1, key: generateUniqueKey()}]})
                        }}
                    ></button>
                  </div>
                  <div className="modal-body">
                    <h6 className="mb-3">Order Items</h6>
                    {formData.orderItems.map((orderItem, index) => (
                        <div key={orderItem.key} className="row mb-3 align-items-end">
                          <div className="col-md-6">
                            <label htmlFor={`item-select-${index}`} className="form-label">Item</label>
                            <select
                                id={`item-select-${index}`}
                                className="form-select"
                                value={orderItem.itemId}
                                onChange={(e) => updateOrderItem(index, 'itemId', Number.parseInt(e.target.value, 10))}
                            >
                              <option value={0}>Select an item</option>
                              {items.map((item) => (
                                  <option key={item.id} value={item.id}>
                                    {item.name} - ${item.price}
                                  </option>
                              ))}
                            </select>
                          </div>
                          <div className="col-md-4">
                            <label htmlFor={`item-quantity-${index}`} className="form-label">Quantity</label>
                            <input
                                id={`item-quantity-${index}`}
                                type="number"
                                className="form-control"
                                min="1"
                                value={orderItem.quantity}
                                onChange={(e) => updateOrderItem(index, 'quantity', Number.parseInt(e.target.value, 10) || 1)}
                            />
                          </div>
                          <div className="col-md-2">
                            <button
                                type="button"
                                className="btn btn-outline-danger w-100"
                                onClick={() => removeOrderItem(index)}
                                disabled={formData.orderItems.length === 1}
                            >
                              Remove
                            </button>
                          </div>
                        </div>
                    ))}
                    <button
                        type="button"
                        className="btn btn-outline-primary btn-sm mb-3"
                        onClick={addOrderItem}
                    >
                      + Add Item
                    </button>
                    <div className="alert alert-info">
                      <strong>Total: ${calculateTotal(formData.orderItems)}</strong>
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={() => {
                          setShowCreateModal(false)
                          setFormData({orderItems: [{itemId: 0, quantity: 1, key: generateUniqueKey()}]})
                        }}
                    >
                      Cancel
                    </button>
                    <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleCreateOrder}
                        disabled={isLoading}
                    >
                      {isLoading ? 'Creating...' : 'Create Order'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
        )}

        {/* Edit Order Modal */}
        {showEditModal && editingOrder && (
            <div className="modal show d-block" tabIndex={-1} style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
              <div className="modal-dialog">
                <div className="modal-content">
                  <div className="modal-header">
                    <h5 className="modal-title">Edit Order #{editingOrder.id}</h5>
                    <button
                        type="button"
                        className="btn-close"
                        onClick={() => {
                          setShowEditModal(false)
                          setEditingOrder(null)
                        }}
                    ></button>
                  </div>
                  <div className="modal-body">
                    <div className="mb-3">
                      <label htmlFor="edit-order-status" className="form-label">Order Status</label>
                      <select
                          id="edit-order-status"
                          className="form-select"
                          value={editStatus}
                          onChange={(e) => setEditStatus(e.target.value as OrderStatus)}
                      >
                        <option value="NEW">NEW</option>
                        <option value="PROCESSING">PROCESSING</option>
                        <option value="DELIVERING">DELIVERING</option>
                        <option value="SHIPPED">SHIPPED</option>
                        <option value="CANCELLED">CANCELLED</option>
                      </select>
                    </div>
                    <div className="mb-3">
                      <div className="fw-bold mb-2">Order Items:</div>
                      <ul className="list-group">
                        {editingOrder.orderItems.map((item) => (
                            <li key={`edit-item-${item.item.id}`} className="list-group-item d-flex justify-content-between">
                              <span>{item.item.name} × {item.quantity}</span>
                              <span className="fw-bold">${(item.item.price * item.quantity).toFixed(2)}</span>
                            </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={() => {
                          setShowEditModal(false)
                          setEditingOrder(null)
                        }}
                    >
                      Cancel
                    </button>
                    <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleUpdateOrder}
                        disabled={isLoading}
                    >
                      {isLoading ? 'Updating...' : 'Update Order'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
        )}
      </div>
  )
}
