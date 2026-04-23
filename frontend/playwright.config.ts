import { defineConfig, devices } from '@playwright/test'

const backendServer =
  process.env.WIREMOCK === 'true'
    ? {
        command:
          'java -jar ../wiremock/wiremock-standalone.jar --port 8001 --root-dir ../wiremock --no-request-journal',
        url: 'http://localhost:8001/health',
        reuseExistingServer: !process.env.CI,
        timeout: 30_000,
      }
    : {
        command:
          'cd ../backend && uv run --active python -m app.seed && uv run --active uvicorn app.main:app --host 0.0.0.0 --port 8001',
        url: 'http://localhost:8001/health',
        reuseExistingServer: !!process.env.REUSE_EXISTING_SERVER || !process.env.CI,
        timeout: 180_000,
      }

export default defineConfig({
  testDir: './e2e/tests',
  timeout: 45000, // Increase for stability
  expect: {
    timeout: 10000,
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { open: 'never' }],
    ['json', { outputFile: 'test-results.json' }],
    ['junit', { outputFile: 'test-results.xml' }],
  ],
  use: {
    baseURL: 'http://localhost:3001',
    headless: process.env.HEADED !== '1',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    testIdAttribute: 'data-testid',
    actionTimeout: 10000,
    // Add stability improvements
    timezoneId: 'UTC',
    locale: 'en-US',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  webServer: [
    backendServer,
    {
      // Use preview in CI for stability, dev locally for faster iteration
      command: process.env.CI
        ? 'npm run build && npm run preview -- --host 0.0.0.0 --port 3001 --strictPort'
        : 'npm run dev',
      url: 'http://localhost:3001',
      reuseExistingServer: !!process.env.REUSE_EXISTING_SERVER || !process.env.CI,
      timeout: 240_000, // Increased timeout for CI build step
    },
  ],
})
