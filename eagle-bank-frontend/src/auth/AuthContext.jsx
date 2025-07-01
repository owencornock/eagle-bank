import React, { createContext, useState, useEffect } from 'react';
import api from '../api/axiosClient';
import { useNavigate } from 'react-router-dom';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const navigate = useNavigate();

    // Handle auth errors (like token expiration)
    useEffect(() => {
        const handleAuthError = () => {
            setToken(null);
            navigate('/login');
        };

        window.addEventListener('auth-error', handleAuthError);
        return () => window.removeEventListener('auth-error', handleAuthError);
    }, [navigate]);

    const login = async (email, password) => {
        try {
            const { data } = await api.post('/v1/auth/login', { email, password });
            const newToken = data.token;
            localStorage.setItem('token', newToken);
            setToken(newToken);
            navigate('/dashboard');
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    };

    const signup = async (user) => {
        try {
            await api.post('/v1/users', user);
            await login(user.email, user.password);
        } catch (error) {
            console.error('Signup failed:', error);
            if (error.response?.data?.error) {
                throw new Error(error.response.data.error);
            }
            throw new Error('Failed to create account. Please try again.');
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        navigate('/login');
    };

    return (
        <AuthContext.Provider value={{ token, login, signup, logout }}>
            {children}
        </AuthContext.Provider>
    );
}