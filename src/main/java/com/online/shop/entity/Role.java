package com.online.shop.entity;

import java.util.*;
import java.util.stream.Collectors;

public enum Role {
    USER("USER"),
    OPERATOR("OPERATOR"),
    ADMIN("ADMIN");

    private final String roleName;
    private static final HashSet<String> availableRoles = new HashSet<>();
    private static final HashMap<String, Role> availableRolesMap = new HashMap<>();

    static {
        for(Role role : Role.values()) {
            availableRoles.add(role.getRole());
            availableRolesMap.put(role.getRole(), role);
        }
    }

    public static Role getRoleObject(String role) {
        return availableRolesMap.get(role);
    }

    public static boolean isValidRole(String role) {
        return role != null && availableRoles.contains(role);
    }

    public static List<String> getRoles() {
        return Arrays.stream(Role.values()).map(Role::getRole).collect(Collectors.toList());
    }

    public String getRole() {
        return roleName;
    }

    Role(String roleName) {
        this.roleName = roleName;
    }
}
