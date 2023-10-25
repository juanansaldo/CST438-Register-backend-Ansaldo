package com.cst438.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.service.GradebookService;
import java.security.Principal;

@RestController
@CrossOrigin 
public class ScheduleController {
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	StudentRepository studentRepository;
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	GradebookService gradebookService;
	
	@Autowired
	UserRepository userRepository;
	
	/*
	 * get current schedule for student.
	 */
	@GetMapping("/schedule")
	public ScheduleDTO[] getSchedule(Principal principal, @RequestParam("year") int year, @RequestParam("semester") String semester) {
		
		System.out.println("/schedule called.");
		
		Student student = studentRepository.findByEmail(principal.getName());
		if (student != null) {
			System.out.println("/schedule student "+student.getName()+" "+student.getStudent_id());
			List<Enrollment> enrollments = enrollmentRepository.findStudentSchedule(principal.getName(), year, semester);
			ScheduleDTO[] sched = createSchedule(year, semester, student, enrollments);
			return sched;
		} else {
			return new ScheduleDTO[0];   // return empty schedule for unknown student.
		}
	}
	/*
	 * add a course for a student
	 */
	@PostMapping("/schedule/course/{id}")
	@Transactional
	public ScheduleDTO addCourse(Principal principal, @PathVariable int id) {
		
		String email = principal.getName();
    	User user = userRepository.findByEmail(email);
    	
    	if (!user.getRole().equals("STUDENT")) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student.");
    	}
		
		Student student = studentRepository.findByEmail(principal.getName());
		Course course  = courseRepository.findById(id).orElse(null);
		
		// student.status
		// = 0  ok to register.  != 0 registration is on hold.		
		if (student!= null && course!=null && student.getStatusCode()==0) {
			// TODO check that today's date is not past add deadline for the course.
			Enrollment enrollment = new Enrollment();
			enrollment.setStudent(student);
			enrollment.setCourse(course);
			enrollment.setYear(course.getYear());
			enrollment.setSemester(course.getSemester());
			enrollmentRepository.save(enrollment);
			// notify grade book of new enrollment event
			gradebookService.enrollStudent(principal.getName(), student.getName(), course.getCourse_id());
			ScheduleDTO result = createSchedule(enrollment);
			return result;
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Course_id invalid or student not allowed to register for the course.  "+id);
		}	
	}
	/*
	 * drop a course from student schedule
	 */
	@DeleteMapping("/schedule/{enrollment_id}")
	@Transactional
	public void dropCourse(Principal principal, @PathVariable int enrollment_id) {
		
		String email = principal.getName();
		User user = userRepository.findByEmail(email);
    	
    	if (!user.getRole().equals("STUDENT")) {
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student.");
    	}
		
		// TODO  check that today's date is not past deadline to drop course.
		Enrollment enrollment = enrollmentRepository.findById(enrollment_id).orElse(null);
		// verify that student is enrolled in the course.
		if (enrollment!=null && enrollment.getStudent().getEmail().equals(principal.getName())) {
			// OK.  drop the course.
			 enrollmentRepository.delete(enrollment);
		} else {
			// something is not right with the enrollment.  
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Enrollment_id invalid. "+enrollment_id);
		}
	}
	
	/* 
	 * helper method to transform course, enrollment, student entities into 
	 * a an instances of ScheduleDTO to return to front end.
	 * This makes the front end less dependent on the details of the database.
	 */
	private ScheduleDTO[] createSchedule(int year, String semester, Student s, List<Enrollment> enrollments) {
		ScheduleDTO[] result = new ScheduleDTO[enrollments.size()];
		for (int i=0; i<enrollments.size(); i++) {
			ScheduleDTO dto =createSchedule(enrollments.get(i));
			result[i] = dto;
		}
		return result;
	}
	
	private ScheduleDTO createSchedule(Enrollment e) {
		Course c = e.getCourse();
		ScheduleDTO dto = new ScheduleDTO(
		   e.getEnrollment_id(),
		   c.getCourse_id(),
		   c.getSection(),
		   c.getTitle(),
		   c.getTimes(),
		   c.getBuilding(),
		   c.getRoom(),
		   c.getInstructor(),
		   c.getStart().toString(),
		   c.getEnd().toString(),
		   e.getCourseGrade());
		   
		return dto;
	}
}
