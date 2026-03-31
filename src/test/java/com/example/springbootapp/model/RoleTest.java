package com.example.springbootapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {
    @Test
    public void roleEnumValues() {
        Role[] roles = Role.values();
        assertEquals(2, roles.length);
        assertEquals(Role.ROLE_USER, roles[0]);
        assertEquals(Role.ROLE_ADMIN, roles[1]);
    }

    @Test
    public void roleEnumValueOf() {
        Role role1 = Role.valueOf("ROLE_USER");
        assertEquals(Role.ROLE_USER, role1);
        
        Role role2 = Role.valueOf("ROLE_ADMIN");
        assertEquals(Role.ROLE_ADMIN, role2);
    }
}
