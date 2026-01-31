import { createContext, useEffect, useState, type ReactNode } from 'react';
import instance from '../api/client.ts';
import type { AuthContextType } from '../types/AuthContextType';
import type { LoginResponse } from '../types/LoginResponse.ts';
import { UserRole } from '../types/enums/UserRole.ts'


const AuthContext  = createContext<AuthContextType | null>(null);

type AuthProviderProps = { children: ReactNode };

function AuthProvider({ children }: AuthProviderProps) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState<{ username: string; role: UserRole } | null>(null);
    const [error, setError] = useState<{ error: unknown } | null>(null);
    const isAuthenticated = !!token;

    useEffect(() => {
        const requestInterceptor
    })


    //we will need a useEffect in here for creating and dismanteling an interceptor

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

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null)
    }

    return(
        <AuthContext value={{ token, user, isAuthenticated, login, logout, error }}>
            {children}
        </AuthContext>
    );
}

