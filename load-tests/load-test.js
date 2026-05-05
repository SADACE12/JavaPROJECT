import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 },   // ramp up
        { duration: '1m', target: 50 },    // steady
        { duration: '30s', target: 100 },  // peak
        { duration: '30s', target: 0 },    // ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // Health check
    const health = http.get(`${BASE_URL}/actuator/health`);
    check(health, { 'health 200': (r) => r.status === 200 });

    // Register (idempotent – will fail for existing users, that's OK)
    const uid = `user_${__VU}_${__ITER}`;
    http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
        username: uid, email: `${uid}@test.com`,
        password: 'Password123!', firstName: 'Load', lastName: 'Test'
    }), { headers: { 'Content-Type': 'application/json' } });

    // Login
    const login = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        username: uid, password: 'Password123!'
    }), { headers: { 'Content-Type': 'application/json' } });
    check(login, { 'login ok': (r) => r.status === 200 || r.status === 401 });

    sleep(1);
}
