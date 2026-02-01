function App() {
  return (
      <div className="container mt-5">
        <div className="text-center">
          <h1 className="mb-4">Innowise Microservices</h1>
          <div className="d-flex gap-3 justify-content-center">
            <a href="/login" className="btn btn-primary">Login</a>
            <a href="/register" className="btn btn-secondary">Register</a>
            <a href="/orders" className="btn btn-success">Orders</a>
          </div>
        </div>
      </div>
  )
}

export default App
