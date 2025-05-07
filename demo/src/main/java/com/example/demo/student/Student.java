package com.example.demo.student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "students")
public class Student {
    @Id
    private String id;
    private String name;
    private String educationLevel;
    private List<String> subjects;
    private Map<String, Integer> days; // e.g., {"Monday": 2}
    private double ratePerLesson;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public Map<String, Integer> getDays() {
        return days;
    }

    public void setDays(Map<String, Integer> days) {
        this.days = days;
    }

    public double getRatePerLesson() {
        return ratePerLesson;
    }

    public void setRatePerLesson(double ratePerLesson) {
        this.ratePerLesson = ratePerLesson;
    }
}