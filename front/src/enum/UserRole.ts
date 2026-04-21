export enum UserRole {
    ADMIN = "ROLE_ADMIN",
    SUPERUSER = "ROLE_SUPERUSER",
    USER = "ROLE_USER"
}

export interface Role {
  id: number
  name: UserRole
}

const roleColors: Record<UserRole, string> = {
    [UserRole.ADMIN]: "red",
    [UserRole.SUPERUSER]: "orange",
    [UserRole.USER]: "green"
};

export function getUserRoleColor(role: UserRole | undefined): string {
    console.log(role)
    if (!role) return 'red';
    return roleColors[role] || "gray";
}

export function getRuRole(role: UserRole): string {
    console.log(role)
    switch (role) {
        case UserRole.ADMIN: return "АДМИН";
        case UserRole.SUPERUSER: return "УЧИТЕЛЬ";
        case UserRole.USER: return "УЧЕНИК";
        default: return "";
    }
}
