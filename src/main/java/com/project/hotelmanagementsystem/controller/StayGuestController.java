package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.StayGuest;
import com.project.hotelmanagementsystem.service.StayGuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 同住客人控制层
 * <p>
 * 负责入住登记中同住客人信息的增删改查及按入住记录、客人、是否主客条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "同住客人管理", description = "同住客人信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/stay-guests")
public class StayGuestController {

    private final StayGuestService stayGuestService;

    /**
     * 构造函数注入同住客人Service
     *
     * @param stayGuestService 同住客人Service
     */
    public StayGuestController(StayGuestService stayGuestService) {
        this.stayGuestService = stayGuestService;
    }

    /**
     * 查询所有同住客人
     *
     * @return 同住客人列表
     */
    @Operation(summary = "查询所有同住客人", description = "返回系统中所有同住客人记录的列表")
    @GetMapping
    public ResponseResult<List<StayGuest>> findAll() {
        return ResponseResult.success(stayGuestService.findAll());
    }

    /**
     * 根据ID查询同住客人
     *
     * @param id 同住客人ID
     * @return 同住客人信息，不存在返回404
     */
    @Operation(summary = "根据ID查询同住客人", description = "根据同住客人ID查询单条同住客人信息")
    @GetMapping("/{id}")
    public ResponseResult<StayGuest> findById(
            @Parameter(description = "同住客人ID", required = true) @PathVariable Integer id) {
        return stayGuestService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增同住客人
     *
     * @param stayGuest 同住客人信息
     * @return 创建后的同住客人信息
     */
    @Operation(summary = "新增同住客人", description = "创建一条新的同住客人记录")
    @PostMapping
    public ResponseResult<StayGuest> create(
            @Parameter(description = "同住客人信息", required = true) @RequestBody StayGuest stayGuest) {
        StayGuest saved = stayGuestService.save(stayGuest);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新同住客人信息
     *
     * @param id        同住客人ID
     * @param stayGuest 同住客人信息
     * @return 更新后的同住客人信息，不存在返回404
     */
    @Operation(summary = "更新同住客人信息", description = "根据同住客人ID更新同住客人信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<StayGuest> update(
            @Parameter(description = "同住客人ID", required = true) @PathVariable Integer id,
            @Parameter(description = "同住客人信息", required = true) @RequestBody StayGuest stayGuest) {
        return stayGuestService.findById(id)
                .map(existing -> {
                    stayGuest.setId(id);
                    return ResponseResult.success(stayGuestService.save(stayGuest));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除同住客人
     *
     * @param id 同住客人ID
     * @return 删除结果
     */
    @Operation(summary = "删除同住客人", description = "根据同住客人ID删除同住客人记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "同住客人ID", required = true) @PathVariable Integer id) {
        stayGuestService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据入住ID查询同住客人列表
     *
     * @param checkInId 入住ID
     * @return 同住客人列表
     */
    @Operation(summary = "按入住ID查询同住客人", description = "根据入住ID查询该次入住的所有同住客人")
    @GetMapping("/search/byCheckInId")
    public ResponseResult<List<StayGuest>> findByCheckInId(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId) {
        return ResponseResult.success(stayGuestService.findByCheckInId(checkInId));
    }

    /**
     * 根据客人ID查询同住记录列表
     *
     * @param guestId 客人ID
     * @return 同住记录列表
     */
    @Operation(summary = "按客人ID查询同住记录", description = "根据客人ID查询该客人作为同住人的所有记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<StayGuest>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId) {
        return ResponseResult.success(stayGuestService.findByGuestId(guestId));
    }

    /**
     * 根据入住ID和是否主客查询同住客人列表
     *
     * @param checkInId 入住ID
     * @param isPrimary 是否主客
     * @return 同住客人列表
     */
    @Operation(summary = "按入住ID和是否主客查询同住客人", description = "根据入住ID和是否主客联合查询同住客人列表")
    @GetMapping("/search/byCheckInIdAndIsPrimary")
    public ResponseResult<List<StayGuest>> findByCheckInIdAndIsPrimary(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId,
            @Parameter(description = "是否主客", required = true) @RequestParam Boolean isPrimary) {
        return ResponseResult.success(stayGuestService.findByCheckInIdAndIsPrimary(checkInId, isPrimary));
    }
}