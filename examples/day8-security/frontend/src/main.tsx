import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

type Product = { id: string; name: string; price: number };
type Stock = { productId?: string; product_id?: string; available: number; reserved: number };
type Order = { id: string; product_id?: string; productId?: string; quantity: number; status: string; fail_payment?: boolean; failureReason?: string };
type Payment = { order_id?: string; orderId?: string; status: string; reason?: string };
type Notification = { order_id?: string; orderId?: string; event_type?: string; type?: string; message: string; created_at?: string; createdAt?: string };
type Reservation = { reserved: boolean };
type ApiResult<T> = { ok: true; data: T } | { ok: false; status: number; message: string };

const gatewayBase = import.meta.env.VITE_API_BASE ?? '/api';
const seedProductId = '11111111-1111-1111-1111-111111111111';

function newCorrelationId(prefix: string, id: string) {
  return `${prefix}-${id}`;
}

function App() {
  const [username, setUsername] = useState('trainer');
  const [password, setPassword] = useState('training');
  const [token, setToken] = useState(localStorage.getItem('day8-token') ?? '');
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductId, setSelectedProductId] = useState(seedProductId);
  const [productName, setProductName] = useState('Training Mouse');
  const [productPrice, setProductPrice] = useState(49.95);
  const [stock, setStock] = useState<Stock | null>(null);
  const [reservationOrderId, setReservationOrderId] = useState<string>(crypto.randomUUID());
  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [order, setOrder] = useState<Order | null>(null);
  const [orderIdLookup, setOrderIdLookup] = useState('');
  const [payments, setPayments] = useState<Payment[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [quantity, setQuantity] = useState(2);
  const [failPayment, setFailPayment] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [status, setStatus] = useState('Ready. Start by getting a token.');
  const [busy, setBusy] = useState(false);

  const authHeader = useMemo(() => token ? { Authorization: `Bearer ${token}` } : {}, [token]);
  const selectedProduct = products.find(p => p.id === selectedProductId);

  async function request<T>(path: string, options: RequestInit & { skipAuth?: boolean } = {}): Promise<ApiResult<T>> {
    try {
      const headers = new Headers(options.headers);
      headers.set('Accept', 'application/json');
      if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
      if (!options.skipAuth && authHeader.Authorization) headers.set('Authorization', authHeader.Authorization);
      const { skipAuth, ...fetchOptions } = options;
      const response = await fetch(`${gatewayBase}${path}`, { ...fetchOptions, headers });
      const text = await response.text();
      const data = text ? JSON.parse(text) : null;
      if (!response.ok) return { ok: false, status: response.status, message: data?.detail ?? data?.message ?? data?.error ?? `${response.statusText} from ${gatewayBase}${path}` };
      return { ok: true, data };
    } catch (error) {
      return { ok: false, status: 0, message: error instanceof Error ? error.message : 'Unknown error' };
    }
  }

  async function run<T>(label: string, action: () => Promise<ApiResult<T>>, onOk: (data: T) => void) {
    setBusy(true);
    setStatus(`${label}...`);
    const result = await action();
    setBusy(false);
    if (result.ok) {
      onOk(result.data);
      setStatus(`${label} succeeded.`);
    } else {
      setStatus(`${label} failed: ${result.status} ${result.message}`);
    }
  }

  async function login() {
    await run('Token request', () => request<{ accessToken: string }>('/auth/token', {
      method: 'POST', skipAuth: true, body: JSON.stringify({ username, password })
    }), data => { setToken(data.accessToken); localStorage.setItem('day8-token', data.accessToken); });
  }

  async function loadProducts() {
    await run('Load products', () => request<Product[]>('/products'), data => {
      setProducts(data);
      if (data.length && !data.some(p => p.id === selectedProductId)) setSelectedProductId(data[0].id);
    });
  }

  async function createProduct() {
    await run('Create product', () => request<Product>('/products', {
      method: 'POST', body: JSON.stringify({ name: productName, price: productPrice })
    }), data => { setSelectedProductId(data.id); void loadProducts(); });
  }

  async function updateProduct() {
    await run('Update product', () => request<Product>(`/products/${selectedProductId}`, {
      method: 'PUT', body: JSON.stringify({ id: selectedProductId, name: productName, price: productPrice })
    }), data => { setProductName(data.name); setProductPrice(Number(data.price)); void loadProducts(); });
  }

  async function deleteProduct() {
    await run('Delete product', () => request<void>(`/products/${selectedProductId}`, { method: 'DELETE' }), () => {
      setStock(null);
      void loadProducts();
    });
  }

  async function loadStock(productId = selectedProductId) {
    await run('Load inventory', () => request<Stock>(`/inventory/${productId}`), setStock);
  }

  async function reserveStock() {
    const orderId = reservationOrderId || crypto.randomUUID();
    setReservationOrderId(orderId);
    await run('Reserve stock directly', () => request<Reservation>('/inventory/reservations', {
      method: 'POST', body: JSON.stringify({ orderId, productId: selectedProductId, quantity })
    }), data => { setReservation(data); void loadStock(selectedProductId); });
  }

  async function releaseStock() {
    await run('Release direct reservation', () => request<void>(`/inventory/reservations/${reservationOrderId}`, { method: 'DELETE' }), () => {
      setReservation(null);
      setReservationOrderId(crypto.randomUUID());
      void loadStock(selectedProductId);
    });
  }

  async function createOrder() {
    const key = crypto.randomUUID();
    const correlationId = newCorrelationId(failPayment ? 'ui-fail' : 'ui-success', key);
    await run('Create order', () => request<Order>('/orders', {
      method: 'POST',
      headers: { 'Idempotency-Key': key, 'X-Correlation-ID': correlationId },
      body: JSON.stringify({ productId: selectedProductId, quantity, failPayment })
    }), data => { setOrder(data); setOrderIdLookup(data.id); setPayments([]); setNotifications([]); });
  }

  async function refreshOrder(id = order?.id || orderIdLookup) {
    if (!id) return;
    await run('Refresh order', () => request<Order>(`/orders/${id}`), data => { setOrder(data); setOrderIdLookup(data.id); });
  }

  async function cancelOrder() {
    const id = order?.id || orderIdLookup;
    if (!id) return;
    await run('Cancel order', () => request<Order>(`/orders/${id}/cancel`, { method: 'POST' }), setOrder);
  }

  async function loadPayments(id = order?.id || orderIdLookup) {
    if (!id) return;
    await run('Load payments', () => request<Payment[]>(`/payments/${id}`), setPayments);
  }

  async function loadNotifications(id = order?.id || orderIdLookup) {
    if (!id) return;
    await run('Load notifications', () => request<Notification[]>(`/notifications/${id}`), setNotifications);
  }

  useEffect(() => {
    if (!autoRefresh || !order?.id) return;
    const terminal = ['CONFIRMED', 'CANCELLED', 'REJECTED'].includes(order.status);
    const id = window.setInterval(async () => {
      const current = await request<Order>(`/orders/${order.id}`);
      if (current.ok) setOrder(current.data);
      const p = await request<Payment[]>(`/payments/${order.id}`);
      if (p.ok) setPayments(p.data);
      const n = await request<Notification[]>(`/notifications/${order.id}`);
      if (n.ok) setNotifications(n.data);
      if (terminal) window.clearInterval(id);
    }, terminal ? 5000 : 1500);
    return () => window.clearInterval(id);
  }, [autoRefresh, order?.id, order?.status, token]);

  useEffect(() => {
    if (selectedProduct) {
      setProductName(selectedProduct.name);
      setProductPrice(Number(selectedProduct.price));
    }
  }, [selectedProductId, products]);

  function clearToken() {
    setToken('');
    localStorage.removeItem('day8-token');
    setStatus('Token cleared. Business APIs should now return 401.');
  }

  return <main>
    <header className="hero">
      <div><p className="eyebrow">Spring Boot Day 8</p><h1>Secured Microservices Training UI</h1><p>Demonstrates browser → API Gateway → secured services, product CRUD, inventory reservation, Saga success, compensation, payments and notifications.</p></div>
      <div className={token ? 'pill ok' : 'pill'}>{token ? 'Token ready' : 'No token'}</div>
    </header>

    <section className="card grid two">
      <div><h2>1. Login</h2><p>The gateway issues a simple signed token. Business requests send it as a bearer token.</p>
        <label>Username<input value={username} onChange={e => setUsername(e.target.value)} /></label>
        <label>Password<input value={password} onChange={e => setPassword(e.target.value)} type="password" /></label>
        <div className="actions"><button disabled={busy} onClick={login}>Get token</button><button disabled={busy || !token} onClick={clearToken} className="secondary">Clear token</button></div>
      </div>
      <div className="status"><h2>Status</h2><p>{status}</p><small>Gateway base: {gatewayBase}</small></div>
    </section>

    <section className="card">
      <h2>2. Product CRUD</h2>
      <div className="actions"><button disabled={busy} onClick={loadProducts}>Load products</button></div>
      <div className="form-row">
        <label>Selected product<select value={selectedProductId} onChange={e => setSelectedProductId(e.target.value)}>{products.map(p => <option key={p.id} value={p.id}>{p.name} - {p.id}</option>)}</select></label>
        <label>Name<input value={productName} onChange={e => setProductName(e.target.value)} /></label>
        <label>Price<input value={productPrice} type="number" step="0.01" min="0" onChange={e => setProductPrice(Number(e.target.value))} /></label>
      </div>
      <div className="actions"><button disabled={busy} onClick={createProduct}>Create product</button><button disabled={busy || !selectedProductId} onClick={updateProduct}>Update selected</button><button disabled={busy || !selectedProductId} onClick={deleteProduct} className="danger">Delete selected</button></div>
      <Result title="Products" value={products} />
    </section>

    <section className="card">
      <h2>3. Inventory</h2><p>Product creation does not add stock because inventory is a separate bounded context. Use the seeded product for stock demos.</p>
      <div className="actions"><button disabled={busy || !selectedProductId} onClick={() => loadStock()}>Load stock</button><button disabled={busy || !selectedProductId} onClick={reserveStock}>Reserve directly</button><button disabled={busy || !reservationOrderId} onClick={releaseStock}>Release direct reservation</button></div>
      <label>Reservation order ID<input value={reservationOrderId} onChange={e => setReservationOrderId(e.target.value)} /></label>
      <div className="grid two"><Result title="Stock" value={stock} /><Result title="Direct reservation" value={reservation} /></div>
    </section>

    <section className="card">
      <h2>4. Order Saga</h2><p>Create an order through the gateway. The UI automatically polls order, payment and notification data while the Saga runs.</p>
      <div className="form-row"><label>Quantity<input value={quantity} min={1} type="number" onChange={e => setQuantity(Number(e.target.value))} /></label><label className="checkbox"><input checked={failPayment} type="checkbox" onChange={e => setFailPayment(e.target.checked)} /> Simulate payment failure</label><label className="checkbox"><input checked={autoRefresh} type="checkbox" onChange={e => setAutoRefresh(e.target.checked)} /> Auto-refresh Saga data</label></div>
      <div className="actions"><button disabled={busy || !selectedProductId} onClick={createOrder}>Create order</button><button disabled={busy || !(order?.id || orderIdLookup)} onClick={() => refreshOrder()}>Refresh order</button><button disabled={busy || !(order?.id || orderIdLookup)} onClick={cancelOrder} className="secondary">Cancel order</button><button disabled={busy || !(order?.id || orderIdLookup)} onClick={() => loadPayments()}>Load payments</button><button disabled={busy || !(order?.id || orderIdLookup)} onClick={() => loadNotifications()}>Load notifications</button></div>
      <label>Lookup order ID<input value={orderIdLookup} onChange={e => setOrderIdLookup(e.target.value)} /></label>
      <div className="grid three"><Result title="Order" value={order} /><Result title="Payments" value={payments} /><Result title="Notifications" value={notifications} /></div>
    </section>
  </main>;
}

function Result({ title, value }: { title: string; value: unknown }) {
  return <div className="result"><h3>{title}</h3><pre>{value == null || (Array.isArray(value) && value.length === 0) ? 'No data yet' : JSON.stringify(value, null, 2)}</pre></div>;
}

createRoot(document.getElementById('root')!).render(<App />);
