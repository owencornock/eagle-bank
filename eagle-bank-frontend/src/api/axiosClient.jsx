import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    // Add withCredentials for CORS with credentials
    withCredentials: true
});


// Add request interceptor
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// Add response interceptor
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 403) {
            // Clear token and trigger auth error event
            localStorage.removeItem('token');
            window.dispatchEvent(new Event('auth-error'));
        }
        return Promise.reject(error);
    }
);

export default api;