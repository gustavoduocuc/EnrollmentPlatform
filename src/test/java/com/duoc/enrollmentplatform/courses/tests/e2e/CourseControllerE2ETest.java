package com.duoc.enrollmentplatform.courses.tests.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class CourseControllerE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void listCoursesReturnsSeededData() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].instructorId").exists())
                .andExpect(jsonPath("$[0].instructorName").exists())
                .andExpect(jsonPath("$[0].section").exists());
    }

    @Test
    void createsCourseAndReturns201() throws Exception {
        String body = """
                {
                  "name": "Seguridad en la Nube",
                  "section": "D",
                  "instructorId": "u-teacher-001",
                  "durationHours": 20,
                  "price": 95000
                }
                """;
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Seguridad en la Nube"))
                .andExpect(jsonPath("$.section").value("D"))
                .andExpect(jsonPath("$.instructorId").value("u-teacher-001"))
                .andExpect(jsonPath("$.instructorName").value("María González"));
    }

    @Test
    void createCourseReturns422WhenNameIsBlank() throws Exception {
        String body = """
                {
                  "name": "",
                  "section": "E",
                  "instructorId": "u-teacher-001",
                  "durationHours": 10,
                  "price": 50000
                }
                """;
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void updatesCourseName() throws Exception {
        String body = """
                {
                  "name": "Introducción a Java Actualizado"
                }
                """;
        mockMvc.perform(put("/courses/c-001").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Introducción a Java Actualizado"))
                .andExpect(jsonPath("$.section").value("A"));
    }

    @Test
    void deletesCourseWithoutEnrollments() throws Exception {
        String createBody = """
                {
                  "name": "Temporal",
                  "section": "Z",
                  "instructorId": "u-teacher-001",
                  "durationHours": 5,
                  "price": 10000
                }
                """;
        String response = mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String courseId = response.replaceAll("(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        mockMvc.perform(delete("/courses/" + courseId)).andExpect(status().isNoContent());
    }
}
