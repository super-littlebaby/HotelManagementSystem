package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.AuthService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final Map<String, Employee> tokenCache = new ConcurrentHashMap<>();

    @Override
    public void storeToken(String token, Employee employee) {
        tokenCache.put(token, employee);
    }

    @Override
    public Employee getEmployeeByToken(String token) {
        return tokenCache.get(token);
    }

    @Override
    public void removeToken(String token) {
        tokenCache.remove(token);
    }
}