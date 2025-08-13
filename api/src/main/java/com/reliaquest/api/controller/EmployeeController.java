package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.CreateEmployeeInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {
    private static final String MOCK_API_BASE = "http://localhost:8112/api/v1/employee";
    private static final String DATA_KEY = "data";
    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final RestTemplate restTemplate;

    @Autowired
    public EmployeeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        logger.info("Fetching all employees");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(MOCK_API_BASE, Map.class);
            if (response == null || response.get(DATA_KEY) == null) {
                logger.warn("No employee data found");
                return ResponseEntity.ok(Collections.emptyList());
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get(DATA_KEY);
            List<Employee> employees = data.stream().map(this::mapToEmployee).filter(Objects::nonNull).collect(Collectors.toList());
            return ResponseEntity.ok(employees);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
    // No unchecked cast here
        logger.info("Searching employees by name: {}", searchString);
        try {
            List<Employee> all = getAllEmployees().getBody();
            if (all == null) return ResponseEntity.ok(Collections.emptyList());
            List<Employee> filtered = all.stream()
                .filter(e -> e.getEmployee_name() != null && e.getEmployee_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        logger.info("Fetching employee by id: {}", id);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(MOCK_API_BASE + "/" + id, Map.class);
            if (response == null || response.get(DATA_KEY) == null) {
                logger.warn("Employee not found for id: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get(DATA_KEY);
            Employee emp = mapToEmployee(data);
            if (emp == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
            return ResponseEntity.ok(emp);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited by mock API");
        } catch (Exception e) {
            logger.error("Error fetching employee by id {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("Getting highest salary");
        try {
            List<Employee> all = getAllEmployees().getBody();
            if (all == null || all.isEmpty()) return ResponseEntity.ok(0);
            Integer max = all.stream().map(Employee::getEmployee_salary).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
            return ResponseEntity.ok(max);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(0);
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info("Getting top 10 highest earning employee names");
        try {
            List<Employee> all = getAllEmployees().getBody();
            if (all == null) return ResponseEntity.ok(Collections.emptyList());
            List<String> top10 = all.stream()
                .filter(e -> e.getEmployee_salary() != null)
                .sorted((a, b) -> b.getEmployee_salary().compareTo(a.getEmployee_salary()))
                .limit(10)
                .map(Employee::getEmployee_name)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            return ResponseEntity.ok(top10);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeInput employeeInput) {
        logger.info("Creating employee: {}", employeeInput.getName());
        if (employeeInput.getName() == null || employeeInput.getName().isBlank() ||
            employeeInput.getSalary() == null || employeeInput.getSalary() <= 0 ||
            employeeInput.getAge() == null || employeeInput.getAge() < 16 || employeeInput.getAge() > 75 ||
            employeeInput.getTitle() == null || employeeInput.getTitle().isBlank()) {
            logger.warn("Invalid employee input: {}", employeeInput);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid employee input");
        }
        Map<String, Object> request = new HashMap<>();
        request.put("name", employeeInput.getName());
        request.put("salary", employeeInput.getSalary());
        request.put("age", employeeInput.getAge());
        request.put("title", employeeInput.getTitle());
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(MOCK_API_BASE, request, Map.class);
            if (response == null || response.get(DATA_KEY) == null) {
                logger.error("No response or data from mock API when creating employee");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating employee");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get(DATA_KEY);
            Employee emp = mapToEmployee(data);
            if (emp == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating employee");
            return ResponseEntity.ok(emp);
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited by mock API");
        } catch (Exception e) {
            logger.error("Error creating employee: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating employee");
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        logger.info("Deleting employee by id: {}", id);
        try {
            Employee emp = getEmployeeById(id).getBody();
            if (emp == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
            String name = emp.getEmployee_name();
            if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee name is blank");
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
    
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                MOCK_API_BASE, // <-- No /{name}
                org.springframework.http.HttpMethod.DELETE,
                entity,
                Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("data"))) {
                return ResponseEntity.ok(name);
            } else {
                logger.error("Failed to delete employee: {}", name);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to delete employee");
            }
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            logger.error("Rate limited by mock API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited by mock API");
        } catch (Exception e) {
            logger.error("Error deleting employee: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error deleting employee");
        }
    }

    private Employee mapToEmployee(Map<String, Object> data) {
    if (data == null) return null;
    Employee e = new Employee();
    e.setId((String) data.get("id"));
    e.setEmployee_name((String) data.get("employee_name"));
    e.setEmployee_salary(data.get("employee_salary") instanceof Integer ? (Integer) data.get("employee_salary") : null);
    e.setEmployee_age(data.get("employee_age") instanceof Integer ? (Integer) data.get("employee_age") : null);
    e.setEmployee_title((String) data.get("employee_title"));
    e.setEmployee_email((String) data.get("employee_email"));
    return e;
    }
}
