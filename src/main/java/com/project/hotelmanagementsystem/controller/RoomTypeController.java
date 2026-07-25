package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.impl.RoomTypeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "房型管理", description = "房型信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final RoomTypeServiceImpl roomTypeService;
    private final DataIsolationService dataIsolationService;

    public RoomTypeController(RoomTypeServiceImpl roomTypeService, DataIsolationService dataIsolationService) {
        this.roomTypeService = roomTypeService;
        this.dataIsolationService = dataIsolationService;
    }

    @Operation(summary = "查询所有房型", description = "返回系统中所有房型的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<Map<String, Object>>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<RoomType> roomTypes;
        if (dataIsolationService.isGroupAdmin(employee)) {
            roomTypes = roomTypeService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            roomTypes = roomTypeService.findByHotelId(hotelId);
        }
        return ResponseResult.success(roomTypeService.convertToDTOList(roomTypes));
    }

    @Operation(summary = "根据ID查询房型", description = "根据房型ID查询单个房型详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> findById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        RoomType roomType = roomTypeOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, roomType.getHotelId())) {
            return ResponseResult.error(403, "无权访问该房型");
        }
        return ResponseResult.success(roomTypeService.convertToDTO(roomType));
    }

    @Operation(summary = "新增房型", description = "创建一个新的房型记录")
    @PostMapping
    public ResponseResult<Map<String, Object>> create(
            @Parameter(description = "房型信息", required = true) @RequestBody RoomType roomType,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer accessibleHotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (!accessibleHotelId.equals(roomType.getHotelId())) {
                return ResponseResult.error(403, "无权创建其他酒店的房型");
            }
        }
        RoomType saved = roomTypeService.save(roomType);
        return ResponseResult.success("创建成功", roomTypeService.convertToDTO(saved));
    }

    @Operation(summary = "更新房型信息", description = "根据房型ID更新房型信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> update(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房型信息", required = true) @RequestBody RoomType roomType,
            HttpServletRequest request) {
        java.util.Optional<RoomType> existingOpt = roomTypeService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        RoomType existing = existingOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, existing.getHotelId())) {
            return ResponseResult.error(403, "无权更新该房型");
        }
        if (!dataIsolationService.isGroupAdmin(employee) && 
            !existing.getHotelId().equals(roomType.getHotelId())) {
            return ResponseResult.error(403, "无权将房型转移到其他酒店");
        }
        roomType.setId(id);
        RoomType updated = roomTypeService.save(roomType);
        return ResponseResult.success(roomTypeService.convertToDTO(updated));
    }

    @Operation(summary = "删除房型", description = "根据房型ID删除房型记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        RoomType roomType = roomTypeOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, roomType.getHotelId())) {
            return ResponseResult.error(403, "无权删除该房型");
        }
        roomTypeService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按酒店ID查询房型", description = "根据酒店ID查询该酒店下所有房型")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<Map<String, Object>>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的房型");
        }
        return ResponseResult.success(roomTypeService.convertToDTOList(roomTypeService.findByHotelId(hotelId)));
    }

    @Operation(summary = "按酒店ID和床型查询房型", description = "根据酒店ID和床型联合查询房型列表")
    @GetMapping("/search/byHotelIdAndBedType")
    public ResponseResult<List<Map<String, Object>>> findByHotelIdAndBedType(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "床型", required = true) @RequestParam String bedType,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的房型");
        }
        return ResponseResult.success(roomTypeService.convertToDTOList(roomTypeService.findByHotelIdAndBedType(hotelId, bedType)));
    }
}