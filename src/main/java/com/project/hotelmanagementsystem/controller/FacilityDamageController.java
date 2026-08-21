package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.FacilityDamageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "设施损坏追责", description = "设施损坏的查询与追责处理接口")
@RestController
@RequestMapping("/api/facility-damage")
public class FacilityDamageController {

    private final FacilityDamageService facilityDamageService;
    private final DataIsolationService dataIsolationService;

    public FacilityDamageController(FacilityDamageService facilityDamageService,
                                    DataIsolationService dataIsolationService) {
        this.facilityDamageService = facilityDamageService;
        this.dataIsolationService = dataIsolationService;
    }

    @Operation(summary = "查询房间损坏信息", description = "根据房间号查询房间设施列表和当前/最近入住客人信息")
    @GetMapping("/info")
    public ResponseResult<Map<String, Object>> getRoomDamageInfo(
            @Parameter(description = "房间号", required = true) @RequestParam String roomNumber,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未登录");
        }
        try {
            Map<String, Object> info = facilityDamageService.getRoomDamageInfo(roomNumber, employee.getId());
            Integer hotelId = (Integer) info.get("hotelId");
            if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
                return ResponseResult.error(403, "无权访问该房间");
            }
            return ResponseResult.success(info);
        } catch (RuntimeException e) {
            return ResponseResult.error(404, e.getMessage());
        }
    }

    @Operation(summary = "上报设施损坏", description = "上报设施损坏信息，设置房间为维修中，并根据是否客人原因决定是否生成账单赔偿")
    @PostMapping("/report")
    public ResponseResult<Map<String, Object>> reportDamage(
            @Parameter(description = "损坏上报信息", required = true) @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未登录");
        }
        try {
            String roomNumber = (String) requestBody.get("roomNumber");
            Map<String, Object> info = facilityDamageService.getRoomDamageInfo(roomNumber, employee.getId());
            Integer hotelId = (Integer) info.get("hotelId");
            if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
                return ResponseResult.error(403, "无权操作该房间");
            }

            Map<String, Object> result = facilityDamageService.reportDamage(requestBody, employee.getId());
            return ResponseResult.success("上报成功", result);
        } catch (RuntimeException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }
}
