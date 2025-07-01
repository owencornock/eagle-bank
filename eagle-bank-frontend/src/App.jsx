import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './auth/AuthContext';
import SignupPage from './pages/SignUpPage';
import LoginPage  from './pages/LoginPage';
import Dashboard  from './pages/Dashboard';

function PrivateRoute({ children }) {
    const { token } = React.useContext(AuthContext);

    if (token === null) {
        return <Navigate to="/signup" />;
    }

    return children;
}

export default function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/dashboard" element={<PrivateRoute><Dashboard/></PrivateRoute>} />
                    <Route path="*" element={<Navigate to="/signup" />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}