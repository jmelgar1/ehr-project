import { createContext, useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import instance from '../api/client.ts';
import type { AuthContextType } from '../types/AuthContextType';
import type { LoginResponse } from '../types/LoginResponse.ts';
import { UserRole } from '../types/enums/UserRole.ts'
import type { RefreshResponse } from '../types/RefreshResponse.ts';


const AuthContext  = createContext<AuthContextType | null>(null);

type AuthProviderProps = { children: ReactNode };

function AuthProvider({ children }: AuthProviderProps) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState<{ username: string; role: UserRole } | null>(null);
    const [error, setError] = useState<{ error: unknown } | null>(null);
    const isRefreshing = useRef(false);
    const refreshPromise = useRef<Promise<string> | null>(null);
    const resolveRef = useRef<((token: string) => void) | null>(null);
    const rejectRef = useRef<((error: unknown) => void) | null>(null);
    const isAuthenticated = !!token;

    const login = async (username: string, password: string) => {
        try {
            const response = await instance.post<LoginResponse>('/auth/login', { username, password });
            setToken(response.data.token);
            setUser({ username: response.data.username, role: response.data.role});
            localStorage.setItem('token', response.data.token);
        } catch (error) {
            setError({error});
            console.error('Login failed:', error);
        }
    };

    const logout = useCallback(() => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null)
    }, [])

    useEffect(() => {
        const interceptorId = instance.interceptors.response.use((response) => response,
            async (error) => {
                if(error.response.status === 401) {
                    if(isRefreshing.current) {
                        const newToken = await refreshPromise.current;
                        error.config.headers['Authorization'] = `Bearer ${newToken}`;
                        return instance.request(error.config);
                    }

                    isRefreshing.current = true;
                    refreshPromise.current = new Promise((resolve, reject) => {
                        resolveRef.current = resolve;
                        rejectRef.current = reject;
                    });

                    try {
                        const response = await instance.post<RefreshResponse>('/auth/refresh', {}, { withCredentials: true });
                        const accessToken = response.data.accessToken;
                        localStorage.setItem('token', accessToken);
                        setToken(accessToken);
                        error.config.headers['Authorization'] = `Bearer ${accessToken}`;
                        resolveRef.current!(accessToken);
                        return instance.request(error.config);
                    } catch (error) {
                        rejectRef.current!(error);
                        logout();
                        throw error;
                    } finally {
                        isRefreshing.current = false;
                        refreshPromise.current = null;
                    }
                }
            return Promise.reject(error);
        })

        return () => {
            instance.interceptors.response.eject(interceptorId)
        }
    }, [])

    return(
        <AuthContext value={{ token, user, isAuthenticated, login, logout, error }}>
            {children}
        </AuthContext>
    );
}

