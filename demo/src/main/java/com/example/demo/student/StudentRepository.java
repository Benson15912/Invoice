package com.example.demo.student;

import com.example.demo.student.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {
    List<Student> findByEducationLevel(String educationLevel);
}