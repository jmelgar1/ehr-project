export const UserRole = {
    ADMIN: 'ADMIN',
    THERAPIST: 'THERAPIST',
    NURSE: 'NURSE',
    RESEARCHER: 'RESEARCHER',
    COORDINATOR: 'COORDINATOR',
} as const;

export type UserRole = typeof UserRole[keyof typeof UserRole];