package com.example.hibernate.controller;

import com.example.hibernate.model.entities.Employee;
import com.example.hibernate.model.request.EmployeeRequest;
import com.example.hibernate.model.response.EmployeeResponse;
import com.example.hibernate.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@RestController
@AllArgsConstructor
@RequestMapping("/api/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    private String getPublicProfile(String profileName){
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        return url+"/profile/"+profileName;
    }
    @GetMapping("/get")
    public ResponseEntity<EmployeeResponse<List<Employee>>> getEmployee() {
        List<Employee> employees = employeeService.getAllEmployees();
        for(Employee employee : employees) {
            employee.setProfile(getPublicProfile(employee.getProfile()));
        }
        EmployeeResponse employeeResponse = EmployeeResponse
                .builder()
                .message("Get All Employees")
                .payload(employees)
                .status(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return new ResponseEntity<>(employeeResponse, HttpStatus.valueOf(200));
        //generate picture : localhost:8080/profile/image name
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<EmployeeResponse<Employee>> getEmployeeById(@PathVariable int id) {
        try{
            Employee employee = employeeService.getEmployeeById(id);
            employee.setProfile(getPublicProfile(employee.getProfile()));
            EmployeeResponse employeeResponse = EmployeeResponse
                    .builder()
                    .message("Get Employee")
                    .payload(employee)
                    .status(HttpStatus.OK)
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
            return new ResponseEntity<>(employeeResponse, HttpStatus.valueOf(200));
        }catch (Exception e){
            EmployeeResponse employeeResponse = EmployeeResponse
                    .builder()
                    .message("Not Found")
                    .status(HttpStatus.NOT_FOUND)
                    .build();
            return new ResponseEntity<>(employeeResponse, HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/add")
    public ResponseEntity<EmployeeResponse> addEmployee(@ModelAttribute EmployeeRequest employeeRequest) {
        System.out.println(employeeRequest.toString());
        String path_dir = "public/profile/";
        EmployeeResponse employeeResponse;
        try{
            Random rand = new Random();
            String fileName = rand.nextInt(999999)+"_"+employeeRequest.getProfile().getOriginalFilename();
            Files.copy(employeeRequest.getProfile().getInputStream(), Paths.get(path_dir + fileName));
            Employee emp = new Employee();
            emp.setName(employeeRequest.getName());
            emp.setGender(employeeRequest.getGender());
            emp.setSalary(employeeRequest.getSalary());
            emp.setProfile(fileName);
            employeeService.addEmployee(emp);
        }catch (Exception e){
            e.printStackTrace();
            employeeResponse = EmployeeResponse
                    .builder()
                    .message(e.getMessage())
                    .status(HttpStatus.valueOf(400))
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
        }
        employeeResponse = EmployeeResponse
                .builder()
                .message("Employee added successfully")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .status(HttpStatus.CREATED)
                .build();
        return new ResponseEntity<>(employeeResponse, HttpStatus.CREATED);
    }
    @PostMapping("/update/{id}")
    public ResponseEntity<EmployeeResponse<Objects>> updateEmployee(@ModelAttribute EmployeeRequest employeeRequest, @PathVariable int id) {
       String path_dir = "public/profile/";
       try{
           Employee employee = employeeService.getEmployeeById(id);
           try{
               String filename;
               if(!(employeeRequest.getProfile().getOriginalFilename().isEmpty())){
                   Random rand = new Random();
                   filename = rand.nextInt(1,999999)+"_"+employeeRequest.getProfile().getOriginalFilename();
                   Files.copy(employeeRequest.getProfile().getInputStream(), Paths.get(path_dir + filename));
               }
               else {
                   filename = employee.getProfile();
               }
               employee.setName(employeeRequest.getName());
               employee.setGender(employeeRequest.getGender());
               employee.setSalary(employeeRequest.getSalary());
               employee.setProfile(filename);
               employeeService.updateEmployee(employee);
               EmployeeResponse employeeRespons = EmployeeResponse
                       .builder()
                       .message("Employee updated successfully")
                       .status(HttpStatus.OK)
                       .timestamp(new Timestamp(System.currentTimeMillis()))
                       .build();
               return new ResponseEntity<>(employeeRespons, HttpStatus.OK);

           } catch (Exception e) {
               e.printStackTrace();
               EmployeeResponse employeeResponse = EmployeeResponse
                       .builder()
                       .message(e.getMessage())
                       .status(HttpStatus.valueOf(400))
                       .timestamp(new Timestamp(System.currentTimeMillis()))
                       .build();
               return new ResponseEntity<>(employeeResponse, HttpStatus.BAD_REQUEST);
           }
       } catch (Exception e) {
           e.printStackTrace();
           EmployeeResponse employeeResponse = EmployeeResponse
                   .builder()
                   .message("Id not found")
                   .status(HttpStatus.NOT_FOUND)
                   .timestamp(new Timestamp(System.currentTimeMillis()))
                   .build();
           return new ResponseEntity<>(employeeResponse, HttpStatus.valueOf(404));
       }

    }
    @PostMapping("/delete/{id}")
    public ResponseEntity<EmployeeResponse> deleteEmployee(@PathVariable int id) {
        try{
            Employee employee = employeeService.getEmployeeById(id);
        } catch (Exception e) {
            EmployeeResponse<Object> employeeResponse = EmployeeResponse
                    .builder()
                    .message("Id not found")
                    .status(HttpStatus.NOT_FOUND)
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();
            return new ResponseEntity<>(employeeResponse, HttpStatus.valueOf(404));
        }
        Employee employee = employeeService.getEmployeeById(id);
        String path_dir = "public/profile/" + employee.getProfile();
        if(Files.exists(Paths.get(path_dir))){
            try {
                Files.delete(Paths.get(path_dir));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        employeeService.deleteEmployee(id);
        EmployeeResponse employeeResponse = EmployeeResponse
                .builder()
                .message("Employee deleted successfully")
                .status(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return new ResponseEntity<>(employeeResponse, HttpStatus.valueOf(200));
    }
}
