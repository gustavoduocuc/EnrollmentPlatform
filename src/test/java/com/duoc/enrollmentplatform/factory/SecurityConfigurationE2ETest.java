package com.duoc.enrollmentplatform.factory;

import com.duoc.enrollmentplatform.EnrollmentPlatformApplication;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class SecurityConfigurationDisabledE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void allowsAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk());
    }
}

@SpringBootTest(properties = {
        "enrollment.security.jwt.enabled=true",
        "AZURE_B2C_JWK_SET_URI=https://example.com/keys",
        "AZURE_B2C_AUDIENCE=test-audience",
        "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("local")
@Import({
        SecurityConfigurationEnabledE2ETest.TestJwtDecoderConfiguration.class,
        SecurityConfigurationEnabledE2ETest.MockMessagingConfiguration.class
})
class SecurityConfigurationEnabledE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsStudentToListCourses() throws Exception {
        mockMvc.perform(get("/courses").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isOk());
    }

    @Test
    void doesNotAllowStudentToCreateCourses() throws Exception {
        mockMvc.perform(post("/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cloud","section":"H","instructorId":"u-teacher-001","durationHours":10,"price":1000}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToCreateCourses() throws Exception {
        mockMvc.perform(post("/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cloud Admin","section":"I","instructorId":"u-teacher-001","durationHours":10,"price":1000}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void doesNotAllowStudentToUpdateCourses() throws Exception {
        mockMvc.perform(put("/courses/c-001")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hack"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void doesNotAllowStudentToDeleteCourses() throws Exception {
        mockMvc.perform(delete("/courses/c-001")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsStudentToCreateEnrollment() throws Exception {
        mockMvc.perform(post("/enrollments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":"s-001","courseIds":["c-001"]}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void doesNotAllowTeacherToCreateEnrollment() throws Exception {
        mockMvc.perform(post("/enrollments")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":"s-001","courseIds":["c-001"]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsTeacherToListEnrollments() throws Exception {
        mockMvc.perform(get("/enrollments").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestJwtDecoderConfiguration {

        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject("test-user")
                    .audience(List.of("test-audience"))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }

    @TestConfiguration
    static class MockMessagingConfiguration {

        @Bean
        @Primary
        EnrollmentMessagePublisher enrollmentMessagePublisher() {
            return mock(EnrollmentMessagePublisher.class);
        }
    }
}

class SecurityConfigurationFailFastTest {

    @Test
    void failsToStartApplicationWhenJwtEnabledWithoutAzureConfig() {
        assertThatThrownBy(() -> new SpringApplication(EnrollmentPlatformApplication.class)
                        .run(
                                "--spring.profiles.active=local",
                                "--enrollment.security.jwt.enabled=true"))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage(
                        "ENROLLMENT_SECURITY_JWT_ENABLED=true requires AZURE_B2C_JWK_SET_URI to be set");
    }
}
