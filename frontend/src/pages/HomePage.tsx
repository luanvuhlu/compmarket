import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const HomePage = () => {
  const { user, logout } = useAuth();

  return (
    <div style={{ padding: '2rem' }}>
      <header style={{ marginBottom: '2rem', borderBottom: '1px solid #ccc', paddingBottom: '1rem' }}>
        <h1>GearVN E-Commerce</h1>
        <nav>
          <Link to="/products" style={{ marginRight: '1rem' }}>Products</Link>
          <Link to="/cart" style={{ marginRight: '1rem' }}>Cart</Link>
          {user ? (
            <>
              {(user.role === 'ADMIN' || user.role === 'SUPER_ADMIN') && (
                <Link to="/admin" style={{ marginRight: '1rem', color: '#dc3545', fontWeight: 'bold' }}>
                  Admin Panel
                </Link>
              )}
              <span style={{ marginRight: '1rem' }}>Welcome, {user.firstName}!</span>
              <button onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ marginRight: '1rem' }}>Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </nav>
      </header>
      
      <main>
        <h2>Welcome to GearVN E-Commerce</h2>
        <p>Your one-stop shop for all your tech needs!</p>
        <Link to="/products">
          <button style={{ padding: '0.5rem 1rem', marginTop: '1rem' }}>Browse Products</button>
        </Link>
      </main>
    </div>
  );
};

export default HomePage;
