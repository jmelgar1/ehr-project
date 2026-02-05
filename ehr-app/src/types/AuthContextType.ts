export type AuthContextType = {
    token: string | null;
    user: { username: string; role: string } | null;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<boolean>;
    logout: () => void;
    error: unknown | null;
}