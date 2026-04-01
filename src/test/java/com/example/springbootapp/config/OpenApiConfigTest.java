package com.example.springbootapp.config;

import com.example.springbootapp.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.swagger.v3.oas.models.OpenAPI;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class, properties = {
    "jwt.secret=Test_JWT_Secret_That_Is_Long_Enough_For_256_Bits_ABCDEFGHIJKLMNOP",
    "stripe.api.key="
})
class OpenApiConfigTest {

    @Autowired(required = false)
    private OpenAPI openAPI;

    @Test
    void testOpenAPIBeanExists() {
        assertNotNull(openAPI, "OpenAPI bean should be created");
    }

    @Test
    void testOpenAPIHasInfo() {
        assertNotNull(openAPI, "OpenAPI bean should exist");
        assertNotNull(openAPI.getInfo(), "OpenAPI info should be configured");
    }

    @Test
    void testOpenAPIInfoTitle() {
        assertNotNull(openAPI.getInfo().getTitle(), "API title should be set");
        assertTrue(openAPI.getInfo().getTitle().length() > 0, "API title should not be empty");
    }

    @Test
    void testOpenAPIInfoDescription() {
        assertNotNull(openAPI.getInfo().getDescription(), "API description should be set");
        assertTrue(openAPI.getInfo().getDescription().length() > 0, "API description should not be empty");
    }

    @Test
    void testOpenAPIInfoVersion() {
        assertNotNull(openAPI.getInfo().getVersion(), "API version should be set");
        assertTrue(openAPI.getInfo().getVersion().length() > 0, "API version should not be empty");
    }

    @Test
    void testOpenAPIInfoContact() {
        assertNotNull(openAPI.getInfo().getContact(), "API contact should be configured");
        assertNotNull(openAPI.getInfo().getContact().getName(), "Contact name should be set");
    }

    @Test
    void testOpenAPIInfoLicense() {
        assertNotNull(openAPI.getInfo().getLicense(), "API license should be configured");
        assertNotNull(openAPI.getInfo().getLicense().getName(), "License name should be set");
    }
}
