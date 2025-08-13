package com.reliaquest.api.model;

public class CreateEmployeeInput {
    private String name;
    private Integer salary;
    private Integer age;
    private String title;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer salary) { this.salary = salary; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
