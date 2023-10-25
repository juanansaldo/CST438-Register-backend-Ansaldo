CREATE TABLE user_table (
	email varchar(25) unique,
	password varchar(100) DEFAULT NULL, 
	role varchar(25) DEFAULT NULL,
    PRIMARY KEY (email)
);

CREATE TABLE course (
  year int  NOT NULL,
  semester varchar(10) NOT NULL,
  course_id int  NOT NULL,
  section int  NOT NULL,
  title varchar(255) NOT NULL,
  times varchar(50) DEFAULT NULL,
  building varchar(20) DEFAULT NULL,
  room varchar(20) DEFAULT NULL,
  instructor varchar(50) DEFAULT NULL,
  start date DEFAULT NULL,
  end date DEFAULT NULL,
  PRIMARY KEY (course_id)
);

CREATE TABLE student (
  student_id int  NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  email varchar(255) NOT NULL UNIQUE,
  status varchar(255) DEFAULT NULL,
  status_code int  NOT NULL,
  PRIMARY KEY (student_id)
);

CREATE TABLE enrollment (
  enrollment_id int NOT NULL AUTO_INCREMENT,
  student_id int  NOT NULL,
  year int NOT NULL,
  semester varchar(10) NOT NULL,
  course_id int  NOT NULL,
  course_grade varchar(5) DEFAULT NULL,
  PRIMARY KEY (enrollment_id),
  FOREIGN KEY (course_id) REFERENCES course (course_id) on delete cascade, 
  FOREIGN KEY (student_id) REFERENCES student (student_id) on delete cascade 
);
