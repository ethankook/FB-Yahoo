import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import LeaguesPage from './pages/LeaguesPage';
import DashboardPage from './pages/DashboardPage';
import './styles/global.css';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/leagues" element={
            <ProtectedRoute><LeaguesPage /></ProtectedRoute>
          } />
          <Route path="/leagues/:leagueKey" element={
            <ProtectedRoute><DashboardPage /></ProtectedRoute>
          } />
          <Route path="*" element={<Navigate to="/leagues" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
