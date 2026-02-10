import { describe, it, expect, beforeEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import client from './client';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

// Replace global localStorage with our mock
Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
});

describe('API Client', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();

    // Create new mock adapter
    mock = new MockAdapter(client);
  });

  it('should create axios instance with correct baseURL', () => {
    expect(client.defaults.baseURL).toBe('/api');
  });

  it('should include Authorization header when token exists', async () => {
    // Arrange: Set up a token in localStorage
    const mockToken = 'test-token-123';
    localStorage.setItem('token', mockToken);

    // Mock the API response
    mock.onGet('/users').reply((config) => {
      // Assert: Check that the Authorization header was added
      expect(config.headers?.['Authorization']).toBe(`Bearer ${mockToken}`);
      return [200, { users: [] }];
    });

    // Act: Make an actual request
    await client.get('/users');
  });

  it('should not include Authorization header when no token exists', async () => {
    // Arrange: No token in localStorage (already cleared in beforeEach)

    // Mock the API response
    mock.onGet('/users').reply((config) => {
      // Assert: Check that Authorization header is not present
      expect(config.headers?.['Authorization']).toBeUndefined();
      return [200, { users: [] }];
    });

    // Act: Make an actual request
    await client.get('/users');
  });

  it('should preserve existing headers when adding Authorization', async () => {
    // Arrange: Set up a token
    const mockToken = 'test-token-456';
    localStorage.setItem('token', mockToken);

    // Mock the API response
    mock.onPost('/login').reply((config) => {
      // Assert: Check that both Authorization and custom headers are present
      expect(config.headers?.['Authorization']).toBe(`Bearer ${mockToken}`);
      expect(config.headers?.['Content-Type']).toBe('application/json');
      expect(config.headers?.['Custom-Header']).toBe('custom-value');
      return [200, { success: true }];
    });

    // Act: Make a request with custom headers
    await client.post('/login', { username: 'test' }, {
      headers: {
        'Custom-Header': 'custom-value',
      },
    });
  });
});
