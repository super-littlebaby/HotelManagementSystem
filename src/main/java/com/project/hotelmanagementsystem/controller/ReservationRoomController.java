package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.ReservationRoom;
import com.project.hotelmanagementsystem.service.ReservationRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预订房间明细控制层
 * <p>
 * 负责预订与房间明细关联关系的增删改查及按预订、房间、房型条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "预订房间明细管理", description = "预订房间明细的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/reservation-rooms")
public class ReservationRoomController {

    private final ReservationRoomService reservationRoomService;

    /**
     * 构造函数注入预订房间明细Service
     *
     * @param reservationRoomService 预订房间明细Service
     */
    public ReservationRoomController(ReservationRoomService reservationRoomService) {
        this.reservationRoomService = reservationRoomService;
    }

    /**
     * 查询所有预订房间明细
     *
     * @return 预订房间明细列表
     */
    @Operation(summary = "查询所有预订房间明细", description = "返回系统中所有预订房间明细的列表")
    @GetMapping
    public ResponseResult<List<ReservationRoom>> findAll() {
        return ResponseResult.success(reservationRoomService.findAll());
    }

    /**
     * 根据ID查询预订房间明细
     *
     * @param id 预订房间明细ID
     * @return 预订房间明细信息，不存在返回404
     */
    @Operation(summary = "根据ID查询预订房间明细", description = "根据明细ID查询单条预订房间明细信息")
    @GetMapping("/{id}")
    public ResponseResult<ReservationRoom> findById(
            @Parameter(description = "预订房间明细ID", required = true) @PathVariable Integer id) {
        return reservationRoomService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增预订房间明细
     *
     * @param reservationRoom 预订房间明细信息
     * @return 创建后的明细信息
     */
    @Operation(summary = "新增预订房间明细", description = "创建一条新的预订房间明细记录")
    @PostMapping
    public ResponseResult<ReservationRoom> create(
            @Parameter(description = "预订房间明细信息", required = true) @RequestBody ReservationRoom reservationRoom) {
        ReservationRoom saved = reservationRoomService.save(reservationRoom);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新预订房间明细
     *
     * @param id              预订房间明细ID
     * @param reservationRoom 预订房间明细信息
     * @return 更新后的明细信息，不存在返回404
     */
    @Operation(summary = "更新预订房间明细", description = "根据明细ID更新预订房间明细信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<ReservationRoom> update(
            @Parameter(description = "预订房间明细ID", required = true) @PathVariable Integer id,
            @Parameter(description = "预订房间明细信息", required = true) @RequestBody ReservationRoom reservationRoom) {
        return reservationRoomService.findById(id)
                .map(existing -> {
                    reservationRoom.setId(id);
                    return ResponseResult.success(reservationRoomService.save(reservationRoom));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除预订房间明细
     *
     * @param id 预订房间明细ID
     * @return 删除结果
     */
    @Operation(summary = "删除预订房间明细", description = "根据明细ID删除预订房间明细记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "预订房间明细ID", required = true) @PathVariable Integer id) {
        reservationRoomService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据预订ID查询预订房间明细列表
     *
     * @param reservationId 预订ID
     * @return 预订房间明细列表
     */
    @Operation(summary = "按预订ID查询房间明细", description = "根据预订ID查询该预订下的所有房间明细")
    @GetMapping("/search/byReservationId")
    public ResponseResult<List<ReservationRoom>> findByReservationId(
            @Parameter(description = "预订ID", required = true) @RequestParam Integer reservationId) {
        return ResponseResult.success(reservationRoomService.findByReservationId(reservationId));
    }

    /**
     * 根据房间ID查询预订房间明细列表
     *
     * @param roomId 房间ID
     * @return 预订房间明细列表
     */
    @Operation(summary = "按房间ID查询预订房间明细", description = "根据房间ID查询关联该房间的预订明细列表")
    @GetMapping("/search/byRoomId")
    public ResponseResult<List<ReservationRoom>> findByRoomId(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId) {
        return ResponseResult.success(reservationRoomService.findByRoomId(roomId));
    }

    /**
     * 根据房型ID查询预订房间明细列表
     *
     * @param roomTypeId 房型ID
     * @return 预订房间明细列表
     */
    @Operation(summary = "按房型ID查询预订房间明细", description = "根据房型ID查询关联该房型的预订明细列表")
    @GetMapping("/search/byRoomTypeId")
    public ResponseResult<List<ReservationRoom>> findByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId) {
        return ResponseResult.success(reservationRoomService.findByRoomTypeId(roomTypeId));
    }
}