package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间信息控制层
 * <p>
 * 负责具体房间实例的增删改查及按房型、状态、房间号条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房间管理", description = "房间信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    /**
     * 构造函数注入房间Service
     *
     * @param roomService 房间Service
     */
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 查询所有房间
     *
     * @return 房间列表
     */
    @Operation(summary = "查询所有房间", description = "返回系统中所有房间的列表")
    @GetMapping
    public ResponseResult<List<Room>> findAll() {
        return ResponseResult.success(roomService.findAll());
    }

    /**
     * 根据ID查询房间
     *
     * @param id 房间ID
     * @return 房间信息，不存在返回404
     */
    @Operation(summary = "根据ID查询房间", description = "根据房间ID查询单个房间详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Room> findById(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id) {
        return roomService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增房间
     *
     * @param room 房间信息
     * @return 创建后的房间信息
     */
    @Operation(summary = "新增房间", description = "创建一个新的房间记录")
    @PostMapping
    public ResponseResult<Room> create(
            @Parameter(description = "房间信息", required = true) @RequestBody Room room) {
        Room saved = roomService.save(room);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新房间信息
     *
     * @param id   房间ID
     * @param room 房间信息
     * @return 更新后的房间信息，不存在返回404
     */
    @Operation(summary = "更新房间信息", description = "根据房间ID更新房间信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Room> update(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房间信息", required = true) @RequestBody Room room) {
        return roomService.findById(id)
                .map(existing -> {
                    room.setId(id);
                    return ResponseResult.success(roomService.save(room));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除房间
     *
     * @param id 房间ID
     * @return 删除结果
     */
    @Operation(summary = "删除房间", description = "根据房间ID删除房间记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房间ID", required = true) @PathVariable Integer id) {
        roomService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据房型ID查询房间列表
     *
     * @param roomTypeId 房型ID
     * @return 房间列表
     */
    @Operation(summary = "按房型ID查询房间", description = "根据房型ID查询该房型下所有房间")
    @GetMapping("/search/byRoomTypeId")
    public ResponseResult<List<Room>> findByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId) {
        return ResponseResult.success(roomService.findByRoomTypeId(roomTypeId));
    }

    /**
     * 根据状态查询房间列表
     *
     * @param status 房间状态
     * @return 房间列表
     */
    @Operation(summary = "按状态查询房间", description = "根据房间状态查询房间列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<Room>> findByStatus(
            @Parameter(description = "房间状态", required = true) @RequestParam String status) {
        return ResponseResult.success(roomService.findByStatus(status));
    }

    /**
     * 根据房间号查询房间
     *
     * @param roomNumber 房间号
     * @return 房间信息，不存在返回404
     */
    @Operation(summary = "按房间号查询房间", description = "根据房间号查询单个房间信息")
    @GetMapping("/search/byRoomNumber")
    public ResponseResult<Room> findByRoomNumber(
            @Parameter(description = "房间号", required = true) @RequestParam String roomNumber) {
        return roomService.findByRoomNumber(roomNumber)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据房型ID和状态查询房间列表
     *
     * @param roomTypeId 房型ID
     * @param status     房间状态
     * @return 房间列表
     */
    @Operation(summary = "按房型ID和状态查询房间", description = "根据房型ID和房间状态联合查询房间列表")
    @GetMapping("/search/byRoomTypeIdAndStatus")
    public ResponseResult<List<Room>> findByRoomTypeIdAndStatus(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId,
            @Parameter(description = "房间状态", required = true) @RequestParam String status) {
        return ResponseResult.success(roomService.findByRoomTypeIdAndStatus(roomTypeId, status));
    }
}