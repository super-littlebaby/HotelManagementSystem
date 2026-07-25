package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import org.springframework.stereotype.Service;

@Service
public class DataIsolationServiceImpl implements DataIsolationService {

    @Override
    public boolean isGroupAdmin(Employee employee) {
        return "admin".equals(employee.getRole()) && employee.getHotelId() == null;
    }

    @Override
    public Integer getAccessibleHotelId(Employee employee) {
        return employee.getHotelId();
    }

    @Override
    public boolean canAccessHotel(Employee employee, Integer hotelId) {
        if (isGroupAdmin(employee)) {
            return true;
        }
        Integer employeeHotelId = employee.getHotelId();
        return employeeHotelId != null && employeeHotelId.equals(hotelId);
    }
}