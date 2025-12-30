import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { productService } from '../services/productService';
import { cartService } from '../services/cartService';
import type { Product } from '../types';
import { useAuth } from '../context/AuthContext';

const ProductDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        if (id) {
          const data = await productService.getProductById(parseInt(id));
          setProduct(data);
        }
      } catch (err: any) {
        setError('Failed to load product');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    if (!product) return;

    setAddingToCart(true);
    try {
      await cartService.addToCart(product.id, quantity);
      alert('Product added to cart!');
    } catch (err: any) {
      alert('Failed to add to cart');
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) return <div style={{ padding: '2rem' }}>Loading...</div>;
  if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;
  if (!product) return <div style={{ padding: '2rem' }}>Product not found</div>;

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <Link to="/products" style={{ marginBottom: '1rem', display: 'inline-block' }}>← Back to Products</Link>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '1rem' }}>
        <div>
          {product.imageUrl && (
            <img 
              src={product.imageUrl} 
              alt={product.name} 
              style={{ width: '100%', height: '400px', objectFit: 'cover', borderRadius: '8px' }}
            />
          )}
        </div>
        
        <div>
          <h1>{product.name}</h1>
          {product.category && <p style={{ color: '#666' }}>Category: {product.category.name}</p>}
          <p style={{ fontSize: '1.5rem', fontWeight: 'bold', margin: '1rem 0' }}>
            ${product.price.toFixed(2)}
          </p>
          <p style={{ color: product.stockQuantity > 0 ? 'green' : 'red', marginBottom: '1rem' }}>
            {product.stockQuantity > 0 ? `In Stock (${product.stockQuantity} available)` : 'Out of Stock'}
          </p>
          <p style={{ marginBottom: '1rem' }}>{product.description}</p>
          
          {product.stockQuantity > 0 && (
            <div style={{ marginTop: '2rem' }}>
              <label style={{ marginRight: '1rem' }}>
                Quantity:
                <input
                  type="number"
                  min="1"
                  max={product.stockQuantity}
                  value={quantity}
                  onChange={(e) => setQuantity(parseInt(e.target.value))}
                  style={{ marginLeft: '0.5rem', padding: '0.5rem', width: '80px' }}
                />
              </label>
              <button
                onClick={handleAddToCart}
                disabled={addingToCart}
                style={{ padding: '0.75rem 1.5rem', fontSize: '1rem' }}
              >
                {addingToCart ? 'Adding...' : 'Add to Cart'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;
