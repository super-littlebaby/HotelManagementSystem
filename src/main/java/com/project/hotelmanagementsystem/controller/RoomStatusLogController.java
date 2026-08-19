package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间状态变更日志控制层
 * <p>
 * 负责房间状态变更日志记录的增删改查及按房间、操作人条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房间状态变更日志管理", description = "房间状态变更日志的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/room-status-logs")
public class RoomStatusLogController {

    private final RoomStatusLogService roomStatusLogService;

    /**
     * 构造函数注入房间状态变更日志Service
     *
     * @param roomStatusLogService 房间状态变更日志Service
     */
    public RoomStatusLogController(RoomStatusLogService roomStatusLogService) {
        this.roomStatusLogService = roomStatusLogService;
    }

    /**
     * 查询所有房间状态变更日志
     *
     * @return 房间状态变更日志列表
     */
    @Operation(summary = "查询所有房间状态变更日志", description = "返回系统中所有房间状态变更日志的列表")
    @GetMapping
    public ResponseResult<List<RoomStatusLog>> findAll() {
        return ResponseResult.success(roomStatusLogService.findAll());
    }

    /**
     * 根据ID查询房间状态变更日志
     *
     * @param id 日志ID
     * @return 房间状态变更日志信息，不存在返回404
     */
    @Operation(summary = "根据ID查询房间状态变更日志", description = "根据日志ID查询单条房间状态变更日志信息")
    @GetMapping("/{id}")
    public ResponseResult<RoomStatusLog> findById(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id) {
        java.util.Optional<RoomStatusLog> optional = roomStatusLogService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        return ResponseResult.success(optional.get());
    }

    /**
     * 新增房间状态变更日志
     *
     * @param roomStatusLog 房间状态变更日志信息
     * @return 创建后的日志信息
     */
    @Operation(summary = "新增房间状态变更日志", description = "创建一条新的房间状态变更日志记录")
    @PostMapping
    public ResponseResult<RoomStatusLog> create(
            @Parameter(description = "房间状态变更日志信息", required = true) @RequestBody RoomStatusLog roomStatusLog) {
        RoomStatusLog saved = roomStatusLogService.save(roomStatusLog);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新房间状态变更日志信息
     *
     * @param id             日志ID
     * @param roomStatusLog  房间状态变更日志信息
     * @return 更新后的日志信息，不存在返回404
     */
    @Operation(summary = "更新房间状态变更日志信息", description = "根据日志ID更新日志信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<RoomStatusLog> update(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房间状态变更日志信息", required = true) @RequestBody RoomStatusLog roomStatusLog) {
        java.util.Optional<RoomStatusLog> optional = roomStatusLogService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        roomStatusLog.setId(id);
        return ResponseResult.success(roomStatusLogService.save(roomStatusLog));
    }

    /**
     * 根据ID删除房间状态变更日志
     *
     * @param id 日志ID
     * @return 删除结果
     */
    @Operation(summary = "删除房间状态变更日志", description = "根据日志ID删除房间状态变更日志记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id) {
        roomStatusLogService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据房间ID查询状态变更日志列表
     *
     * @param roomId 房间ID
     * @return 状态变更日志列表
     */
    @Operation(summary = "按房间ID查询状态变更日志", description = "根据房间ID查询该房间的所有状态变更日志")
    @GetMapping("/search/byRoomId")
    public ResponseResult<List<RoomStatusLog>> findByRoomId(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId) {
        return ResponseResult.success(roomStatusLogService.findByRoomId(roomId));
    }

    /**
     * 根据操作人ID查询状态变更日志列表
     *
     * @param changedBy 操作人ID
     * @return 状态变更日志列表
     */
    @Operation(summary = "按操作人ID查询状态变更日志", description = "根据操作人ID查询其操作的所有房间状态变更日志")
    @GetMapping("/search/byChangedBy")
    public ResponseResult<List<RoomStatusLog>> findByChangedBy(
            @Parameter(description = "操作人ID", required = true) @RequestParam Integer changedBy) {
        return ResponseResult.success(roomStatusLogService.findByChangedBy(changedBy));
    }
}