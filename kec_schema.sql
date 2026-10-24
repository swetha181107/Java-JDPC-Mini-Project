-- KEC Student Academic & Continuous Evaluation System
-- SAFE SETUP: does not DROP tables or delete existing records.
CREATE DATABASE IF NOT EXISTS kec_academic_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kec_academic_system;

CREATE TABLE IF NOT EXISTS department (
  dept_code VARCHAR(10) PRIMARY KEY,
  dept_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
  roll_no VARCHAR(20) PRIMARY KEY,
  student_name VARCHAR(100) NOT NULL,
  dept_code VARCHAR(10) NOT NULL,
  current_semester INT NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  CONSTRAINT fk_students_department FOREIGN KEY (dept_code) REFERENCES department(dept_code),
  CONSTRAINT chk_student_sem CHECK (current_semester BETWEEN 1 AND 8)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courses (
  course_code VARCHAR(20) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  credits DECIMAL(3,1) NOT NULL,
  course_type ENUM('THEORY','PRACTICAL','EMBEDDED') NOT NULL,
  dept_code VARCHAR(10) NOT NULL,
  CONSTRAINT fk_courses_department FOREIGN KEY (dept_code) REFERENCES department(dept_code),
  CONSTRAINT chk_course_credits CHECK (credits > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_no VARCHAR(20) NOT NULL,
  course_code VARCHAR(20) NOT NULL,
  semester INT NOT NULL,
  classes_held INT NOT NULL DEFAULT 0,
  classes_attended INT NOT NULL DEFAULT 0,
  medical_approval BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_enroll_student FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
  CONSTRAINT fk_enroll_course FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
  CONSTRAINT uq_student_course_sem UNIQUE (roll_no,course_code,semester),
  CONSTRAINT chk_enroll_sem CHECK (semester BETWEEN 1 AND 8),
  CONSTRAINT chk_held CHECK (classes_held >= 0),
  CONSTRAINT chk_attended CHECK (classes_attended >= 0 AND classes_attended <= classes_held)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assessment_marks (
  mark_id INT AUTO_INCREMENT PRIMARY KEY,
  enrollment_id INT NOT NULL UNIQUE,
  cia1 DECIMAL(5,2) NOT NULL DEFAULT 0,
  cia2 DECIMAL(5,2) NOT NULL DEFAULT 0,
  assignment_mark DECIMAL(5,2) NOT NULL DEFAULT 0,
  quiz_mark DECIMAL(5,2) NOT NULL DEFAULT 0,
  lab_evaluation DECIMAL(5,2) NOT NULL DEFAULT 0,
  ese_mark DECIMAL(5,2) NOT NULL DEFAULT 0,
  ese_absent BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_marks_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
  CONSTRAINT chk_cia1 CHECK (cia1 BETWEEN 0 AND 100),
  CONSTRAINT chk_cia2 CHECK (cia2 BETWEEN 0 AND 100),
  CONSTRAINT chk_assignment CHECK (assignment_mark BETWEEN 0 AND 100),
  CONSTRAINT chk_quiz CHECK (quiz_mark BETWEEN 0 AND 100),
  CONSTRAINT chk_lab CHECK (lab_evaluation BETWEEN 0 AND 100),
  CONSTRAINT chk_ese CHECK (ese_mark BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- Idempotent sample data: INSERT IGNORE does not replace existing records.
INSERT IGNORE INTO department(dept_code,dept_name) VALUES
 ('CSE','Computer Science and Engineering'),
 ('IT','Information Technology'),
 ('ECE','Electronics and Communication Engineering');

INSERT IGNORE INTO students(roll_no,student_name,dept_code,current_semester,email,phone) VALUES
 ('24CSR001','Karthik Raj','CSE',3,'karthik@example.edu','9000000001'),
 ('24ITR001','Arun Kumar','IT',3,'arun@example.edu','9000000002'),
 ('24ITR002','Priya Devi','IT',3,'priya@example.edu','9000000003');

INSERT IGNORE INTO courses(course_code,title,credits,course_type,dept_code) VALUES
 ('24ITR31','Java Programming',4.0,'THEORY','IT'),
 ('24ITR32','Database Management System',4.0,'THEORY','IT'),
 ('24ITR33','Java Programming Lab',2.0,'PRACTICAL','IT'),
 ('24CSR31','Data Structures',4.0,'THEORY','CSE'),
 ('24ECR31','Digital Electronics',3.0,'THEORY','ECE');

INSERT IGNORE INTO enrollments(roll_no,course_code,semester,classes_held,classes_attended,medical_approval) VALUES
 ('24ITR001','24ITR31',3,40,35,FALSE),
 ('24ITR001','24ITR32',3,40,30,FALSE),
 ('24ITR001','24ITR33',3,20,18,FALSE),
 ('24ITR002','24ITR31',3,40,25,FALSE),
 ('24ITR002','24ITR32',3,40,28,TRUE),
 ('24CSR001','24CSR31',3,40,32,FALSE);

INSERT IGNORE INTO assessment_marks(enrollment_id,cia1,cia2,assignment_mark,quiz_mark,lab_evaluation,ese_mark,ese_absent)
SELECT enrollment_id,75,80,85,80,82,78,FALSE FROM enrollments
WHERE (roll_no='24ITR001' AND course_code='24ITR31' AND semester=3)
   OR (roll_no='24ITR001' AND course_code='24ITR32' AND semester=3)
   OR (roll_no='24ITR001' AND course_code='24ITR33' AND semester=3)
   OR (roll_no='24ITR002' AND course_code='24ITR31' AND semester=3)
   OR (roll_no='24ITR002' AND course_code='24ITR32' AND semester=3)
   OR (roll_no='24CSR001' AND course_code='24CSR31' AND semester=3);
