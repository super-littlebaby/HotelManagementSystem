package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Employee;

public interface DataIsolationService {

    boolean isGroupAdmin(Employee employee);

    Integer getAccessibleHotelId(Employee employee);

    boolean canAccessHotel(Employee employee, Integer hotelId);
}