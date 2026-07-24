package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Reservation;
import com.project.hotelmanagementsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 预订信息控制层
 * <p>
 * 负责客人预订记录的增删改查及按客人、员工、状态、入住日期范围条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "预订管理", description = "预订信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 构造函数注入预订Service
     *
     * @param reservationService 预订Service
     */
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 查询所有预订
     *
     * @return 预订列表
     */
    @Operation(summary = "查询所有预订", description = "返回系统中所有预订记录的列表")
    @GetMapping
    public ResponseResult<List<Reservation>> findAll() {
        return ResponseResult.success(reservationService.findAll());
    }

    /**
     * 根据ID查询预订
     *
     * @param id 预订ID
     * @return 预订信息，不存在返回404
     */
    @Operation(summary = "根据ID查询预订", description = "根据预订ID查询单个预订详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Reservation> findById(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id) {
        return reservationService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增预订
     *
     * @param reservation 预订信息
     * @return 创建后的预订信息
     */
    @Operation(summary = "新增预订", description = "创建一个新的预订记录")
    @PostMapping
    public ResponseResult<Reservation> create(
            @Parameter(description = "预订信息", required = true) @RequestBody Reservation reservation) {
        Reservation saved = reservationService.save(reservation);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新预订信息
     *
     * @param id          预订ID
     * @param reservation 预订信息
     * @return 更新后的预订信息，不存在返回404
     */
    @Operation(summary = "更新预订信息", description = "根据预订ID更新预订信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Reservation> update(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id,
            @Parameter(description = "预订信息", required = true) @RequestBody Reservation reservation) {
        return reservationService.findById(id)
                .map(existing -> {
                    reservation.setId(id);
                    return ResponseResult.success(reservationService.save(reservation));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除预订
     *
     * @param id 预订ID
     * @return 删除结果
     */
    @Operation(summary = "删除预订", description = "根据预订ID删除预订记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id) {
        reservationService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据客人ID查询预订列表
     *
     * @param guestId 客人ID
     * @return 预订列表
     */
    @Operation(summary = "按客人ID查询预订", description = "根据客人ID查询该客人的所有预订记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<Reservation>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId) {
        return ResponseResult.success(reservationService.findByGuestId(guestId));
    }

    /**
     * 根据状态查询预订列表
     *
     * @param status 预订状态
     * @return 预订列表
     */
    @Operation(summary = "按状态查询预订", description = "根据预订状态查询预订列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<Reservation>> findByStatus(
            @Parameter(description = "预订状态", required = true) @RequestParam String status) {
        return ResponseResult.success(reservationService.findByStatus(status));
    }

    /**
     * 根据员工ID查询预订列表
     *
     * @param employeeId 员工ID
     * @return 预订列表
     */
    @Operation(summary = "按员工ID查询预订", description = "根据操作员工ID查询其处理的预订记录")
    @GetMapping("/search/byEmployeeId")
    public ResponseResult<List<Reservation>> findByEmployeeId(
            @Parameter(description = "员工ID", required = true) @RequestParam Integer employeeId) {
        return ResponseResult.success(reservationService.findByEmployeeId(employeeId));
    }

    /**
     * 根据客人ID和状态查询预订列表
     *
     * @param guestId 客人ID
     * @param status  预订状态
     * @return 预订列表
     */
    @Operation(summary = "按客人ID和状态查询预订", description = "根据客人ID和预订状态联合查询预订列表")
    @GetMapping("/search/byGuestIdAndStatus")
    public ResponseResult<List<Reservation>> findByGuestIdAndStatus(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId,
            @Parameter(description = "预订状态", required = true) @RequestParam String status) {
        return ResponseResult.success(reservationService.findByGuestIdAndStatus(guestId, status));
    }

    /**
     * 根据入住日期范围查询预订列表
     *
     * @param checkInDate  入住开始日期
     * @param checkOutDate 入住结束日期
     * @return 预订列表
     */
    @Operation(summary = "按入住日期范围查询预订", description = "根据入住日期范围查询落在区间内的预订列表")
    @GetMapping("/search/byCheckInDateBetween")
    public ResponseResult<List<Reservation>> findByCheckInDateBetween(
            @Parameter(description = "入住开始日期(yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @Parameter(description = "入住结束日期(yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {
        return ResponseResult.success(reservationService.findByCheckInDateBetween(checkInDate, checkOutDate));
    }
}