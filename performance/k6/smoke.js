import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE = 'http://localhost:8001';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '60s', target: 5 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'],
    errors: ['rate<0.01'],
  },
};

export default function () {
  const list = http.get(`${BASE}/products`);
  check(list, { 'products 200': (r) => r.status === 200 });
  errorRate.add(list.status !== 200);

  const products = list.json();
  if (products.length > 0) {
    const detail = http.get(`${BASE}/products/${products[0].id}`);
    check(detail, { 'detail 200': (r) => r.status === 200 });
    errorRate.add(detail.status !== 200);
  }

  const search = http.get(`${BASE}/products?search=shirt`);
  check(search, { 'search 200': (r) => r.status === 200 });
  errorRate.add(search.status !== 200);

  sleep(1);
}
