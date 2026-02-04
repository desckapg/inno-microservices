import {useState} from 'react'
import type {CreateOrderRequest} from '../types/Order'
import type {Item} from "../types/Item.ts";

type Props = {
  isOpen: boolean
  onClose: () => void
  onSubmit: (data: CreateOrderRequest) => Promise<void>
  items: Item[]
  isLoading: boolean
}

type FormItem = { itemId: number; quantity: number; key: string }

export function CreateOrderModal({isOpen, onClose, onSubmit, items, isLoading}: Readonly<Props>) {
  const [orderItems, setOrderItems] = useState<FormItem[]>([
    {itemId: 0, quantity: 1, key: 'initial'}
  ])

  if (!isOpen) return null

  const generateUniqueKey = () => `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`

  const updateItem = (index: number, field: keyof FormItem, value: number) => {
    const newItems = [...orderItems]
    // @ts-expect-error: field corresponds to value type
    newItems[index][field] = value
    setOrderItems(newItems)
  }

  const removeItem = (index: number) => {
    const newItems = orderItems.filter((_, i) => i !== index)
    setOrderItems(newItems)
  }

  const handleSave = () => {
    const request: CreateOrderRequest = {
      orderItems: orderItems
        .filter(i => i.itemId !== 0 && i.quantity > 0)
        .map(i => ({ item: {id: i.itemId}, quantity: i.quantity }))
    }

    if (request.orderItems.length === 0) {
        alert("Please select at least one valid item.")
        return
    }

    onSubmit(request).then(() => {
        setOrderItems([{itemId: 0, quantity: 1, key: generateUniqueKey()}])
    }).catch(() => {
    })
  }

  const calculateTotal = () => {
    return orderItems.reduce((sum, item) => {
        const price = items.find(i => i.id === item.itemId)?.price || 0
        return sum + (price * item.quantity)
    }, 0).toFixed(2)
  }

  return (
    <div className="modal show d-block" tabIndex={-1} style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Create New Order</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            {orderItems.map((orderItem, index) => (
              <div key={orderItem.key} className="row mb-3 align-items-end">
                <div className="col-md-6">
                  <label htmlFor={`item-${index}`} className="form-label">Item</label>
                  <select
                    id={`item-${index}`}
                    className="form-select"
                    value={orderItem.itemId}
                    onChange={(e) => updateItem(index, 'itemId', Number.parseInt(e.target.value))}
                  >
                    <option value={0}>Select item</option>
                    {items.map(i => <option key={i.id} value={i.id}>{i.name} - ${i.price}</option>)}
                  </select>
                </div>
                <div className="col-md-4">
                  <label htmlFor={`qty-${index}`} className="form-label">Quantity</label>
                  <input
                    id={`qty-${index}`}
                    type="number"
                    className="form-control"
                    min="1"
                    value={orderItem.quantity}
                    onChange={(e) => updateItem(index, 'quantity', Number.parseInt(e.target.value) || 1)}
                  />
                </div>
                <div className="col-md-2">
                    <button
                        className="btn btn-outline-danger w-100"
                        onClick={() => removeItem(index)}
                        disabled={orderItems.length === 1}
                    >Remove</button>
                </div>
              </div>
            ))}
            <button
                className="btn btn-outline-primary btn-sm mb-3"
                onClick={() => setOrderItems([...orderItems, {itemId: 0, quantity: 1, key: generateUniqueKey()}])}
            >+ Add Item</button>
            <div className="alert alert-info"><strong>Total: ${calculateTotal()}</strong></div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="button" className="btn btn-primary" onClick={handleSave} disabled={isLoading}>
              {isLoading ? 'Creating...' : 'Create Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
