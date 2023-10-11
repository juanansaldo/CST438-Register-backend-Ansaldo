package com.cst438.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cst438.domain.FinalGradeDTO;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;

@Service
@ConditionalOnProperty(prefix = "gradebook", name = "service", havingValue = "rest")
@RestController
public class GradebookServiceREST implements GradebookService {

	private RestTemplate restTemplate = new RestTemplate();

	@Value("${gradebook.url}")
	private String gradebook_url;

	@Override
	public void enrollStudent(String student_email, String student_name, int course_id) {
		System.out.println("Start Message "+ student_email +" " + course_id); 

        // use RestTemplate to send a message to the gradebook service
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO(0, student_email, student_name, course_id);

        restTemplate.postForObject(gradebook_url, enrollmentDTO, EnrollmentDTO.class);
	}
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	/*
	 * endpoint for final course grades
	 */
	@PutMapping("/course/{course_id}")
	@Transactional
	public void updateCourseGrades( @RequestBody FinalGradeDTO[] grades, @PathVariable("course_id") int course_id) {
		System.out.println("Grades received "+grades.length);
		
		// update grades in enrollment records with grades received from the gradebook service
        for (FinalGradeDTO grade : grades) {

            Enrollment enrollment = enrollmentRepository.findByEmailAndCourseId(grade.studentEmail(), course_id);

            if (enrollment != null) {
                // Update the grade for the enrollment.
                enrollment.setCourseGrade(grade.grade());
                enrollmentRepository.save(enrollment);
            } else {
                System.err.println("Enrollment not found for student email: " + grade.studentEmail()
                        + " and course id: " + course_id);
            }
        }
	}
}
