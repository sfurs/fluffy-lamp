package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.CreateEmployeeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EmployeeController(restTemplate);
    }

    @Test
    void testGetAllEmployees() {
        Map<String, Object> mockResponse = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("id", "1");
        emp.put("employee_name", "John Doe");
        emp.put("employee_salary", 1000);
        emp.put("employee_age", 30);
        emp.put("employee_title", "Engineer");
        emp.put("employee_email", "john@company.com");
        data.add(emp);
        mockResponse.put("data", data);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        ResponseEntity<List<Employee>> response = controller.getAllEmployees();
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getEmployee_name());
    }

        @Test
        void testGetAllEmployeesEmptyResponse() {
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
            ResponseEntity<List<Employee>> response = controller.getAllEmployees();
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

    @Test
    void testGetEmployeeById() {
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("id", "1");
        emp.put("employee_name", "John Doe");
        emp.put("employee_salary", 1000);
        emp.put("employee_age", 30);
        emp.put("employee_title", "Engineer");
        emp.put("employee_email", "john@company.com");
        mockResponse.put("data", emp);
        when(restTemplate.getForObject(contains("/1"), eq(Map.class))).thenReturn(mockResponse);
        ResponseEntity<Employee> response = controller.getEmployeeById("1");
    assertNotNull(response.getBody());
    assertEquals("John Doe", response.getBody().getEmployee_name());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        when(restTemplate.getForObject(contains("/2"), eq(Map.class))).thenReturn(null);
        Exception exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.getEmployeeById("2"));
    assertTrue(exception instanceof org.springframework.web.server.ResponseStatusException);
    assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ((org.springframework.web.server.ResponseStatusException) exception).getStatusCode());
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Jane");
        input.setSalary(2000);
        input.setAge(25);
        input.setTitle("Manager");
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("id", "2");
        emp.put("employee_name", "Jane");
        emp.put("employee_salary", 2000);
        emp.put("employee_age", 25);
        emp.put("employee_title", "Manager");
        emp.put("employee_email", "jane@company.com");
        mockResponse.put("data", emp);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);
        ResponseEntity<Employee> response = controller.createEmployee(input);
    assertNotNull(response.getBody());
    assertEquals("Jane", response.getBody().getEmployee_name());
    }

    @Test
    void testCreateEmployeeInvalidInput() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("");
        input.setSalary(0);
        input.setAge(10);
        input.setTitle("");
        Exception exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.createEmployee(input));
    assertTrue(exception instanceof org.springframework.web.server.ResponseStatusException);
    assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ((org.springframework.web.server.ResponseStatusException) exception).getStatusCode());
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        Map<String, Object> mockResponse = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("id", "1");
        emp1.put("employee_name", "John");
        emp1.put("employee_salary", 1000);
        emp1.put("employee_age", 30);
        emp1.put("employee_title", "Engineer");
        emp1.put("employee_email", "john@company.com");
        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("id", "2");
        emp2.put("employee_name", "Jane");
        emp2.put("employee_salary", 2000);
        emp2.put("employee_age", 25);
        emp2.put("employee_title", "Manager");
        emp2.put("employee_email", "jane@company.com");
        data.add(emp1);
        data.add(emp2);
        mockResponse.put("data", data);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();
        assertEquals(2000, response.getBody());
    }

    @Test
    void testGetHighestSalaryOfEmployeesEmpty() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
        ResponseEntity<Integer> response = controller.getHighestSalaryOfEmployees();
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        Map<String, Object> mockResponse = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Map<String, Object> emp = new HashMap<>();
            emp.put("id", String.valueOf(i));
            emp.put("employee_name", "Emp" + i);
            emp.put("employee_salary", 1000 + i);
            emp.put("employee_age", 20 + i);
            emp.put("employee_title", "Title" + i);
            emp.put("employee_email", "emp" + i + "@company.com");
            data.add(emp);
        }
        mockResponse.put("data", data);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);
        ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();
    assertNotNull(response.getBody());
    assertEquals(10, response.getBody().size());
    assertEquals("Emp14", response.getBody().get(0));
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNamesEmpty() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
        ResponseEntity<List<String>> response = controller.getTopTenHighestEarningEmployeeNames();
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
    }
}
