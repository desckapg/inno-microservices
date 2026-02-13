import {useState} from 'react'
import type {Order, UpdateOrderRequest} from '../types/Order'
import type {OrderStatus} from "../types/OrderStatus.ts";

type Props = {
  order: Order | null
  isOpen: boolean
  onClose: () => void
  onSubmit: (id: number, data: UpdateOrderRequest) => Promise<void>
  isLoading: boolean
}

export function EditOrderModal({order, isOpen, onClose, onSubmit, isLoading}: Readonly<Props>) {
  const [status, setStatus] = useState<OrderStatus>(order?.status || 'NEW')

  if (!isOpen || !order) return null

  return (
    <div className="modal show d-block" tabIndex={-1} style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Edit Order #{order.id}</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <div className="mb-3">
              <label htmlFor="order-status" className="form-label">Order Status</label>
              <select
                id="order-status"
                className="form-select"
                value={status}
                onChange={(e) => setStatus(e.target.value as OrderStatus)}
              >
                <option value="NEW">NEW</option>
                <option value="PROCESSING">PROCESSING</option>
                <option value="DELIVERING">DELIVERING</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            {/* Displaying static items for context */}
            <ul className="list-group">
                {order.orderItems.map(item => (
                    <li key={item.item.id} className="list-group-item d-flex justify-content-between">
                        <span>{item.item.name} × {item.quantity}</span>
                        <span className="fw-bold">${(item.item.price * item.quantity).toFixed(2)}</span>
                    </li>
                ))}
            </ul>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button
                type="button"
                className="btn btn-primary"
                onClick={() => order.id && onSubmit(order.id, {status})}
                disabled={isLoading}
            >
              {isLoading ? 'Updating...' : 'Update Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
