import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 10 },
        { duration: '30s', target: 25 },
        { duration: '30s', target: 50 },
        { duration: '20s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.10'],
        http_req_duration: ['p(95)<2000'],
    },
};

const BASE_URL = 'https://cheddar-expiring-punctured.ngrok-free.dev';

// Paste your JWT here
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3OTUzNTk5MTMsInN1YiI6InNhbG1hbmlibmFsYXZpQGdtYWlsLmNvbSIsImlhdCI6MTc5NTM1NjMxM30.Dw8uJgPwMtzLFpzruZi-JNXhDmO4ZarEgelmRTIfbQg';

function uuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

export default function () {
    const orderId = uuid();
    const idempotencyKey = uuid();

    const payload = JSON.stringify({
        orderId: orderId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Idempotency-Key': idempotencyKey,
            'Authorization': `Bearer ${TOKEN}`,
            'ngrok-skip-browser-warning': 'true',
        },
    };

    const response = http.post(
        `${BASE_URL}/api/payments`,
        payload,
        params
    );

    if (__ITER < 2) {
        console.log(`Status: ${response.status}`);
        console.log(`Response: ${response.body}`);
    }

    check(response, {
        'status is 201': (r) => r.status === 201,
    });

    sleep(1);
}