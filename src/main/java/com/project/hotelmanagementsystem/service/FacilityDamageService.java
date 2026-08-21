package com.project.hotelmanagementsystem.service;

import java.util.List;
import java.util.Map;

public interface FacilityDamageService {

    Map<String, Object> getRoomDamageInfo(String roomNumber, Integer employeeId);

    Map<String, Object> reportDamage(Map<String, Object> requestBody, Integer employeeId);
}
