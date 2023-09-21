package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class JunitTestStudent {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getStudents() throws Exception {
        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders
                        .get("/student/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that the return status is OK (HTTP status code 200)
        assertEquals(200, response.getStatus());
    }

    @Test
    public void createStudent() throws Exception {
    	MockHttpServletResponse response;
        Student student = new Student();
        
        student.setName("John Doe");
        student.setEmail("john@gmail.com");

        ObjectMapper objectMapper = new ObjectMapper();
        String studentJson = objectMapper.writeValueAsString(student);

        // Send a POST request to create the student
        response = mvc.perform(
                MockMvcRequestBuilders
                        .post("/student/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson) // Set the request body
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that the return status is OK (HTTP status code 200)
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void updateStudent() throws Exception {
        MockHttpServletResponse response;
        Student student = new Student();
        
        student.setName("Juan");
        student.setEmail("Juan@gmail.com");

        response = mvc.perform(
                MockMvcRequestBuilders
                        .get("/student/2")
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that student exists
        assertEquals(200, response.getStatus());
        
        ObjectMapper objectMapper = new ObjectMapper();
        String studentJson = objectMapper.writeValueAsString(student);

        response = mvc.perform(
                MockMvcRequestBuilders
                        .put("/student/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson)
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that the return status is OK (HTTP status code 200)
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void dropStudent() throws Exception {	
        MockHttpServletResponse response = mvc.perform(
                MockMvcRequestBuilders
                        .get("/student/1")
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that student with id = 1 exists
        StudentDTO dto = fromJsonString(response.getContentAsString(), StudentDTO.class);
        boolean found = false;

        if (dto.student_id() == 1) {
            found = true;
        }
        assertTrue(found);

        // Drop student with id = 1 without using force
        response = mvc.perform(
                MockMvcRequestBuilders
                        .delete("/student/1"))
                	  .andReturn().getResponse();

        // Verify that we cannot delete without using force (HTTP status code 400)
        assertEquals(400, response.getStatus());

        // Drop student with id = 1
        response = mvc.perform(
                MockMvcRequestBuilders
                        .delete("/student/1?force=yes"))
                	  .andReturn().getResponse();

        // Verify that the return status is OK (HTTP status code 200)
        assertEquals(200, response.getStatus());

        response = mvc.perform(
                MockMvcRequestBuilders
                        .get("/student/1")
                        .accept(MediaType.APPLICATION_JSON))
                	  .andReturn().getResponse();

        // Verify that 1 is not in the student list
        assertEquals(400, response.getStatus());
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
