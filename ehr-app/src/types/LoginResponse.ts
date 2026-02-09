import { UserRole } from './enums/UserRole.ts'

export type LoginResponse = {
    token: string;
    username: string;
    role: UserRole;
}