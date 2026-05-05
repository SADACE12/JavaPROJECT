// Utility for API calls using JWT
const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('jwt_token');
}

function setToken(token) {
    localStorage.setItem('jwt_token', token);
}

function logout() {
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
}

async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE}${endpoint}`, options);
    
    if (response.status === 401 || response.status === 403) {
        // Token expired or unauthorized
        logout();
        throw new Error('Unauthorized');
    }
    
    if (!response.ok) {
        throw new Error(`API Error: ${response.statusText}`);
    }
    
    // Some endpoints return 204 No Content
    if (response.status === 204) return null;
    
    return await response.json();
}
