import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const listDuration = new Trend('product_list_duration');
const BASE = 'http://localhost:8001';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '3m', target: 20 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    errors: ['rate<0.02'],
    product_list_duration: ['p(95)<400'],
  },
};

export default function () {
  const res = http.get(`${BASE}/products`);
  listDuration.add(res.timings.duration);
  check(res, { 'products 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  sleep(Math.random() * 2 + 1);
}
