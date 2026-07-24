package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入住登记控制层
 * <p>
 * 负责客人入住登记记录的增删改查及按客人、房间、预订、状态条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "入住登记管理", description = "入住登记记录的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * 构造函数注入入住登记Service
     *
     * @param checkInService 入住登记Service
     */
    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * 查询所有入住记录
     *
     * @return 入住记录列表
     */
    @Operation(summary = "查询所有入住记录", description = "返回系统中所有入住登记记录的列表")
    @GetMapping
    public ResponseResult<List<CheckIn>> findAll() {
        return ResponseResult.success(checkInService.findAll());
    }

    /**
     * 根据ID查询入住记录
     *
     * @param id 入住记录ID
     * @return 入住记录信息，不存在返回404
     */
    @Operation(summary = "根据ID查询入住记录", description = "根据入住记录ID查询单条入住登记详细信息")
    @GetMapping("/{id}")
    public ResponseResult<CheckIn> findById(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id) {
        return checkInService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增入住登记
     *
     * @param checkIn 入住记录信息
     * @return 创建后的入住记录信息
     */
    @Operation(summary = "新增入住登记", description = "创建一条新的入住登记记录")
    @PostMapping
    public ResponseResult<CheckIn> create(
            @Parameter(description = "入住记录信息", required = true) @RequestBody CheckIn checkIn) {
        CheckIn saved = checkInService.save(checkIn);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新入住登记信息
     *
     * @param id      入住记录ID
     * @param checkIn 入住记录信息
     * @return 更新后的入住记录信息，不存在返回404
     */
    @Operation(summary = "更新入住登记信息", description = "根据入住记录ID更新入住登记信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<CheckIn> update(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            @Parameter(description = "入住记录信息", required = true) @RequestBody CheckIn checkIn) {
        return checkInService.findById(id)
                .map(existing -> {
                    checkIn.setId(id);
                    return ResponseResult.success(checkInService.save(checkIn));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除入住记录
     *
     * @param id 入住记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除入住记录", description = "根据入住记录ID删除入住登记记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id) {
        checkInService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据客人ID查询入住记录列表
     *
     * @param guestId 客人ID
     * @return 入住记录列表
     */
    @Operation(summary = "按客人ID查询入住记录", description = "根据客人ID查询该客人的所有入住记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<CheckIn>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId) {
        return ResponseResult.success(checkInService.findByGuestId(guestId));
    }

    /**
     * 根据房间ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @return 入住记录列表
     */
    @Operation(summary = "按房间ID查询入住记录", description = "根据房间ID查询关联该房间的入住记录列表")
    @GetMapping("/search/byRoomId")
    public ResponseResult<List<CheckIn>> findByRoomId(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId) {
        return ResponseResult.success(checkInService.findByRoomId(roomId));
    }

    /**
     * 根据状态查询入住记录列表
     *
     * @param status 入住状态
     * @return 入住记录列表
     */
    @Operation(summary = "按状态查询入住记录", description = "根据入住状态查询入住记录列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<CheckIn>> findByStatus(
            @Parameter(description = "入住状态", required = true) @RequestParam String status) {
        return ResponseResult.success(checkInService.findByStatus(status));
    }

    /**
     * 根据预订ID查询入住记录列表
     *
     * @param reservationId 预订ID
     * @return 入住记录列表
     */
    @Operation(summary = "按预订ID查询入住记录", description = "根据预订ID查询关联该预订的入住记录列表")
    @GetMapping("/search/byReservationId")
    public ResponseResult<List<CheckIn>> findByReservationId(
            @Parameter(description = "预订ID", required = true) @RequestParam Integer reservationId) {
        return ResponseResult.success(checkInService.findByReservationId(reservationId));
    }

    /**
     * 根据房间ID和状态查询入住记录列表
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    @Operation(summary = "按房间ID和状态查询入住记录", description = "根据房间ID和入住状态联合查询入住记录列表")
    @GetMapping("/search/byRoomIdAndStatus")
    public ResponseResult<List<CheckIn>> findByRoomIdAndStatus(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId,
            @Parameter(description = "入住状态", required = true) @RequestParam String status) {
        return ResponseResult.success(checkInService.findByRoomIdAndStatus(roomId, status));
    }
}