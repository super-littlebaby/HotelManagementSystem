package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.service.AuthService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final Map<String, Employee> employeeTokenCache = new ConcurrentHashMap<>();
    private final Map<String, Guest> guestTokenCache = new ConcurrentHashMap<>();

    @Override
    public void storeToken(String token, Employee employee) {
        employeeTokenCache.put(token, employee);
    }

    @Override
    public Employee getEmployeeByToken(String token) {
        return employeeTokenCache.get(token);
    }

    @Override
    public void removeToken(String token) {
        employeeTokenCache.remove(token);
    }

    @Override
    public void saveGuestToken(String token, Guest guest) {
        guestTokenCache.put(token, guest);
    }

    @Override
    public Guest getGuestByToken(String token) {
        return guestTokenCache.get(token);
    }

    @Override
    public void removeGuestToken(String token) {
        guestTokenCache.remove(token);
    }
}