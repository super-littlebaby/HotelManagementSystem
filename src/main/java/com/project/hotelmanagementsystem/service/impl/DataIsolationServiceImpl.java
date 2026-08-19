package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import org.springframework.stereotype.Service;

@Service
public class DataIsolationServiceImpl implements DataIsolationService {

    @Override
    public boolean isGroupAdmin(Employee employee) {
        if (employee == null) {
            return false;
        }
        return "admin".equals(employee.getRole()) && employee.getHotelId() == null;
    }

    @Override
    public Integer getAccessibleHotelId(Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getHotelId();
    }

    @Override
    public boolean canAccessHotel(Employee employee, Integer hotelId) {
        if (employee == null) {
            return true;
        }
        if (isGroupAdmin(employee)) {
            return true;
        }
        Integer employeeHotelId = employee.getHotelId();
        return employeeHotelId != null && employeeHotelId.equals(hotelId);
    }
}