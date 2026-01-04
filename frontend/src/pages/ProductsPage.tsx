import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/productService';
import type { Product } from '../types';

const ProductsPage = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await productService.getAllProducts();
        setProducts(data.content);
      } catch (err: any) {
        setError('Failed to load products');
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  if (loading) return <div style={{ padding: '2rem' }}>Loading...</div>;
  if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

  return (
    <div style={{ padding: '2rem' }}>
      <header style={{ marginBottom: '2rem' }}>
        <h1>Products</h1>
        <Link to="/">Back to Home</Link>
      </header>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem' }}>
        {products.length === 0 ? (
          <p>No products available.</p>
        ) : (
          products.map((product) => (
            <div key={product.id} style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '8px' }}>
              {product.imageUrl && (
                <img 
                  src={product.imageUrl} 
                  alt={product.name} 
                  style={{ width: '100%', height: '200px', objectFit: 'cover', marginBottom: '0.5rem' }}
                />
              )}
              <h3>{product.name}</h3>
              <p>{product.description}</p>
              <p style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
                ${product.price ? product.price.toFixed(2) : '0.00'}
              </p>
              <p style={{ color: product.stockQuantity > 0 ? 'green' : 'red' }}>
                {product.stockQuantity > 0 ? `In Stock (${product.stockQuantity})` : 'Out of Stock'}
              </p>
              <Link to={`/products/${product.id}`}>
                <button style={{ width: '100%', padding: '0.5rem' }}>View Details</button>
              </Link>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ProductsPage;
