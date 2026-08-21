package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "房间管理", description = "房间信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final DataIsolationService dataIsolationService;

    public RoomController(RoomService roomService, DataIsolationService dataIsolationService) {
        this.roomService = roomService;
        this.dataIsolationService = dataIsolationService;
    }

    @Operation(summary = "查询所有房间", description = "返回系统中所有房间的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<Map<String, Object>>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Room> rooms;
        if (dataIsolationService.isGroupAdmin(employee)) {
            rooms = roomService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            rooms = roomService.findByHotelId(hotelId);
        }
        return ResponseResult.success(roomService.convertToDTOList(rooms));
    }

    @Operation(summary = "根据ID查询房间", description = "根据房间ID查询单个房间详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> findById(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Room> roomOpt = roomService.findById(id);
        if (roomOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Room room = roomOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, room.getHotelId())) {
            return ResponseResult.error(403, "无权访问该房间");
        }
        return ResponseResult.success(roomService.convertToDTO(room));
    }

    @Operation(summary = "新增房间", description = "创建一个新的房间记录")
    @PostMapping
    public ResponseResult<Map<String, Object>> create(
            @Parameter(description = "房间信息", required = true) @Valid @RequestBody Room room,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer accessibleHotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (!accessibleHotelId.equals(room.getHotelId())) {
                return ResponseResult.error(403, "无权创建其他酒店的房间");
            }
        }
        room.setStatus("vacant");
        Room saved = roomService.save(room);
        return ResponseResult.success("创建成功", roomService.convertToDTO(saved));
    }

    @Operation(summary = "批量新增房间", description = "根据起始房间号和数量批量创建房间")
    @PostMapping("/batch")
    public ResponseResult<List<Map<String, Object>>> batchCreate(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");

        Integer hotelId = (Integer) requestBody.get("hotelId");
        Integer roomTypeId = (Integer) requestBody.get("roomTypeId");
        String startRoomNumber = (String) requestBody.get("startRoomNumber");
        Integer count = (Integer) requestBody.get("count");
        Integer floor = (Integer) requestBody.get("floor");
        String notes = (String) requestBody.getOrDefault("notes", "");

        if (hotelId == null || roomTypeId == null || startRoomNumber == null || count == null || count <= 0) {
            return ResponseResult.error(400, "缺少必要参数：hotelId、roomTypeId、startRoomNumber、count");
        }
        if (count > 200) {
            return ResponseResult.error(400, "单次批量添加不能超过200个房间");
        }
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer accessibleHotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (!accessibleHotelId.equals(hotelId)) {
                return ResponseResult.error(403, "无权创建其他酒店的房间");
            }
        }

        List<Map<String, Object>> createdRooms = new java.util.ArrayList<>();
        int successCount = 0;
        StringBuilder failedNumbers = new StringBuilder();

        try {
            int baseNumber = Integer.parseInt(startRoomNumber);
            for (int i = 0; i < count; i++) {
                String roomNumber = String.valueOf(baseNumber + i);
                if (roomService.findByRoomNumber(roomNumber).isPresent()) {
                    if (failedNumbers.length() > 0) failedNumbers.append(", ");
                    failedNumbers.append(roomNumber);
                    continue;
                }
                Room room = new Room();
                room.setHotelId(hotelId);
                room.setRoomNumber(roomNumber);
                room.setFloor(floor != null ? floor : 1);
                room.setRoomTypeId(roomTypeId);
                room.setStatus("vacant");
                room.setNotes(notes);
                Room saved = roomService.save(room);
                createdRooms.add(roomService.convertToDTO(saved));
                successCount++;
            }
        } catch (NumberFormatException e) {
            return ResponseResult.error(400, "起始房间号必须是数字");
        }

        String message = String.format("成功创建 %d 个房间", successCount);
        if (failedNumbers.length() > 0) {
            message += "，以下房间号已存在跳过：" + failedNumbers;
        }
        return ResponseResult.success(message, createdRooms);
    }

    @Operation(summary = "更新房间信息", description = "根据房间ID更新房间信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> update(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房间信息", required = true) @Valid @RequestBody Room room,
            HttpServletRequest request) {
        java.util.Optional<Room> existingOpt = roomService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Room existing = existingOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, existing.getHotelId())) {
            return ResponseResult.error(403, "无权更新该房间");
        }
        if (!dataIsolationService.isGroupAdmin(employee) && 
            !existing.getHotelId().equals(room.getHotelId())) {
            return ResponseResult.error(403, "无权将房间转移到其他酒店");
        }
        room.setId(id);
        Room updated = roomService.save(room);
        return ResponseResult.success(roomService.convertToDTO(updated));
    }

    @Operation(summary = "删除房间", description = "根据房间ID删除房间记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Room> roomOpt = roomService.findById(id);
        if (roomOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Room room = roomOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, room.getHotelId())) {
            return ResponseResult.error(403, "无权删除该房间");
        }
        roomService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按房型ID查询房间", description = "根据房型ID查询该房型下所有房间")
    @GetMapping("/search/byRoomTypeId")
    public ResponseResult<List<Map<String, Object>>> findByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId,
            HttpServletRequest request) {
        List<Room> rooms = roomService.findByRoomTypeId(roomTypeId);
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            rooms = rooms.stream().filter(r -> r.getHotelId().equals(hotelId)).toList();
        }
        return ResponseResult.success(roomService.convertToDTOList(rooms));
    }

    @Operation(summary = "按状态查询房间", description = "根据状态查询房间列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<Map<String, Object>>> findByStatus(
            @Parameter(description = "房间状态", required = true) @RequestParam String status,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Room> rooms;
        if (dataIsolationService.isGroupAdmin(employee)) {
            rooms = roomService.findByStatus(status);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            rooms = roomService.findByHotelIdAndStatus(hotelId, status);
        }
        return ResponseResult.success(roomService.convertToDTOList(rooms));
    }

    @Operation(summary = "按房间号查询房间", description = "根据房间号查询单个房间")
    @GetMapping("/search/byRoomNumber")
    public ResponseResult<Map<String, Object>> findByRoomNumber(
            @Parameter(description = "房间号", required = true) @RequestParam String roomNumber,
            HttpServletRequest request) {
        java.util.Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Room room = roomOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, room.getHotelId())) {
            return ResponseResult.error(403, "无权访问该房间");
        }
        return ResponseResult.success(roomService.convertToDTO(room));
    }

    @Operation(summary = "按酒店ID查询房间", description = "根据酒店ID查询该酒店下所有房间")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<Map<String, Object>>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的房间");
        }
        return ResponseResult.success(roomService.convertToDTOList(roomService.findByHotelId(hotelId)));
    }

    @Operation(summary = "按酒店ID和状态查询房间", description = "根据酒店ID和状态联合查询房间列表")
    @GetMapping("/search/byHotelIdAndStatus")
    public ResponseResult<List<Map<String, Object>>> findByHotelIdAndStatus(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "状态", required = true) @RequestParam String status,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的房间");
        }
        return ResponseResult.success(roomService.convertToDTOList(roomService.findByHotelIdAndStatus(hotelId, status)));
    }

    @Operation(summary = "更新房间状态", description = "更新房间状态并记录状态变更日志")
    @PutMapping("/{id}/status")
    public ResponseResult<Map<String, Object>> updateStatus(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        java.util.Optional<Room> roomOpt = roomService.findById(id);
        if (roomOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Room room = roomOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, room.getHotelId())) {
            return ResponseResult.error(403, "无权更新该房间状态");
        }
        String newStatus = (String) requestBody.get("status");
        Integer changedBy = employee.getId();
        String notes = (String) requestBody.getOrDefault("notes", "");
        // 设置为维修中时，必须填写备注说明损坏/维修原因
        if ("out_of_order".equals(newStatus) && (notes == null || notes.trim().isEmpty())) {
            return ResponseResult.error(400, "房间设置为维修中时必须填写备注说明");
        }
        Room updated = roomService.updateStatus(id, newStatus, changedBy, notes);
        return ResponseResult.success(roomService.convertToDTO(updated));
    }
}