import type {Order} from '../types/Order'
import {jwtUtils} from '../utils/jwt-utils'
import type {OrderStatus} from "../types/OrderStatus.ts";

type OrdersTableProps = {
  orders: Order[]
  onEdit: (order: Order) => void
  onDelete: (id: number) => void
}

export function OrdersTable({orders, onEdit, onDelete}: Readonly<OrdersTableProps>) {
  const hasManagerAuthority = jwtUtils.hasManagerAuthority()

  const getStatusBadgeClass = (status?: OrderStatus) => {
    switch (status) {
      case 'NEW': return 'bg-primary'
      case 'PROCESSING': return 'bg-info'
      case 'DELIVERING': return 'bg-warning'
      case 'SHIPPED': return 'bg-success'
      case 'CANCELLED': return 'bg-danger'
      default: return 'bg-secondary'
    }
  }

  if (orders.length === 0) {
    return <div className="alert alert-info">No orders found. Create your first order!</div>
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
            {hasManagerAuthority && <th>Actions</th>}
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
              {hasManagerAuthority && (
                <td>
                  <button
                    className="btn btn-sm btn-outline-primary me-2"
                    onClick={() => onEdit(order)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-sm btn-outline-danger"
                    onClick={() => order.id && onDelete(order.id)}
                  >
                    Delete
                  </button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
