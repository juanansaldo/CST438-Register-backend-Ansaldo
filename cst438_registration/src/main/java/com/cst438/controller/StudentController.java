package com.cst438.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;

@RestController
@CrossOrigin
public class StudentController {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;
    
    @Autowired
    UserRepository userRepository;

    @GetMapping("/student" )
    public List<StudentDTO> getAllStudent() {
    	
        // Retrieve all students from the repository
        Iterable<Student> students = studentRepository.findAll();
        List<StudentDTO> studentList = new ArrayList<>();

        // Convert each Student to StudentDTO and add to the list
        for (Student s : students) {
            StudentDTO d = new StudentDTO(s.getStudent_id(), s.getName(), s.getEmail(), s.getStatusCode(), s.getStatus());
            studentList.add(d);
        }

        // Return the list of StudentDTOs
        return studentList;
    }

    @GetMapping("/student/{id}")
    public StudentDTO getStudent(@PathVariable("id") int id) {
    	
        // Find a student by their ID in the repository
        Optional<Student> studentOptional = studentRepository.findById(id);

        if (studentOptional.isPresent()) {
            // If the student is found, convert it to a StudentDTO and return
            Student student = studentOptional.get();
            return new StudentDTO(student.getStudent_id(), student.getName(), student.getEmail(), student.getStatusCode(), student.getStatus());
        } else {
            // If the student is not found, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found.");
        }
    }

    @PostMapping("/student")
    public int createStudent(@RequestBody StudentDTO studentDTO, Principal principal) {
    	
    	String email = principal.getName();
    	User user = userRepository.findByEmail(email);
    	
    	if (!user.getRole().equals("ADMIN")) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin.");
    	}
    		
        // Check for duplicates by email
        Student duplicate = studentRepository.findByEmail(studentDTO.email());

        if (duplicate != null) {
            // If a duplicate email is found, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$";
        
        if (!studentDTO.email().matches(emailRegex)) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email entered does not have valid format.");
        }

        // Create a new Student
        Student student = new Student();
        student.setEmail(studentDTO.email());
        student.setName(studentDTO.name());
        student.setStatusCode(studentDTO.statusCode());
        student.setStatus(studentDTO.status());
        
        // Save the new student to the database
        studentRepository.save(student);
        
        // Create new student user
        user = new User();
        user.setEmail(studentDTO.email());
        user.setPassword("$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue");
        user.setRole("STUDENT");
        
        // save new student user to the database
        userRepository.save(user);

        // Return the ID of the newly created student
        return student.getStudent_id();
    }

    @DeleteMapping("/student/{id}")
    public void deleteStudent(@PathVariable("id") int id, @RequestParam("force") Optional<String> force, Principal principal) {
    	
    	String email = principal.getName();
    	User user = userRepository.findByEmail(email);
    	
    	if (!user.getRole().equals("ADMIN")) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin.");
    	}
    	
    	// Check if the student exists in the repository
    	
        Optional<Student> studentOptional = studentRepository.findById(id);

        if (!studentOptional.isPresent()) {
            // If the student is not found, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found.");
        }

        Student student = studentOptional.get();

        // Check for enrollments associated with the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id);

        if (enrollments.isEmpty() || force.isPresent()) {
            // If no enrollments exist or the 'force' parameter is present, delete the student
            studentRepository.delete(student);
            userRepository.delete(user);
        } else {
            // If enrollments exist and 'force' is not present, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student has enrollments. Use 'force' parameter to delete.");
        }
    }

    @PutMapping("/student/{id}")
    public void updateStudent(@PathVariable("id") int id, @RequestBody StudentDTO studentDTO, Principal principal) {
        // Check if the student exists in the repository
    	
    	String email = principal.getName();
    	User user = userRepository.findByEmail(email);
    	
    	if (!user.getRole().equals("ADMIN")) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin.");
    	}
    	
        Optional<Student> studentOptional = studentRepository.findById(id);

        if (!studentOptional.isPresent()) {
            // If the student is not found, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found.");
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$";
        
        if (!studentDTO.email().matches(emailRegex)) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email entered does not have valid format.");
        }

        Student student = studentOptional.get();

        // Check if the email is a duplicate
        Student duplicate = studentRepository.findByEmail(studentDTO.email());

        if (duplicate != null && duplicate.getStudent_id() != id) {
            // If a duplicate email is found, throw an error
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        // Update the student details
        student.setEmail(studentDTO.email());
        student.setName(studentDTO.name());
        student.setStatusCode(studentDTO.statusCode());
        student.setStatus(studentDTO.status());

        // Save the updated student to the database
        studentRepository.save(student);
        
        // Create a new student user
        user = new User();
        user.setEmail(studentDTO.email());
        user.setPassword("$2a$10$NVM0n8ElaRgg7zWO1CxUdei7vWoPg91Lz2aYavh9.f9q0e4bRadue");
        user.setRole("STUDENT");
        
        // Save the student user to the database
        userRepository.save(user);
    }
}
