
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<1000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8086';
const ORDER_ID = __ENV.ORDER_ID;
const USER_ID = __ENV.USER_ID;

export default function () {
    const url = `${BASE_URL}/api/orders/${ORDER_ID}`;

    const params = {
        headers: {
            'X-User-Id': USER_ID,
        },
    };

    const response = http.get(url, params);

    check(response, {
        'status is 200': (r) => r.status === 200,
    });
}
