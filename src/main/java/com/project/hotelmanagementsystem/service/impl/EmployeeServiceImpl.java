package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.repository.EmployeeRepository;
import com.project.hotelmanagementsystem.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 员工Service实现类
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee save(Employee employee) {
        if (employee.getPasswordHash() != null && !employee.getPasswordHash().isEmpty()) {
            if (!employee.getPasswordHash().startsWith("$2a$") && 
                !employee.getPasswordHash().startsWith("$2b$") && 
                !employee.getPasswordHash().startsWith("$2y$")) {
                employee.setPasswordHash(passwordEncoder.encode(employee.getPasswordHash()));
            }
        }
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelId(Integer hotelId) {
        return employeeRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByRole(String role) {
        return employeeRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelIdAndRole(Integer hotelId, String role) {
        return employeeRepository.findByHotelIdAndRole(hotelId, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelIdAndIsActive(Integer hotelId, Boolean isActive) {
        return employeeRepository.findByHotelIdAndIsActive(hotelId, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> login(String username, String password) {
        return employeeRepository.findByUsername(username)
                .filter(employee -> employee.getIsActive() != null && employee.getIsActive())
                .filter(employee -> {
                    String storedPassword = employee.getPasswordHash();
                    boolean isEncrypted = storedPassword != null && 
                        (storedPassword.startsWith("$2a$") || 
                         storedPassword.startsWith("$2b$") || 
                         storedPassword.startsWith("$2y$"));
                    if (isEncrypted) {
                        return passwordEncoder.matches(password, storedPassword);
                    }
                    return password.equals(storedPassword);
                });
    }
}