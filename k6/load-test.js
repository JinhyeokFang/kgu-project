import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 20,
    duration: '1m',
    thresholds: {
        http_req_duration: [{
            threshold: 'p(95)<1000',
            abortOnFail: true,
        }],
    },
};

const BASE_URL = 'https://server.jinhy.uk/api/test';
const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export default function () {
    const payload = JSON.stringify({
        message: `Test message ${Date.now()}`
    });

    const writeRes = http.post(BASE_URL, payload, params);
    const readRes = http.get(`${BASE_URL}?page=0&size=10`, params);

    check(writeRes, {
        'status is 200': (r) => r.status === 200
    });

    check(readRes, {
        'status is 200': (r) => r.status === 200
    });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'result.json': JSON.stringify(data)
    };
}