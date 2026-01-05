import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const AdminLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <aside style={{
        width: '250px',
        backgroundColor: '#2c3e50',
        color: 'white',
        padding: '1rem'
      }}>
        <h2 style={{ marginBottom: '2rem', color: 'white' }}>Admin Panel</h2>
        <nav>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            <li style={{ marginBottom: '1rem' }}>
              <Link to="/admin" style={{ color: 'white', textDecoration: 'none', display: 'block', padding: '0.5rem' }}>
                📊 Dashboard
              </Link>
            </li>
            <li style={{ marginBottom: '1rem' }}>
              <Link to="/admin/products" style={{ color: 'white', textDecoration: 'none', display: 'block', padding: '0.5rem' }}>
                📦 Products
              </Link>
            </li>
            <li style={{ marginBottom: '1rem' }}>
              <Link to="/admin/orders" style={{ color: 'white', textDecoration: 'none', display: 'block', padding: '0.5rem' }}>
                🛒 Orders
              </Link>
            </li>
            <li style={{ marginBottom: '1rem' }}>
              <Link to="/admin/users" style={{ color: 'white', textDecoration: 'none', display: 'block', padding: '0.5rem' }}>
                👥 Users
              </Link>
            </li>
            <li style={{ marginBottom: '1rem', borderTop: '1px solid #34495e', paddingTop: '1rem' }}>
              <Link to="/" style={{ color: 'white', textDecoration: 'none', display: 'block', padding: '0.5rem' }}>
                🏠 Back to Store
              </Link>
            </li>
          </ul>
        </nav>
      </aside>

      {/* Main Content */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <header style={{
          backgroundColor: 'white',
          borderBottom: '1px solid #ddd',
          padding: '1rem 2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <h1 style={{ margin: 0 }}>E-Commerce Admin</h1>
          <div>
            <span style={{ marginRight: '1rem' }}>Welcome, {user?.firstName}!</span>
            <button onClick={handleLogout} style={{ padding: '0.5rem 1rem' }}>Logout</button>
          </div>
        </header>

        {/* Page Content */}
        <main style={{ flex: 1, padding: '2rem', backgroundColor: '#f5f5f5' }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
