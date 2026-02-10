import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate('/leagues', { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate]);

  const handleLogin = () => {
    const returnPath = sessionStorage.getItem('returnPath') || '/leagues';
    const returnTo = `${window.location.origin}${returnPath}`;
    window.location.href = `/oauth/yahoo/login?returnTo=${encodeURIComponent(returnTo)}`;
  };

  if (isLoading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="login-page">
      <h1>Fantasy Basketball Helper</h1>
      <p className="subtitle">Analyze your Yahoo Fantasy Basketball league</p>
      <button className="login-btn" onClick={handleLogin}>
        Login with Yahoo
      </button>
    </div>
  );
}
