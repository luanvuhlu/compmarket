import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { Cart } from '../types';
import { useAuth } from '../context/AuthContext';

const CartPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const fetchCart = async () => {
      try {
        const data = await cartService.getCart();
        setCart(data);
      } catch (err: any) {
        setError('Failed to load cart');
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [user, navigate]);

  const handleUpdateQuantity = async (itemId: number, quantity: number) => {
    try {
      const updatedCart = await cartService.updateCartItem(itemId, quantity);
      setCart(updatedCart);
    } catch (err) {
      alert('Failed to update quantity');
    }
  };

  const handleRemoveItem = async (itemId: number) => {
    try {
      const updatedCart = await cartService.removeFromCart(itemId);
      setCart(updatedCart);
    } catch (err) {
      alert('Failed to remove item');
    }
  };

  const handleClearCart = async () => {
    if (window.confirm('Are you sure you want to clear your cart?')) {
      try {
        await cartService.clearCart();
        setCart({ id: cart?.id || 0, items: [], totalPrice: 0 });
      } catch (err) {
        alert('Failed to clear cart');
      }
    }
  };

  if (loading) return <div style={{ padding: '2rem' }}>Loading...</div>;
  if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

  return (
    <div style={{ padding: '2rem', maxWidth: '1000px', margin: '0 auto' }}>
      <header style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Shopping Cart</h1>
        <Link to="/products">Continue Shopping</Link>
      </header>

      {!cart || cart.items.length === 0 ? (
        <div>
          <p>Your cart is empty.</p>
          <Link to="/products">
            <button style={{ padding: '0.5rem 1rem', marginTop: '1rem' }}>Browse Products</button>
          </Link>
        </div>
      ) : (
        <>
          <div style={{ marginBottom: '2rem' }}>
            {cart.items.map((item) => (
              <div
                key={item.id}
                style={{
                  display: 'grid',
                  gridTemplateColumns: '100px 2fr 1fr 1fr auto',
                  gap: '1rem',
                  padding: '1rem',
                  borderBottom: '1px solid #ccc',
                  alignItems: 'center',
                }}
              >
                <img
                  src={item.product.imageUrl || '/placeholder.png'}
                  alt={item.product.name}
                  style={{ width: '100px', height: '100px', objectFit: 'cover' }}
                />
                <div>
                  <h3>{item.product.name}</h3>
                  <p style={{ color: '#666' }}>{item.product.description}</p>
                </div>
                <div>
                  <p style={{ fontWeight: 'bold' }}>${item.product.price.toFixed(2)}</p>
                </div>
                <div>
                  <input
                    type="number"
                    min="1"
                    max={item.product.stockQuantity}
                    value={item.quantity}
                    onChange={(e) => handleUpdateQuantity(item.id, parseInt(e.target.value))}
                    style={{ width: '60px', padding: '0.25rem' }}
                  />
                </div>
                <button
                  onClick={() => handleRemoveItem(item.id)}
                  style={{ padding: '0.5rem 1rem', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px' }}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>

          <div style={{ borderTop: '2px solid #000', paddingTop: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <h2>Total:</h2>
              <h2>${cart.totalPrice.toFixed(2)}</h2>
            </div>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                onClick={handleClearCart}
                style={{ padding: '0.75rem 1.5rem', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px' }}
              >
                Clear Cart
              </button>
              <button
                style={{ padding: '0.75rem 1.5rem', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px' }}
              >
                Proceed to Checkout
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default CartPage;
